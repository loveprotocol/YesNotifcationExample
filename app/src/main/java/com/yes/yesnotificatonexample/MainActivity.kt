package com.yes.yesnotificatonexample

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import com.yes.yesnotificatonexample.manager.YesNotificationManager.Companion.ChannelType
import com.yes.yesnotificatonexample.manager.YesFcmTopicManager
import com.yes.yesnotificatonexample.manager.YesFcmTopicManager.Companion.FcmTopicType
import com.yes.yesnotificatonexample.manager.YesNotificationManager

class MainActivity : AppCompatActivity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* 알림 설정값 불러오기 */
        val isSubscribed = YesFcmTopicManager.isSubscribedTopic(this, YesFcmTopicManager.Companion.FcmTopicType.TOPIC_ONE)
        setSwitchButtonChecked(isSubscribed)

        btn_new_message_noti_send.setOnClickListener(this)
        btn_open_channel_setting.setOnClickListener(this)
        btn_open_app_setting.setOnClickListener(this)
        btn_delete_new_message_channel.setOnClickListener(this)
        switch_new_message_subscribe.setOnCheckedChangeListener(this)

        // 구독 초기화
        if (!YesFcmTopicManager.isInitializedTopic(this, FcmTopicType.TOPIC_ONE)) {
            YesFcmTopicManager.subscribe(this, FcmTopicType.TOPIC_ONE)
        }

        // 앱 알림 Android 설정이 꺼져있는 경우, 알림 활성화 요청 다이얼로그 팝업
        if (!YesNotificationManager.isEnabledNotificationChannel(this, ChannelType.NEW_MESSAGE)) {
            YesNotificationManager.showAppSettingDialog(this)
        }
    }

    private fun setSwitchButtonChecked(checked: Boolean) {
        switch_new_message_subscribe.isChecked = checked
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_new_message_noti_send -> YesNotificationManager.sendNotification(
                this,
                2,
                ChannelType.NEW_MESSAGE,
                "New Message title",
                "New Message body"
            )
            R.id.btn_open_app_setting -> YesNotificationManager.openNotificationsSettings(this)
            R.id.btn_delete_new_message_channel -> {
                YesNotificationManager.deleteChannel(this, ChannelType.NEW_MESSAGE)
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView == switch_new_message_subscribe) {
            when (isChecked) {
                true -> {
                    if (YesNotificationManager.isEnabledNotificationChannel(this, ChannelType.NEW_MESSAGE)) {
                        YesFcmTopicManager.subscribe(
                            this, FcmTopicType.TOPIC_ONE
                        ).observe(this, Observer { resources ->
                            if (!resources.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "새로운 메시지 알림 설정에 실패했습니다\n\n" + resources.error().message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                switch_new_message_subscribe.setOnCheckedChangeListener(null)
                                switch_new_message_subscribe.isChecked = false
                                switch_new_message_subscribe.setOnCheckedChangeListener(this)
                            }
                        })
                    } else {
                        YesNotificationManager.showAppSettingDialog(this)
                    }
                }
                false -> YesFcmTopicManager.unSubscribe(this, FcmTopicType.TOPIC_ONE
                ).observe(this, Observer {resources ->
                    if (!resources.isSuccessful) {
                        Toast.makeText(this, "잠시 후 재시도 부탁 드립니다\n\n" + resources.error().message, Toast.LENGTH_SHORT).show()
                        switch_new_message_subscribe.setOnCheckedChangeListener(null)
                        switch_new_message_subscribe.isChecked =  true
                        switch_new_message_subscribe.setOnCheckedChangeListener(this)
                    }
                })
            }
        }
    }
}

