package com.yes.yesnotificatonexample.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.yes.yesnotificatonexample.manager.YesFcmTopicManager.Companion.FcmTopicType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yes.yesnotificatonexample.MainActivity
import com.yes.yesnotificatonexample.MyWorker
import com.yes.yesnotificatonexample.R
import com.yes.yesnotificatonexample.SharedPreferencesHelper
import com.yes.yesnotificatonexample.manager.YesNotificationManager

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Kotlin sample functional, you must remove the following from the Java messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
class YesMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]
        remoteMessage.from?.let { from ->
            val topicType = sliceTopicString(from)?.let {topicString ->
                    FcmTopicType.valueOf(topicString)
                } ?: FcmTopicType.NONE

            if (topicType != FcmTopicType.NONE) {
                remoteMessage.data.isNotEmpty().let {
                    if (/* Check if data needs to be processed by long running job */ false) {
                        // For long-running tasks (10 seconds or more) use WorkManager.
                        scheduleJob()
                    } else {
                        // Handle message within 10 seconds
                        handleNow(topicType, remoteMessage.data)
                    }
                }
            }
        }
    }

    private fun sliceTopicString(remoteMessageFrom: String): String? {
        val splitList = remoteMessageFrom.split("/")
        return when (splitList.isEmpty()) {
            true -> null
            false -> splitList.last()
        }
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        WorkManager.getInstance().beginWith(work).enqueue()
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow(topicType: FcmTopicType, data:Map<String, String>) {
        when (topicType) {
            FcmTopicType.TOPIC_ONE -> processTopicOne(data)
            else -> {}
        }
    }

    private fun processTopicOne(data: Map<String, String>) {
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }
}
