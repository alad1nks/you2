import { after, before, beforeEach, test } from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import {
  doc, collection, getDoc, getDocs, setDoc, updateDoc, deleteDoc,
  writeBatch, runTransaction, serverTimestamp, Timestamp, arrayUnion,
} from 'firebase/firestore';

let env;
const code = 'a'.repeat(32);
const nextCode = 'b'.repeat(32);
const db = (uid) => env.authenticatedContext(uid).firestore();
before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-you2',
    firestore: { rules: await readFile(new URL('../firestore.rules', import.meta.url), 'utf8') },
  });
});
beforeEach(async () => { await env.clearFirestore(); });
after(async () => { await env.cleanup(); });

async function profile(uid) {
  await setDoc(doc(db(uid), 'users', uid), { name: '', coupleId: null });
}
function invitation(ownerId, coupleId) {
  return { ownerId, coupleId, usedBy: null, createdAt: serverTimestamp(), expiresAt: Timestamp.fromMillis(Date.now() + 86400000) };
}
async function createPair() {
  await profile('alice');
  const client = db('alice');
  // Mirrors the Android transaction, including reading an absent couple document.
  await runTransaction(client, async tx => {
    await tx.get(doc(client, 'users/alice'));
    await tx.get(doc(client, 'couples/pair'));
    tx.set(doc(client, 'couples/pair'), { memberIds: ['alice'], inviteId: code, createdAt: serverTimestamp() });
    tx.update(doc(client, 'users/alice'), { name: 'Alice', coupleId: 'pair' });
    tx.set(doc(client, 'invites', code), invitation('alice', 'pair'));
  });
}
async function join(uid = 'bob', inviteCode = code) {
  const client = db(uid);
  return runTransaction(client, async tx => {
    const invite = await tx.get(doc(client, 'invites', inviteCode));
    await tx.get(doc(client, 'users', uid));
    tx.update(doc(client, 'couples', invite.data().coupleId), { memberIds: arrayUnion(uid) });
    tx.update(doc(client, 'users', uid), { name: uid, coupleId: invite.data().coupleId });
    tx.update(doc(client, 'invites', inviteCode), { usedBy: uid });
  });
}
function wish() {
  return {
    authorId: 'alice', createdOn: '2026-10-31', createdAt: serverTimestamp(), updatedAt: serverTimestamp(), completion: null,
    content: { title: 'Mountains', category: 'ADVENTURES', description: '', dates: { type: 'none' }, cover: null, attachments: [] },
  };
}
async function paired() { await createPair(); await profile('bob'); await join(); }

test('profiles and wishes are private; invite lists are forbidden', async () => {
  await createPair();
  await profile('eve');
  const guest = env.unauthenticatedContext().firestore();
  await assertFails(getDoc(doc(guest, 'users/alice')));
  await assertFails(getDoc(doc(db('eve'), 'users/alice')));
  await assertFails(getDoc(doc(db('eve'), 'couples/pair')));
  await assertFails(getDocs(collection(db('eve'), 'couples/pair/wishes')));
  await assertFails(getDocs(collection(db('eve'), 'invites')));
  await assertFails(getDoc(doc(guest, 'invites', code)));
});

test('a valid invitation atomically joins exactly two users', async () => {
  await paired();
  assert.deepEqual((await getDoc(doc(db('bob'), 'couples/pair'))).data().memberIds, ['alice', 'bob']);
  await assertSucceeds(getDoc(doc(db('bob'), 'users/alice')));
  await assertSucceeds(getDoc(doc(db('alice'), 'users/bob')));
  await profile('eve');
  await assertFails(join('eve'));
});

test('simultaneous joins have only one winner', async () => {
  await createPair(); await profile('bob'); await profile('eve');
  const result = await Promise.allSettled([join('bob'), join('eve')]);
  assert.equal(result.filter(r => r.status === 'fulfilled').length, 1);
  assert.equal((await getDoc(doc(db('alice'), 'couples/pair'))).data().memberIds.length, 2);
});

test('membership, profile pointer, and invite consumption cannot be forged separately', async () => {
  await createPair(); await profile('eve');
  await assertFails(updateDoc(doc(db('eve'), 'couples/pair'), { memberIds: ['alice', 'eve'] }));
  await assertFails(updateDoc(doc(db('eve'), 'users/eve'), { name: 'Eve', coupleId: 'pair' }));
  await assertFails(updateDoc(doc(db('eve'), 'invites', code), { usedBy: 'eve' }));
  await assertFails(updateDoc(doc(db('alice'), 'couples/pair'), { memberIds: ['alice', 'eve'] }));
});

test('expired invitations cannot be used', async () => {
  await createPair(); await profile('bob');
  await env.withSecurityRulesDisabled(async context => {
    await updateDoc(doc(context.firestore(), 'invites', code), { expiresAt: Timestamp.fromMillis(Date.now() - 1000) });
  });
  await assertFails(join());
});

