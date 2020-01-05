package com.yes.yesnotificatonexample.manager

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.yes.yesnotificatonexample.R

/**
 * Notification 알림 메시지 및 설정 관련 기능 클래스
 */
class YesNotificationManager {
    companion object {
        enum class ChannelType(val id: String, val title: String, val description: String ) {
            NEW_MESSAGE("new_message", "새 메시지 알림", "새로운 메시지 알림 수신 여부를 선택합니다"),
        }

        /**
         * 앱 내에서 사용할 모든 채널을 생성한다.
         * @param context 채널을 생성할 때 필요한 context
         */
        @JvmStatic
        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelNewMessage = NotificationChannel(Companion.ChannelType.NEW_MESSAGE.id, Companion.ChannelType.NEW_MESSAGE.title, NotificationManager.IMPORTANCE_DEFAULT)
                channelNewMessage.description = Companion.ChannelType.NEW_MESSAGE.description
                channelNewMessage.lightColor = Color.BLUE
                channelNewMessage.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                NotificationManagerCompat.from(context).createNotificationChannel(channelNewMessage)
            }
        }

        /**
         * 채널을 삭제한다
         * @param context 채널을 삭제할 때 필요한 context
         * @param channelType 삭제하고자 하는 채널의 타입
         */
        @JvmStatic
        fun deleteChannel(context: Context, channelType: ChannelType) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManagerCompat.from(context).deleteNotificationChannel(channelType.id)
            }
        }

        /**
         * 특정 채널의 알림이 허용 중인지 확인
         * Oreo(Api 26) 버전 미만에서는 채널이 지원되지 않으므로 전체 알림 허용 여부를 리턴함
         * @param context NotificationMangerCompat 접근 시 사용하는 context
         * @param channelType 알림 허용 여부를 확인할 채널의 타입
         */
        @JvmStatic
        fun isEnabledNotificationChannel(context: Context, channelType: ChannelType) : Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {    // 전체 알림 설정 우선 확인
                    NotificationManagerCompat.from(context).getNotificationChannel(channelType.id)?.let { channel ->
                        return channel.importance != NotificationManagerCompat.IMPORTANCE_NONE  // 채널 알림 설정 확인
                    } ?: return false
                } else {
                    return false
                }
            } else {    // Oreo 버전 미만의 경우 채널을 지원하지 않으므로 전체 알림 설정만 확인하면 됨
                return NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }

        /**
         * Notification 알림 메시지를 생성해 특정 채널로 알림을 전송한다
         * @param context 알림 전송 시 필요한 context
         * @param notificationId 알림 메시지 고유 id (추후 알림 메시지를 업데이트할 때 사용함)
         * @param channelType 알림 메시지를 전송할 채널 타입
         * @param title 알림 메시지의 타이틀
         * @param body 알림 메시지의 바디
         */
        @JvmStatic
        fun sendNotification(context: Context, notificationId: Int, channelType: ChannelType, title: String, body: String) {
            val builder = NotificationCompat.Builder(context, channelType.id)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(getSmallIcon())
                .setAutoCancel(true)
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }

        /**
         * 알림 메시지의 아이콘 리소스를 가져온다
         * @return icon resource id
         */
        private fun getSmallIcon(): Int {
            return android.R.drawable.sym_def_app_icon
        }

        /**
         * 앱 설정 - 알림 페이지를 연다
         * @param context 앱 설정 - 알림 페이지 intent를 열 때 사용하는 context
         * */
        @JvmStatic
        fun openNotificationsSettings(context: Context) {
            val intent = Intent()
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> intent.setOpenSettingsForApiLarger25(context)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> intent.setOpenSettingsForApiBetween21And25(context)
                else -> intent.setOpenSettingsForApiLess21(context)
            }
            context.startActivity(intent)
        }

        /**
         * Oreo version (Api26) 이상인 경우 앱 설정 - 알림 페이지 오픈
         * @param context 앱 설정 - 알림 페이지 intent를 열 때 사용하는 context
         */
        @RequiresApi(Build.VERSION_CODES.O)
        private fun Intent.setOpenSettingsForApiLarger25(context: Context){
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }

        /**
         * Api21 ~ 25인 경우 앱 설정 - 알림 페이지 오픈
         * 공식적으로 APP_NOTIFICATION_SETTING action은 Api26부터 지원하지만, Api21~25에서도 정상적으로 작동함
         * @param context 앱 설정 - 알림 페이지 intent를 열 때 사용하는 context
         */
        private fun Intent.setOpenSettingsForApiBetween21And25(context: Context){
            action = "android.settings.APP_NOTIFICATION_SETTINGS"
            putExtra("app_package", context.packageName)
            putExtra("app_uid", context.applicationInfo.uid)
        }

        /**
         * Api21 미만인 경우 앱 설정 페이지까지만 진입 가능하므로 앱 설정 페이지 오픈
         * @param context 앱 설정 - 알림 페이지 intent를 열 때 사용하는 context
         */
        private fun Intent.setOpenSettingsForApiLess21(context: Context) {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            addCategory(Intent.CATEGORY_DEFAULT)
            data = Uri.parse("package:$context.packageName")
        }

        /**
         * 특정 채널 상세 설정 페이지 오픈 (api 26 이상 사용 가능)
         * @param context intent
         */
        @JvmStatic
        fun openNotificationChannelSetting(context: Context, channelType: ChannelType) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent()
                intent.action = Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelType.id)
                context.startActivity(intent)
            }
        }

        /**
         * 앱 알림 설정 유도 다이얼로그 보여주기
         * @param activity AlertDialog를 띄울 때 사용하는 activity
         */
        @JvmStatic
        fun showAppSettingDialog(activity: Activity) {
            if (activity.isFinishing) {
                return
            }

            val builder = AlertDialog.Builder(activity)
            builder.setMessage("앱 알림 설정이 꺼져있습니다. 설정이 필요합니다.")
            builder.setPositiveButton(R.string.dialog_positive_check) { _, _ ->
                openNotificationsSettings(
                    activity
                )
            }
                .create()
                .show()
        }
    }
}