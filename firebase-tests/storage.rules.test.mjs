import { after, before, beforeEach, test } from 'node:test';
import { readFile } from 'node:fs/promises';
import { initializeTestEnvironment, assertFails } from '@firebase/rules-unit-testing';
import { doc, setDoc } from 'firebase/firestore';
import { ref, uploadBytes, getMetadata, getBytes, getDownloadURL, deleteObject, listAll, updateMetadata } from 'firebase/storage';

let env;
const path = 'couples/pair/wishes/wish/photo.jpg';
const bytes = new Uint8Array([0xff, 0xd8, 0xff, 0xd9]);
const image = (uid, objectPath = path) => ref(
  (uid ? env.authenticatedContext(uid) : env.unauthenticatedContext()).storage(), objectPath,
);

before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-you2',
    firestore: { rules: await readFile(new URL('../firestore.rules', import.meta.url), 'utf8') },
    storage: { rules: await readFile(new URL('../storage.rules', import.meta.url), 'utf8') },
  });
});
beforeEach(async () => {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async context => {
    async function clearPrefix(prefix) {
      const result = await listAll(prefix);
      await Promise.all(result.items.map(item => deleteObject(item)));
      await Promise.all(result.prefixes.map(clearPrefix));
    }
    await clearPrefix(ref(context.storage()));
    const db = context.firestore();
    await setDoc(doc(db, 'couples/pair'), { memberIds: ['alice', 'bob'] });
    await setDoc(doc(db, 'couples/pair/wishes/wish'), { title: 'Fixture' });
    // Existing files must also be inaccessible through authenticated SDK requests.
    await uploadBytes(ref(context.storage(), path), bytes, { contentType: 'image/jpeg' });
  });
});
after(async () => { await env.cleanup(); });

for (const uid of [null, 'alice', 'bob', 'eve']) {
  test(`${uid ?? 'guest'} cannot upload images or arbitrary bytes at any path`, async () => {
    for (const objectPath of [
      'couples/pair/wishes/wish/new.jpg', 'couples/pair/wishes/wish/new.png',
      'couples/pair/wishes/wish/new.webp', 'couples/pair/wishes/missing/new.jpg',
      'couples/other/wishes/wish/new.jpg', 'users/alice/photo.jpg', 'photo.jpg',
    ]) {
      await assertFails(uploadBytes(image(uid, objectPath), bytes, { contentType: 'image/jpeg' }));
      await assertFails(uploadBytes(image(uid, objectPath), new TextEncoder().encode('not an image'), {
        contentType: 'image/jpeg',
      }));
    }
  });

  test(`${uid ?? 'guest'} cannot read, list, overwrite, delete, or obtain download tokens`, async () => {
    await assertFails(getMetadata(image(uid)));
    await assertFails(getBytes(image(uid)));
    await assertFails(getDownloadURL(image(uid)));
    await assertFails(listAll(image(uid, 'couples/pair/wishes/wish')));
    await assertFails(listAll(image(uid, '')));
    await assertFails(uploadBytes(image(uid), bytes, { contentType: 'image/jpeg' }));
    await assertFails(updateMetadata(image(uid), { customMetadata: { anything: 'changed' } }));
    await assertFails(deleteObject(image(uid)));
  });
}