test('only the owner can read expired, superseded, or consumed invitations', async () => {
  await createPair(); await profile('bob');
  const owner = doc(db('alice'), 'invites', code);
  const outsider = doc(db('eve'), 'invites', code);
  await assertSucceeds(getDoc(outsider));
  await env.withSecurityRulesDisabled(context => updateDoc(doc(context.firestore(), 'invites', code), {
    expiresAt: Timestamp.fromMillis(Date.now() - 1000),
  }));
  await assertFails(getDoc(outsider));
  await assertSucceeds(getDoc(owner));
  await env.withSecurityRulesDisabled(context => updateDoc(doc(context.firestore(), 'invites', code), {
    expiresAt: Timestamp.fromMillis(Date.now() + 86400000),
  }));
  const client = db('alice');
  const batch = writeBatch(client);
  batch.update(doc(client, 'couples/pair'), { inviteId: nextCode });
  batch.set(doc(client, 'invites', nextCode), invitation('alice', 'pair'));
  await batch.commit();
  await assertFails(getDoc(outsider));
  await assertSucceeds(getDoc(owner));
  await assertSucceeds(getDoc(doc(db('bob'), 'invites', nextCode)));
  await join('bob', nextCode);
  await assertFails(getDoc(doc(db('eve'), 'invites', nextCode)));
  await assertFails(getDoc(doc(db('bob'), 'invites', nextCode)));
  await assertSucceeds(getDoc(doc(client, 'invites', nextCode)));
});

test('all date fields reject impossible calendar dates, including non-leap centuries', async () => {
  await paired();
  const ref = doc(db('alice'), 'couples/pair/wishes/wish');
  await setDoc(ref, wish());
  for (const date of ['2026-99-99', '2026-00-01', '2026-01-00', '2026-04-31',
    '2026-02-29', '1900-02-29', '2100-02-29', '2026-02-30', '0000-01-01',
    '2026-1-01', 'not-a-date', null, 20261031]) {
    await assertFails(setDoc(doc(db('alice'), 'couples/pair/wishes/invalid'), { ...wish(), createdOn: date }));
    for (const dates of [
      { type: 'deadline', end: date },
      { type: 'range', start: date, end: '9999-12-31' },
      { type: 'range', start: '0001-01-01', end: date },
    ]) {
      await assertFails(updateDoc(ref, { 'content.dates': dates, updatedAt: serverTimestamp() }));
    }
    await assertFails(updateDoc(ref, {
      completion: { date, authorId: 'alice', memory: '' }, updatedAt: serverTimestamp(),
    }));
  }
});

test('valid calendar boundaries and leap years are accepted in all date fields', async () => {
  await paired();
  for (const date of ['0001-01-01', '9999-12-31', '2026-04-30', '2026-01-31',
    '2026-02-28', '2024-02-29', '2000-02-29', '2400-02-29']) {
    const ref = doc(db('alice'), 'couples/pair/wishes', date);
    await assertSucceeds(setDoc(ref, { ...wish(), createdOn: date }));
    for (const dates of [{ type: 'deadline', end: date }, { type: 'range', start: date, end: date }]) {
      await assertSucceeds(updateDoc(ref, { 'content.dates': dates, updatedAt: serverTimestamp() }));
    }
    await assertSucceeds(updateDoc(ref, {
      completion: { date, authorId: 'alice', memory: '' }, updatedAt: serverTimestamp(),
    }));
  }
});

test('renewal invalidates the previous invitation', async () => {
  await createPair(); await profile('bob');
  const client = db('alice');
  const batch = writeBatch(client);
  batch.update(doc(client, 'couples/pair'), { inviteId: nextCode });
  batch.set(doc(client, 'invites', nextCode), invitation('alice', 'pair'));
  await assertSucceeds(batch.commit());
  await assertFails(join());
  await assertSucceeds(join('bob', nextCode));
});

test('existing members cannot join another pair or replace their partner', async () => {
  await paired();
  await assertFails(join('alice'));
  await assertFails(updateDoc(doc(db('alice'), 'users/alice'), { coupleId: null }));
  await assertFails(updateDoc(doc(db('alice'), 'couples/pair'), { memberIds: ['alice', 'eve'] }));
});

