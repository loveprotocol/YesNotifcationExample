package com.yes.yesnotificatonexample

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import com.yes.yesnotificatonexample.manager.PlumNotificationManager.Companion.ChannelType
import com.yes.yesnotificatonexample.manager.PlumFcmTopicManager
import com.yes.yesnotificatonexample.manager.PlumFcmTopicManager.Companion.FcmTopicType
import com.yes.yesnotificatonexample.manager.PlumNotificationManager

class MainActivity : AppCompatActivity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /* 포인트 조건값 체크, 라디오 버튼 설정*/
        val abovePoint = SharedPreferencesHelper.get(this, "abovePoint", 50)
        if (abovePoint is Int) {
            setRadioChecked(abovePoint)
        } else {
            saveSPPointCondition(radioGroup_point_range.checkedRadioButtonId)
        }

        radioGroup_point_range.setOnCheckedChangeListener { group, checkedId ->
            saveSPPointCondition(checkedId)
        }

        /* 알림 설정값 불러오기 */
        val isSubscribed = PlumFcmTopicManager.isSubscribedTopic(this, PlumFcmTopicManager.Companion.FcmTopicType.NEW_CAMPAIGN)
        setSwitchButtonChecked(isSubscribed)

        val range = getArrayWithSteps(10, 1000, 10)

        numberPickerA.minValue = 0
        numberPickerA.maxValue = range.lastIndex
        numberPickerA.displayedValues = range

        btn_plumboard_news.setOnClickListener(this)
        btn_new_campaign.setOnClickListener(this)
        btn_setting_plumboard_news.setOnClickListener(this)
        btn_setting.setOnClickListener(this)
        btn_delete_btn_new_campaign.setOnClickListener(this)
        switch_new_campaign_subscribe.setOnCheckedChangeListener(this)


        // 구독 초기화
        if (!PlumFcmTopicManager.isInitializedTopic(this, FcmTopicType.NEW_CAMPAIGN)) {
            PlumFcmTopicManager.subscribe(this, FcmTopicType.NEW_CAMPAIGN)
        }

        // 앱 알림 Android 설정이 꺼져있는 경우, 알림 활성화 요청 다이얼로그 팝업
        if (!PlumNotificationManager.isNotificationChannelEnabled(this, ChannelType.NEW_CAMPAIGN)) {
            PlumNotificationManager.showAppSettingDialog(this)
        }
    }

    private fun getArrayWithSteps(iMinValue: Int, iMaxValue: Int, iStep: Int): Array<String> {
        val iStepsArray = (iMaxValue - iMinValue) / iStep + 1 //get the lenght array that will return

        return Array(iStepsArray) { index: Int ->
            (iMinValue + index * iStep).toString() }
    }

    private fun setSwitchButtonChecked(checked: Boolean) {
        switch_new_campaign_subscribe.isChecked = checked
    }

    private fun setRadioChecked(abovePoint: Int) {
        when (abovePoint) {
            50 -> radioGroup_point_range.check(radioButton.id)
            100 -> radioGroup_point_range.check(radioButton2.id)
            200 -> radioGroup_point_range.check(radioButton3.id)
            1000 -> radioGroup_point_range.check(radioButton4.id)
        }
    }

    private fun saveSPPointCondition(checkedId: Int) {
        val abovePoint = when (checkedId) {
            radioButton.id ->50
            radioButton2.id -> 100
            radioButton3.id -> 200
            radioButton4.id -> 1000
            else -> 50
        }

        SharedPreferencesHelper.put(this, "abovePoint", abovePoint)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_new_campaign -> PlumNotificationManager.sendNotification(
                this,
                2,
                PlumNotificationManager.Companion.ChannelType.NEW_CAMPAIGN,
                "New Campaign title",
                "New Campaign body"
            )
            R.id.btn_setting -> PlumNotificationManager.openNotificationsSettings(this)
            R.id.btn_delete_btn_new_campaign -> {
                PlumNotificationManager.deleteChannel(this, ChannelType.NEW_CAMPAIGN)
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView == switch_new_campaign_subscribe) {
            when (isChecked) {
                true -> {
                    if (PlumNotificationManager.isNotificationChannelEnabled(this, ChannelType.NEW_CAMPAIGN)) {
                        PlumFcmTopicManager.subscribe(
                            this, FcmTopicType.NEW_CAMPAIGN
                        ).observe(this, Observer { resources ->
                            if (!resources.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "새로운 캠페인 알림 설정에 실패했습니다\n\n" + resources.error().message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                switch_new_campaign_subscribe.setOnCheckedChangeListener(null)
                                switch_new_campaign_subscribe.isChecked = false
                                switch_new_campaign_subscribe.setOnCheckedChangeListener(this)
                            }
                        })
                    } else {
                        PlumNotificationManager.showAppSettingDialog(this)
                    }
                }
                false -> PlumFcmTopicManager.unSubscribe(this, FcmTopicType.NEW_CAMPAIGN
                ).observe(this, Observer {resources ->
                    if (!resources.isSuccessful) {
                        Toast.makeText(this, "잠시 후 재시도 부탁 드립니다\n\n" + resources.error().message, Toast.LENGTH_SHORT).show()
                        switch_new_campaign_subscribe.setOnCheckedChangeListener(null)
                        switch_new_campaign_subscribe.isChecked =  true
                        switch_new_campaign_subscribe.setOnCheckedChangeListener(this)
                    }
                })
            }
        }
    }
}

