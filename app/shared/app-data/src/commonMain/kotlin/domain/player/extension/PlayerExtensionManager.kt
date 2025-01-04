/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.player.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.him188.ani.app.domain.episode.EpisodeFetchPlayState
import me.him188.ani.app.domain.episode.MediaFetchSelectBundle
import me.him188.ani.app.domain.episode.player
import org.koin.core.Koin
import org.openani.mediamp.MediampPlayer
import kotlin.coroutines.cancellation.CancellationException

class PlayerExtensionManager(
    val extensions: List<PlayerExtension>,
) {
    inline fun call(block: (PlayerExtension) -> Unit) {
        extensions.forEach {
            try {
                block(it)
            } catch (e: Throwable) {
                if (e is CancellationException) {
                    throw e
                }

                throw ExtensionException("Error calling extension ${it.name}, see cause", e)
            }
        }
    }
}

fun PlayerExtensionManager(
    extensions: List<EpisodePlayerExtensionFactory<*>>,
    state: EpisodeFetchPlayState,
    koin: Koin,
): PlayerExtensionManager {
    val context = object : PlayerExtensionContext {
        override val subjectId: Int
            get() = state.subjectId
        override val episodeIdFlow: StateFlow<Int>
            get() = state.episodeIdFlow
        override val player: MediampPlayer
            get() = state.player
        override val fetchSelectFlow: Flow<MediaFetchSelectBundle?>
            get() = state.fetchSelectFlow

        override suspend fun switchEpisode(newEpisodeId: Int) {
            if (episodeIdFlow.value == newEpisodeId) {
                error("Cannot switch to the same episode: $newEpisodeId")
            }

            state.switchEpisode(newEpisodeId)
        }
    }

    return PlayerExtensionManager(extensions.map { it.create(context, koin) })
}

class ExtensionException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