test('partners can create, edit, complete, annotate, restore, and delete wishes', async () => {
  await paired();
  const mine = doc(db('alice'), 'couples/pair/wishes/wish');
  const partner = doc(db('bob'), 'couples/pair/wishes/wish');
  await assertSucceeds(setDoc(mine, wish()));
  await assertSucceeds(updateDoc(partner, { 'content.title': 'A weekend', updatedAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(partner, {
    completion: { date: '2026-10-31', authorId: 'bob', memory: '' }, updatedAt: serverTimestamp(),
  }));
  await assertSucceeds(updateDoc(mine, { 'completion.memory': 'Wonderful!', updatedAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(partner, { completion: null, updatedAt: serverTimestamp() }));
  await assertSucceeds(deleteDoc(partner));
});

test('outsiders cannot read or mutate wishes even when they know document IDs', async () => {
  await paired(); await profile('eve');
  await setDoc(doc(db('alice'), 'couples/pair/wishes/wish'), wish());
  const ref = doc(db('eve'), 'couples/pair/wishes/wish');
  await assertFails(getDoc(ref));
  await assertFails(updateDoc(ref, { 'content.title': 'Hacked', updatedAt: serverTimestamp() }));
  await assertFails(deleteDoc(ref));
});

test('authors and creation fields are immutable; completion cannot impersonate a partner', async () => {
  await paired();
  const ref = doc(db('alice'), 'couples/pair/wishes/wish');
  await assertFails(setDoc(ref, { ...wish(), authorId: 'bob' }));
  await setDoc(ref, wish());
  await assertFails(updateDoc(ref, { authorId: 'bob', updatedAt: serverTimestamp() }));
  await assertFails(updateDoc(ref, { createdOn: '2020-01-01', updatedAt: serverTimestamp() }));
  await assertFails(updateDoc(ref, { completion: { date: '2026-10-31', authorId: 'bob', memory: '' }, updatedAt: serverTimestamp() }));
});

test('invalid content and profile changes are rejected', async () => {
  await paired();
  const ref = doc(db('alice'), 'couples/pair/wishes/wish');
  for (const change of [
    { title: ' ' }, { title: 'a'.repeat(81) }, { description: 'x'.repeat(501) }, { category: 'INVALID' },
    { dates: { type: 'range', start: '2026-12-01', end: '2026-01-01' } },
    { attachments: ['INVALID'] }, { attachments: Array(13).fill('OTHER') },
  ]) {
    await assertFails(setDoc(ref, { ...wish(), content: { ...wish().content, ...change } }));
  }
  await assertFails(updateDoc(doc(db('alice'), 'users/alice'), { name: ' ' }));
  await assertFails(updateDoc(doc(db('bob'), 'users/alice'), { name: 'Changed' }));
  await assertSucceeds(updateDoc(doc(db('alice'), 'users/alice'), { name: 'New name' }));
});

test('nested memory edits cannot revive a restored wish', async () => {
  await paired();
  const ref = doc(db('alice'), 'couples/pair/wishes/wish');
  await setDoc(ref, wish());
  await assertFails(updateDoc(ref, { 'completion.memory': 'Stale editor', updatedAt: serverTimestamp() }));
});

test('transactional completion preserves an existing memory when a stale client completes again', async () => {
  await paired();
  const ref = doc(db('alice'), 'couples/pair/wishes/wish');
  await setDoc(ref, wish());
  await updateDoc(ref, {
    completion: { date: '2026-10-31', authorId: 'alice', memory: 'Keep this memory' }, updatedAt: serverTimestamp(),
  });
  const partner = db('bob');
  await assertSucceeds(runTransaction(partner, async tx => {
    const target = doc(partner, 'couples/pair/wishes/wish');
    const existing = await tx.get(target);
    if (existing.data().completion == null) {
      tx.update(target, { completion: { date: '2026-10-31', authorId: 'bob', memory: '' }, updatedAt: serverTimestamp() });
    }
  }));
  assert.equal((await getDoc(ref)).data().completion.memory, 'Keep this memory');
  assert.equal((await getDoc(ref)).data().completion.authorId, 'alice');
});


test('wish image references accept private paths and reject foreign or malformed paths at every position', async () => {
  await paired();
  const ref = doc(db('alice'), 'couples/pair/wishes/photos');
  const path = 'couples/pair/wishes/photos/image.jpg';
  const data = wish();
  data.content.cover = path;
  data.content.attachments = Array(12).fill(path);
  data.content.dates = { type: 'range', start: '2024-02-29', end: '2026-12-31' };
  await assertSucceeds(setDoc(ref, data));
  await assertSucceeds(updateDoc(ref, { completion: { date: '2024-02-29', authorId: 'alice', memory: 'Photos' }, updatedAt: serverTimestamp() }));
  await assertSucceeds(getDoc(doc(db('bob'), 'couples/pair/wishes/photos')));
  for (const invalid of [
    'couples/other/wishes/photos/image.jpg', 'couples/pair/wishes/other/image.jpg',
    'https://example.com/image.jpg', 'file:///cache/image.jpg',
    'couples/pair/wishes/photos/image.gif', 'couples/pair/wishes/photos/nested/image.jpg',
    'couples/pair/wishes/photos/imageXjpg', 'couples/pair/wishes/photos/../image.jpg',
  ]) {
    await assertFails(updateDoc(ref, { 'content.cover': invalid, updatedAt: serverTimestamp() }));
    for (let index = 0; index < 12; index++) {
      const attachments = Array(12).fill(path);
      attachments[index] = invalid;
      await assertFails(updateDoc(ref, { 'content.attachments': attachments, updatedAt: serverTimestamp() }));
    }
  }
  await assertFails(updateDoc(ref, { 'content.attachments': Array(13).fill(path), updatedAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(ref, { 'content.cover': null, 'content.attachments': ['PLANS', path], updatedAt: serverTimestamp() }));
});
