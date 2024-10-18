/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoutes {
    @Serializable
    data object Welcome : NavRoutes()

    @Serializable
    data class Main(
        val initialPage: MainScenePage,
        val requestSearchFocus: Boolean = false,
    ) : NavRoutes()

    @Serializable
    data object BangumiOAuth : NavRoutes()

    @Serializable
    data object BangumiTokenAuth : NavRoutes()

    @Serializable
    data class Settings(
        val tab: SettingsTab,
    ) : NavRoutes()

    @Serializable
    data class SubjectDetail(
        val subjectId: Int,
    ) : NavRoutes()

    @Serializable
    data class SubjectCaches(
        val subjectId: Int,
    ) : NavRoutes()

    @Serializable
    data class EpisodeDetail(
        val subjectId: Int,
        val episodeId: Int,
    ) : NavRoutes()

    @Serializable
    data class EditMediaSource(
        val factoryId: String,
        val mediaSourceInstanceId: String,
    ) : NavRoutes()

    @Serializable
    data object TorrentPeerSettings : NavRoutes()

    @Serializable
    data object Caches : NavRoutes()

    @Serializable
    data class CacheDetail(
        val cacheId: String,
    ) : NavRoutes()

}

@Serializable
enum class MainScenePage {
    Exploration,
    Collection,
    CacheManagement,
    Search, ;

    companion object {
        @Stable
        val visibleEntries by lazy(LazyThreadSafetyMode.PUBLICATION) { entries - Search }

        @Stable
        val NavType by lazy(LazyThreadSafetyMode.PUBLICATION) {
            EnumNavType(kotlin.enums.enumEntries<MainScenePage>())
        }
    }
}

@Immutable
enum class SettingsTab {
    APPEARANCE,
    UPDATE,

    PLAYER,
    MEDIA_SUBSCRIPTION,
    MEDIA_SOURCE,
    MEDIA_SELECTOR,
    DANMAKU,

    PROXY,
    BT,
    CACHE,
    STORAGE,

    ABOUT,
    DEBUG,
    ;

    companion object {
        val DEFAULT = APPEARANCE

        @Stable
        val NavType by lazy(LazyThreadSafetyMode.PUBLICATION) {
            EnumNavType(kotlin.enums.enumEntries<SettingsTab>())
        }
    }
}

