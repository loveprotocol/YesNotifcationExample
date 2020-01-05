package com.yes.yesnotificatonexample

import android.app.Application
import com.yes.yesnotificatonexample.manager.YesFcmTopicManager
import com.yes.yesnotificatonexample.manager.YesFcmTopicManager.Companion.FcmTopicType
import com.yes.yesnotificatonexample.manager.YesNotificationManager

class YesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        YesNotificationManager.createChannel(this)

        if (!YesFcmTopicManager.isInitializedTopic(this, FcmTopicType.TOPIC_ONE)) {
            YesFcmTopicManager.subscribe(this, FcmTopicType.TOPIC_ONE)
        }
    }
}