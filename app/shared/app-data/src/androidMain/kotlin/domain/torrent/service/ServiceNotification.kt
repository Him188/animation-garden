/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.torrent.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.Service.NOTIFICATION_SERVICE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes

class ServiceNotification(
    private val context: Context
) {
    private val notificationService by lazy { context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager }

    private var notificationAppearance = defaultNotificationAppearance
    private var notificationOpenActivityIntent: Intent? = null
    private val stopServiceIntent by lazy {
        PendingIntent.getService(
            context, 0,
            Intent(context, AniTorrentService::class.java).apply { putExtra("stopService", true) },
            PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private val notificationChannel by lazy {
        notificationService.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            ?: NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                notificationAppearance.name,
                NotificationManager.IMPORTANCE_LOW,
            )
                .apply { lockscreenVisibility = Notification.VISIBILITY_PUBLIC }
                .also { notificationService.createNotificationChannel(it) }
    }

    fun parseNotificationStrategyFromIntent(intent: Intent?): NotificationAppearance {
        val name = intent.getStringOrDefault("app_name") {
            defaultNotificationAppearance.name
        }
        val titleIdle = intent.getStringOrDefault("app_service_title_text_idle") {
            defaultNotificationAppearance.titleIdle
        }
        val titleWorking = intent.getStringOrDefault("app_service_title_text_working") {
            defaultNotificationAppearance.titleWorking
        }
        val content = intent.getStringOrDefault("app_service_content_text") {
            defaultNotificationAppearance.content
        }
        val stopActionText = intent.getStringOrDefault("app_service_stop_text") {
            defaultNotificationAppearance.stopActionText
        }
        val icon = (intent?.getIntExtra("app_icon", -1) ?: -1)
            .let { if (it != -1) defaultNotificationAppearance.icon else Icon.createWithResource(context, it) }

        notificationOpenActivityIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("open_activity_intent", Intent::class.java)
        } else {
            intent?.getParcelableExtra<Intent>("open_activity_intent")
        }

        return NotificationAppearance(
            name = name,
            titleIdle = titleIdle,
            titleWorking = titleWorking,
            content = content,
            stopActionText = stopActionText,
            icon = icon,
        )
    }

    /**
     * create notification with initial state idle.
     */
    fun createNotification(service: Service) {
        val currentNotification = notificationService.activeNotifications.find { it.id == NOTIFICATION_ID }
        if (currentNotification != null) return

        val notification = buildNotification(
            notificationAppearance,
            NotificationDisplayStrategy.Idle(0.bytes, 0.bytes),
        )
        service.startForeground(NOTIFICATION_ID, notification)
    }

    /**
     * update notification to current state.
     */
    fun updateNotification(displayStrategy: NotificationDisplayStrategy) {
        notificationService.activeNotifications.find { it.id == NOTIFICATION_ID } ?: return

        val notification = buildNotification(notificationAppearance, displayStrategy)
        notificationService.notify(NOTIFICATION_ID, notification)
    }

    /**
     * build notification and show it initially
     */
    private fun buildNotification(
        appearance: NotificationAppearance,
        displayStrategy: NotificationDisplayStrategy
    ): Notification {
        val openActivityAction = if (notificationOpenActivityIntent == null) null else
            PendingIntent.getActivity(
                context,
                0,
                notificationOpenActivityIntent,
                PendingIntent.FLAG_IMMUTABLE,
            )

        return Notification.Builder(context, notificationChannel.id).apply {
            setContentTitle(
                when (displayStrategy) {
                    is NotificationDisplayStrategy.Idle -> appearance.titleIdle
                    is NotificationDisplayStrategy.Working ->
                        String.format(appearance.titleWorking, displayStrategy.taskCount)
                },
            )
            setContentText(
                String.format(
                    notificationAppearance.content,
                    "${displayStrategy.downloadSpeed}/s",
                    "${displayStrategy.uploadSpeed}/s",
                ),
            )
            setSmallIcon(notificationAppearance.icon)
            setContentIntent(openActivityAction)
            setActions(
                Notification.Action.Builder(
                    notificationAppearance.icon,
                    notificationAppearance.stopActionText,
                    stopServiceIntent,
                ).build(),
            )
            setTicker(notificationAppearance.name)
            setOngoing(displayStrategy is NotificationDisplayStrategy.Working)
            setVisibility(Notification.VISIBILITY_PUBLIC)
        }.build()
    }

    private fun Intent?.getStringOrDefault(extraName: String, default: () -> String): String {
        val result = this?.getIntExtra(extraName, -1) ?: -1
        return if (result == -1) default() else context.getString(result)
    }

    companion object {
        private const val NOTIFICATION_ID = 114
        private const val NOTIFICATION_CHANNEL_ID = "me.him188.ani.app.domain.torrent.service.AniTorrentService"

        private val defaultNotificationAppearance = NotificationAppearance(
            name = "Animeko BT 引擎服务",
            titleIdle = "没有下载任务",
            titleWorking = "正在下载 %1\$d 个资源",
            content = "下载：%1\$s，上传：%2\$s",
            stopActionText = "停止",
            icon = Icon.createWithBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)),
        )
    }
}

class NotificationAppearance(
    val name: String,
    val titleIdle: String,
    val titleWorking: String,
    val content: String,
    val stopActionText: String,
    val icon: Icon,
)

sealed class NotificationDisplayStrategy(val downloadSpeed: FileSize, val uploadSpeed: FileSize) {
    class Idle(
        downloadSpeed: FileSize,
        uploadSpeed: FileSize
    ) : NotificationDisplayStrategy(downloadSpeed, uploadSpeed)

    class Working(
        downloadSpeed: FileSize,
        uploadSpeed: FileSize,
        val taskCount: Int,
    ) : NotificationDisplayStrategy(downloadSpeed, uploadSpeed)
}