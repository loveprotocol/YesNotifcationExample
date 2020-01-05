package com.yes.yesnotificatonexample.manager

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessaging
import com.yes.yesnotificatonexample.manager.YesNotificationManager.Companion.ChannelType
import com.yes.yesnotificatonexample.Resource
import com.yes.yesnotificatonexample.SharedPreferencesHelper

/**
 * FCM Topic 관련 기능을 모아놓은 클래스
 */
class YesFcmTopicManager {
    companion object {
        /**
         * Fcm Topic Type 정의
         */
        enum class FcmTopicType(val spKeyName: String /* SharedPreference Key name */,
                                            val channelType: ChannelType) /* Topic이 속하는 Channel Type*/
        {
            NONE("subscribe_topic_none", ChannelType.NEW_MESSAGE),
            TOPIC_ONE("subscribe_topic_one", ChannelType.NEW_MESSAGE),
        }

        /**
         * Topic 초기화가 되었는지 확인한다.
         * @param context sharedPreference 접근을 위한 context
         * @param topicType 초기화 되었는지 확인하고자 하는 Topic
         * @return Topic 초기화 여부
         */
        @JvmStatic
        fun isInitializedTopic(context: Context, topicType: FcmTopicType) : Boolean {
            return SharedPreferencesHelper.get(context, topicType.spKeyName, false) != null
        }

        /**
         * Topic 구독 상태인지 확인한다
         * @param context sharedPreference 접근을 위한 context
         * @param topicType 구독 여부를 확인하고자 하는 Topic
         * @return Topic 구독 여부
         */
        @JvmStatic
        fun isSubscribedTopic(context: Context, topicType: FcmTopicType) : Boolean {
            val isSubscribed =
                SharedPreferencesHelper.get(context, topicType.spKeyName, false)
            return if (isSubscribed is Boolean) {
                isSubscribed
            } else {
                false
            }
        }

        /**
         * Topic 구독
         * @param context SharedPreference 접근 시 사용하는 context
         * @param topicType 구독하고자 하는 Topic 타입
         * @return 구독 성공 여부를 liveData로 리턴
         */
        @JvmStatic
        fun subscribe(context: Context, topicType: FcmTopicType) : LiveData<Resource<Boolean>> {
            val isSubscribe = MutableLiveData<Resource<Boolean>>()

            FirebaseMessaging.getInstance().subscribeToTopic(topicType.name)
                .addOnSuccessListener {
                    // 구독 여부를 SharedPreference에 저장
                    SharedPreferencesHelper.put(context, topicType.spKeyName, true)
                    isSubscribe.value = Resource(true)
                }
                .addOnFailureListener {exception ->
                    isSubscribe.value = Resource(exception)
                }

            return isSubscribe
        }

        /**
         * Topic 구독 취소
         * @param context SharedPreference 접근 시 사용하는 context
         * @param topicType 구독 취소하고자 하는 Topic 타입
         * @return 구독 취소 성공 여부를 liveData로 리턴
         */
        @JvmStatic
        fun unSubscribe(context: Context, topicType: FcmTopicType) : LiveData<Resource<Boolean>> {
            val isUnsubscribe = MutableLiveData<Resource<Boolean>>()

            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicType.name)
                .addOnSuccessListener {
                    SharedPreferencesHelper.put(context, topicType.spKeyName, false)
                    isUnsubscribe.value = Resource(false)
                }
                .addOnFailureListener {exception ->
                    isUnsubscribe.value = Resource(exception)
                }

            return isUnsubscribe
        }
    }
}