package com.washapp.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class WashAppMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // TODO: build a notification from message.data at each service stage transition
        // (Washing -> Drying -> Completed), triggered server-side by a Cloud Function.
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: persist the token to the signed-in user's Firestore document so
        // Cloud Functions can target this device for stage-transition alerts.
    }
}
