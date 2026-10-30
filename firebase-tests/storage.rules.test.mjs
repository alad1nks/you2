import { after, before, beforeEach, test } from 'node:test';
import { readFile } from 'node:fs/promises';
import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { doc, setDoc, deleteDoc, updateDoc } from 'firebase/firestore';
import { ref, uploadBytes, getMetadata, getBytes, getDownloadURL, deleteObject, listAll, updateMetadata } from 'firebase/storage';

let env;
const path = 'couples/pair/wishes/wish/photo.jpg';
const bytes = new Uint8Array([0xff, 0xd8, 0xff, 0xd9]);
const image = (uid, objectPath = path) => ref(
  (uid ? env.authenticatedContext(uid) : env.unauthenticatedContext()).storage(), objectPath,
);
const upload = (uid, objectPath = path, data = bytes, contentType = 'image/jpeg') =>
  uploadBytes(image(uid, objectPath), data, { contentType });

before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-you2',
    firestore: { rules: await readFile(new URL('../firestore.rules', import.meta.url), 'utf8') },
    storage: { rules: await readFile(new URL('../storage.rules', import.meta.url), 'utf8') },
  });
});
beforeEach(async () => {
  // clearStorage() only deletes root-level items in this SDK version, not nested prefixes.
  await env.withSecurityRulesDisabled(async context => {
    async function clearPrefix(prefix) {
      const result = await listAll(prefix);
      await Promise.all(result.items.map(item => deleteObject(item)));
      await Promise.all(result.prefixes.map(clearPrefix));
    }
    await clearPrefix(ref(context.storage()));
  });
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async context => {
    const db = context.firestore();
    await setDoc(doc(db, 'couples/pair'), { memberIds: ['alice', 'bob'] });
    await setDoc(doc(db, 'couples/pair/wishes/wish'), { title: 'Fixture' });
    await setDoc(doc(db, 'couples/other'), { memberIds: ['eve'] });
    await setDoc(doc(db, 'couples/other/wishes/wish'), { title: 'Other pair' });
  });
});
after(async () => { await env.cleanup(); });

test('both partners can upload, read, and delete shared images', async () => {
  await assertSucceeds(upload('alice'));
  await assertSucceeds(getMetadata(image('bob')));
  await assertSucceeds(getBytes(image('bob')));
  await assertSucceeds(deleteObject(image('bob')));
  await assertSucceeds(upload('bob'));
  await assertSucceeds(getMetadata(image('alice')));
  await assertSucceeds(deleteObject(image('alice')));
});

test('guests and other couples cannot access known image paths', async () => {
  await upload('alice');
  for (const uid of [null, 'eve']) {
    await assertFails(getMetadata(image(uid)));
    await assertFails(getBytes(image(uid)));
    await assertFails(getDownloadURL(image(uid)));
    await assertFails(deleteObject(image(uid)));
    await assertFails(upload(uid, 'couples/pair/wishes/wish/new.jpg'));
  }
  await assertFails(upload('alice', 'couples/other/wishes/wish/new.jpg'));
});

test('missing couples and missing wishes cannot receive uploads', async () => {
  await assertFails(upload('alice', 'couples/missing/wishes/wish/photo.jpg'));
  await assertFails(upload('alice', 'couples/pair/wishes/missing/photo.jpg'));
});

test('deleted wishes hide images but members can clean up remaining files', async () => {
  await upload('alice');
  await env.withSecurityRulesDisabled(context => deleteDoc(doc(context.firestore(), 'couples/pair/wishes/wish')));
  await assertFails(getMetadata(image('bob')));
  await assertFails(upload('bob', 'couples/pair/wishes/wish/new.jpg'));
  await assertSucceeds(deleteObject(image('bob')));
});

test('removed members lose access even if they know the path', async () => {
  await upload('alice');
  await env.withSecurityRulesDisabled(context => updateDoc(doc(context.firestore(), 'couples/pair'), { memberIds: ['alice'] }));
  await assertFails(getMetadata(image('bob')));
  await assertFails(deleteObject(image('bob')));
  await assertFails(upload('bob', 'couples/pair/wishes/wish/new.jpg'));
});

test('JPEG, PNG, and WebP are accepted only with matching extensions', async () => {
  await assertSucceeds(upload('alice', path));
  await assertSucceeds(upload('alice', 'couples/pair/wishes/wish/photo.png', bytes, 'image/png'));
  await assertSucceeds(upload('alice', 'couples/pair/wishes/wish/photo.webp', bytes, 'image/webp'));
  for (const [name, type] of [
    ['wrong.png', 'image/jpeg'], ['script.svg', 'image/svg+xml'], ['movie.mp4', 'video/mp4'],
    ['image.gif', 'image/gif'], ['invalid.jpg', 'application/octet-stream'], ['photo', 'image/jpeg'],
  ]) {
    await assertFails(upload('alice', `couples/pair/wishes/wish/${name}`, bytes, type));
  }
});

test('empty images and images over 5 MiB are rejected; the boundary is accepted', async () => {
  await assertFails(upload('alice', path, new Uint8Array(0)));
  await assertFails(upload('alice', path, new Uint8Array(5 * 1024 * 1024 + 1)));
  await assertSucceeds(upload('alice', path, new Uint8Array(5 * 1024 * 1024)));
});

test('overwrites and metadata changes are forbidden', async () => {
  await upload('alice');
  await assertFails(upload('alice'));
  await assertFails(upload('bob'));
  await assertFails(updateMetadata(image('alice'), { contentType: 'image/png' }));
  await assertFails(updateMetadata(image('bob'), { customMetadata: { anything: 'changed' } }));
});

test('uploads reject validation claims and arbitrary application metadata', async () => {
  for (const customMetadata of [
    { validated: 'true' },
    { ownerId: 'bob' },
    { anything: 'unexpected' },
  ]) {
    await assertFails(uploadBytes(image('alice'), bytes, { contentType: 'image/jpeg', customMetadata }));
  }
  await assertSucceeds(uploadBytes(image('alice'), bytes, { contentType: 'image/jpeg', customMetadata: {} }));
});

test('uploads reject encoded bodies and arbitrary disposition overrides', async () => {
  for (const contentEncoding of ['gzip', 'br', 'identity, gzip']) {
    await assertFails(uploadBytes(image('alice'), bytes, { contentType: 'image/jpeg', contentEncoding }));
  }
  for (const contentDisposition of ['form-data', 'inline; filename="script.html"', 'attachment; filename="script.html"']) {
    await assertFails(uploadBytes(image('alice'), bytes, { contentType: 'image/jpeg', contentDisposition }));
  }
  await assertSucceeds(uploadBytes(image('alice'), bytes, {
    contentType: 'image/jpeg', contentEncoding: 'identity', contentDisposition: 'inline',
  }));
  await assertSucceeds(uploadBytes(image('alice', 'couples/pair/wishes/wish/attachment.jpg'), bytes, {
    contentType: 'image/jpeg', contentDisposition: 'attachment',
  }));
});

test('listing, unexpected paths, nested paths, and unsupported filenames are denied', async () => {
  await upload('alice');
  await assertFails(listAll(image('alice', 'couples/pair/wishes/wish')));
  await assertFails(listAll(image('alice', 'couples')));
  for (const objectPath of [
    'photo.jpg', 'users/alice/photo.jpg', 'couples/pair/photo.jpg',
    'couples/pair/wishes/wish/nested/photo.jpg', 'couples/pair/wishes/wish/bad name.jpg',
    `couples/pair/wishes/wish/${'a'.repeat(129)}.jpg`,
  ]) {
    await assertFails(upload('alice', objectPath));
  }
});
