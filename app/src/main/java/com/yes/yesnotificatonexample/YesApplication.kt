package com.yes.yesnotificatonexample

import android.app.Application
import com.yes.yesnotificatonexample.manager.PlumFcmTopicManager
import com.yes.yesnotificatonexample.manager.PlumFcmTopicManager.Companion.FcmTopicType
import com.yes.yesnotificatonexample.manager.PlumNotificationManager

class YesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        PlumNotificationManager.createChannel(this)

        if (!PlumFcmTopicManager.isInitializedTopic(this, FcmTopicType.NEW_CAMPAIGN)) {
            PlumFcmTopicManager.subscribe(this, FcmTopicType.NEW_CAMPAIGN)
        }
    }
}