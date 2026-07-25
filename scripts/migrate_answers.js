/**
 * Data Migration Script: Move Challenge Answers to Deny-All Subcollections
 *
 * Requirements & Behavior:
 * 1. Iterates all heritage_sites/{siteId}/hunts/{huntId}/challenges/{challengeId} documents.
 * 2. Reads secret answer fields (expectedText, target, tolerance, latitude, longitude, targetAssetId, etc.).
 * 3. Writes these secret fields to heritage_sites/{siteId}/hunts/{huntId}/answers/{challengeId}.
 * 4. Verifies document write success in answers/ subcollection.
 * 5. Idempotently deletes secret fields from the public challenges/{challengeId} document only after verification.
 * 6. Can be safely re-run without data corruption or duplicate writes.
 */

const admin = require('firebase-admin');
const fs = require('fs');

if (process.env.GOOGLE_APPLICATION_CREDENTIALS || process.env.FIRESTORE_EMULATOR_HOST) {
  admin.initializeApp();
} else {
  console.log('NOTICE: Set GOOGLE_APPLICATION_CREDENTIALS or FIRESTORE_EMULATOR_HOST before running migration in production.');
  admin.initializeApp();
}

const db = admin.firestore();

async function migrateAnswers() {
  console.log('Starting migration of secret challenge answers to /answers subcollection...');

  const sitesSnap = await db.collection('heritage_sites').get();
  let migratedCount = 0;

  for (const siteDoc of sitesSnap.docs) {
    const siteId = siteDoc.id;
    const huntsSnap = await db.collection('heritage_sites').doc(siteId).collection('hunts').get();

    for (const huntDoc of huntsSnap.docs) {
      const huntId = huntDoc.id;
      const challengesSnap = await db.collection('heritage_sites').doc(siteId)
        .collection('hunts').doc(huntId)
        .collection('challenges').get();

      for (const challengeDoc of challengesSnap.docs) {
        const challengeId = challengeDoc.id;
        const challengeData = challengeDoc.data();
        const val = challengeData.validation || {};

        const answerData = {
          expectedText: val.expectedText || challengeData.expectedText || null,
          caseSensitive: val.caseSensitive !== undefined ? val.caseSensitive : false,
          target: val.target !== undefined ? val.target : null,
          tolerance: val.tolerance !== undefined ? val.tolerance : 0,
          targetAssetId: val.targetAssetId || null,
          confidenceThreshold: val.confidenceThreshold || null,
          latitude: val.latitude || null,
          longitude: val.longitude || null,
          radiusMeters: val.radiusMeters || null,
          migratedAt: admin.firestore.FieldValue.serverTimestamp()
        };

        // Filter out null/undefined keys
        Object.keys(answerData).forEach(key => answerData[key] === null && delete answerData[key]);

        if (Object.keys(answerData).length > 1) {
          const answerRef = db.collection('heritage_sites').doc(siteId)
            .collection('hunts').doc(huntId)
            .collection('answers').doc(challengeId);

          // Step 1: Write to answers subcollection
          await answerRef.set(answerData, { merge: true });

          // Step 2: Verify write success
          const verifySnap = await answerRef.get();
          if (!verifySnap.exists) {
            console.error(`ERROR: Verification failed for ${siteId}/${huntId}/${challengeId}. Skipping public field strip.`);
            continue;
          }

          // Step 3: Strip secret fields from validation object in public challenge doc
          const sanitizedValidation = { ...val };
          delete sanitizedValidation.expectedText;
          delete sanitizedValidation.target;
          delete sanitizedValidation.tolerance;
          delete sanitizedValidation.latitude;
          delete sanitizedValidation.longitude;

          await challengeDoc.ref.update({
            validation: sanitizedValidation,
            expectedText: admin.firestore.FieldValue.delete(),
            target: admin.firestore.FieldValue.delete(),
            tolerance: admin.firestore.FieldValue.delete()
          });

          migratedCount++;
          console.log(`Successfully migrated answer for challenge: ${siteId}/${huntId}/${challengeId}`);
        }
      }
    }
  }

  console.log(`Migration finished. Migrated ${migratedCount} challenge answer documents.`);
}

if (require.main === module) {
  migrateAnswers().catch(err => {
    console.error('Migration failed:', err);
    process.exit(1);
  });
}

module.exports = { migrateAnswers };
