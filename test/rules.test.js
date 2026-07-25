const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const fs = require('fs');
const path = require('path');

let testEnv;

describe('Firestore Security Rules', () => {
  beforeAll(async () => {
    testEnv = await initializeTestEnvironment({
      projectId: 'itihasa-test-project',
      firestore: {
        rules: fs.readFileSync(path.resolve(__dirname, '../firestore.rules'), 'utf8'),
      },
    });
  });

  afterAll(async () => {
    await testEnv.cleanup();
  });

  beforeEach(async () => {
    await testEnv.clearFirestore();
  });

  it('allows authenticated users to read public heritage sites and challenges', async () => {
    const aliceDb = testEnv.authenticatedContext('alice_uid', { email: 'alice@example.com' }).firestore();
    await assertSucceeds(aliceDb.doc('heritage_sites/hampi').get());
    await assertSucceeds(aliceDb.doc('heritage_sites/hampi/hunts/hampi_intro/challenges/gopuram').get());
  });

  it('denies client access to the secret answers subcollection', async () => {
    const aliceDb = testEnv.authenticatedContext('alice_uid', { email: 'alice@example.com' }).firestore();
    await assertFails(aliceDb.doc('heritage_sites/hampi/hunts/hampi_intro/answers/gopuram').get());
  });

  it('allows users to read their own hunt_progress but denies unauthenticated access', async () => {
    const aliceDb = testEnv.authenticatedContext('alice_uid', { email: 'alice@example.com' }).firestore();
    const unauthDb = testEnv.unauthenticatedContext().firestore();

    await assertSucceeds(aliceDb.doc('users/alice@example.com/hunt_progress/hampi_hampi_intro').get());
    await assertFails(unauthDb.doc('users/alice@example.com/hunt_progress/hampi_hampi_intro').get());
  });

  it('denies user A reading user B hunt_progress', async () => {
    const aliceDb = testEnv.authenticatedContext('alice_uid', { email: 'alice@example.com' }).firestore();
    await assertFails(aliceDb.doc('users/bob@example.com/hunt_progress/hampi_hampi_intro').get());
  });
});
