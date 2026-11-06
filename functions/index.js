/**
 * The Mzantsi Table - push notifications (Firebase Cloud Messaging).
 *
 * When the app writes a new document to users/{uid}/notifications/{id}
 * (someone rated, cheered or commented on an attempt, or reviewed a recipe),
 * this function sends it as a push notification to every phone that user is signed in on.
 * Because it runs on Google's servers, the notification arrives even when the app is closed.
 *
 * Deploy:  cd functions && npm install && cd .. && firebase deploy --only functions
 * (Cloud Functions need the Firebase "Blaze" pay-as-you-go plan. The free monthly allowance
 *  is far more than a student project uses.)
 */
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

exports.sendPushNotification = onDocumentCreated(
  { document: "users/{uid}/notifications/{notificationId}", region: "us-central1" },
  async (event) => {
    const snap = event.data;
    if (!snap) return;
    const n = snap.data();
    const uid = event.params.uid;

    const userRef = getFirestore().collection("users").doc(uid);
    const user = (await userRef.get()).data() || {};

    // The user switched notifications off in their Profile.
    if (user.NotificationsEnabled === false) {
      logger.info(`Notifications are off for ${uid}`);
      return;
    }

    const tokens = Array.isArray(user.FcmTokens) ? user.FcmTokens.filter(Boolean) : [];
    if (tokens.length === 0) {
      logger.info(`No phones registered for ${uid}`);
      return;
    }

    // A "data" message: the app's MzantsiMessagingService builds the notification itself,
    // so it never shows twice and tapping it opens the right screen. FCM data values must be text.
    const message = {
      tokens,
      data: {
        id: String(event.params.notificationId),
        toUid: String(uid),
        type: String(n.type || ""),
        title: String(n.title || "The Mzantsi Table"),
        body: String(n.body || ""),
        route: String(n.route || ""),
      },
      android: { priority: "high" },
    };

    const result = await getMessaging().sendEachForMulticast(message);
    logger.info(`Sent to ${result.successCount}/${tokens.length} phone(s) of ${uid}`);

    // Remove tokens of phones that uninstalled the app or signed out.
    const dead = [];
    result.responses.forEach((r, i) => {
      const code = r.error && r.error.code;
      if (
        code === "messaging/registration-token-not-registered" ||
        code === "messaging/invalid-registration-token" ||
        code === "messaging/invalid-argument"
      ) {
        dead.push(tokens[i]);
      }
    });
    if (dead.length > 0) {
      await userRef.update({ FcmTokens: FieldValue.arrayRemove(...dead) });
      logger.info(`Removed ${dead.length} old token(s) for ${uid}`);
    }
  }
);
