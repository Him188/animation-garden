/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.torrent.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.domain.torrent.IDisposableHandle
import me.him188.ani.app.domain.torrent.IRemotePieceList
import me.him188.ani.app.domain.torrent.IRemoteTorrentFileEntry
import me.him188.ani.app.domain.torrent.IRemoteTorrentFileHandle
import me.him188.ani.app.domain.torrent.IRemoteTorrentInput
import me.him188.ani.app.domain.torrent.ITorrentFileEntryStatsFlow
import me.him188.ani.app.domain.torrent.parcel.PTorrentFileEntryStats
import me.him188.ani.app.torrent.api.files.TorrentFileEntry
import me.him188.ani.utils.coroutines.childScope
import me.him188.ani.utils.io.absolutePath
import kotlin.coroutines.CoroutineContext

class TorrentFileEntryProxy(
    private val delegate: TorrentFileEntry,
    context: CoroutineContext
) : IRemoteTorrentFileEntry.Stub(), CoroutineScope by context.childScope() {
    override fun getFileStats(flow: ITorrentFileEntryStatsFlow?): IDisposableHandle {
        val job = launch {
            delegate.fileStats.collect {
                flow?.onEmit(PTorrentFileEntryStats(it.downloadedBytes, it.downloadProgress))
            }
        }

        return DisposableHandleProxy { job.cancel() }
    }

    override fun getLength(): Long {
        return delegate.length
    }

    override fun getPathInTorrent(): String {
        return delegate.pathInTorrent
    }

    override fun getPieces(): IRemotePieceList {
        TODO("Not yet implemented")
    }

    override fun getSupportsStreaming(): Boolean {
        return delegate.supportsStreaming
    }

    override fun createHandle(): IRemoteTorrentFileHandle {
        TODO("Not yet implemented")
    }

    override fun resolveFile(): String {
        return runBlocking { delegate.resolveFile().absolutePath }
    }

    override fun resolveFileMaybeEmptyOrNull(): String? {
        return delegate.resolveFileMaybeEmptyOrNull()?.absolutePath
    }

    override fun createInput(): IRemoteTorrentInput {
        TODO("Not yet implemented")
    }
}