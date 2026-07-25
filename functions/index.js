const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();
const db = admin.firestore();

/**
 * Normalizes string input for text validation.
 */
function normalizeText(text, caseSensitive) {
  if (!text) return '';
  const trimmed = text.trim().replace(/\s+/g, ' ');
  return caseSensitive ? trimmed : trimmed.toLowerCase();
}

/**
 * Cloud Function: verifyChallengeAnswer
 * Verified by Firebase App Check, executed in a Firestore transaction with atomic rate-limiting,
 * progression checking, replay protection, and server-side point awards.
 */
exports.verifyChallengeAnswer = functions.https.onCall(async (data, context) => {
  // 1. Enforce App Check verification
  if (!context.app && process.env.NODE_ENV === 'production') {
    throw new functions.https.HttpsError(
      'failed-precondition',
      'The function must be called from a verified app instance (App Check failed).'
    );
  }

  // 2. Enforce Authentication
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'User must be signed in to submit answers.'
    );
  }

  const userEmail = context.auth.token.email || context.auth.uid;
  const { siteId, huntId, challengeId, userInput } = data || {};

  if (!siteId || !huntId || !challengeId || userInput === undefined) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'Missing required parameters: siteId, huntId, challengeId, or userInput.'
    );
  }

  const progressRef = db.collection('users').doc(userEmail)
    .collection('hunt_progress').doc(`${siteId}_${huntId}`);

  const challengeRef = db.collection('heritage_sites').doc(siteId)
    .collection('hunts').doc(huntId)
    .collection('challenges').doc(challengeId);

  const answerRef = db.collection('heritage_sites').doc(siteId)
    .collection('hunts').doc(huntId)
    .collection('answers').doc(challengeId);

  const allChallengesRef = db.collection('heritage_sites').doc(siteId)
    .collection('hunts').doc(huntId)
    .collection('challenges');

  return db.runTransaction(async (transaction) => {
    const progressDoc = await transaction.get(progressRef);
    const challengeDoc = await transaction.get(challengeRef);
    const answerDoc = await transaction.get(answerRef);

    if (!challengeDoc.exists) {
      return { isCorrect: false, errorCode: 'CHALLENGE_NOT_FOUND', message: 'Challenge document not found.' };
    }

    const challengeData = challengeDoc.data();
    const progressData = progressDoc.exists ? progressDoc.data() : {
      currentChallengeIndex: 0,
      completedChallengeIds: [],
      earnedPoints: 0,
      failedAttempts: 0,
      lockedUntil: 0
    };

    const now = Date.now();
    const lockedUntil = progressData.lockedUntil || 0;
    if (now < lockedUntil) {
      const waitSeconds = Math.ceil((lockedUntil - now) / 1000);
      return {
        isCorrect: false,
        errorCode: 'LOCKED_OUT',
        lockedUntil,
        message: `Too many incorrect attempts. Please wait ${waitSeconds} seconds before trying again.`
      };
    }

    const completedIds = new Set(progressData.completedChallengeIds || []);

    // Replay protection
    if (completedIds.has(challengeId)) {
      return {
        isCorrect: false,
        errorCode: 'ALREADY_COMPLETED',
        message: 'This challenge has already been completed.'
      };
    }

    // Progression check: ensure user is on this challenge
    const allChallengesSnap = await transaction.get(allChallengesRef);
    const orderedChallenges = allChallengesSnap.docs
      .map(doc => ({ id: doc.id, order: doc.data().order || 0 }))
      .sort((a, b) => a.order - b.order || a.id.localeCompare(b.id));

    const expectedChallenge = orderedChallenges[progressData.currentChallengeIndex || 0];
    if (!expectedChallenge || expectedChallenge.id !== challengeId) {
      return {
        isCorrect: false,
        errorCode: 'OUT_OF_ORDER',
        message: 'You must complete challenges in order.'
      };
    }

    // Secret answer lookup (fallback to legacy fields in challengeData for backwards compatibility during cutover)
    const secretAnswer = answerDoc.exists ? answerDoc.data() : (challengeData.validation || {});
    const valType = (challengeData.validation?.type || secretAnswer.type || '').toUpperCase();

    let isMatch = false;
    if (valType === 'TEXT' || valType === 'MULTIPLE_CHOICE') {
      const expected = normalizeText(secretAnswer.expectedText, secretAnswer.caseSensitive);
      const actual = normalizeText(typeof userInput === 'string' ? userInput : String(userInput), secretAnswer.caseSensitive);
      isMatch = actual === expected;
    } else if (valType === 'NUMBER') {
      const target = Number(secretAnswer.target);
      const tolerance = Number(secretAnswer.tolerance || 0);
      const actual = Number(userInput);
      isMatch = !isNaN(actual) && Math.abs(actual - target) <= tolerance;
    }

    if (isMatch) {
      const pointsEarned = challengeData.points || 0;
      const updatedCompletedIds = Array.from(completedIds).concat(challengeId);
      const updatedIndex = (progressData.currentChallengeIndex || 0) + 1;
      const updatedPoints = (progressData.earnedPoints || 0) + pointsEarned;

      transaction.set(progressRef, {
        siteId,
        huntId,
        currentChallengeIndex: updatedIndex,
        completedChallengeIds: updatedCompletedIds,
        earnedPoints: updatedPoints,
        isCompleted: updatedIndex >= orderedChallenges.length,
        failedAttempts: 0,
        lockedUntil: 0,
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      }, { merge: true });

      return {
        isCorrect: true,
        pointsEarned,
        discovery: challengeData.discovery || null
      };
    } else {
      const failedAttempts = (progressData.failedAttempts || 0) + 1;
      let newLockedUntil = 0;
      if (failedAttempts >= 10) {
        newLockedUntil = now + (5 * 60 * 1000); // 5 minute lock
      } else if (failedAttempts >= 5) {
        newLockedUntil = now + (30 * 1000); // 30 second lock
      }

      transaction.set(progressRef, {
        failedAttempts,
        lockedUntil: newLockedUntil,
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      }, { merge: true });

      return {
        isCorrect: false,
        errorCode: 'INCORRECT',
        failedAttempts,
        lockedUntil: newLockedUntil,
        message: newLockedUntil > 0 
          ? `Incorrect answer. Account locked for ${Math.ceil((newLockedUntil - now) / 1000)} seconds.`
          : 'That is not quite right. Try again!'
      };
    }
  });
});

/**
 * Cloud Function: skipChallenge
 * Verified by Firebase App Check, executes in transaction and enforces isOptional: true.
 */
exports.skipChallenge = functions.https.onCall(async (data, context) => {
  if (!context.app && process.env.NODE_ENV === 'production') {
    throw new functions.https.HttpsError('failed-precondition', 'App Check verification failed.');
  }
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be signed in.');
  }

  const userEmail = context.auth.token.email || context.auth.uid;
  const { siteId, huntId, challengeId } = data || {};

  const progressRef = db.collection('users').doc(userEmail)
    .collection('hunt_progress').doc(`${siteId}_${huntId}`);

  const challengeRef = db.collection('heritage_sites').doc(siteId)
    .collection('hunts').doc(huntId)
    .collection('challenges').doc(challengeId);

  return db.runTransaction(async (transaction) => {
    const challengeDoc = await transaction.get(challengeRef);
    if (!challengeDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Challenge not found.');
    }

    const challengeData = challengeDoc.data();
    if (!challengeData.isOptional) {
      return { success: false, errorCode: 'NOT_OPTIONAL', message: 'This challenge cannot be skipped.' };
    }

    const progressDoc = await transaction.get(progressRef);
    const progressData = progressDoc.exists ? progressDoc.data() : { currentChallengeIndex: 0 };
    const updatedIndex = (progressData.currentChallengeIndex || 0) + 1;

    transaction.set(progressRef, {
      currentChallengeIndex: updatedIndex,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    return { success: true };
  });
});
