package me.him188.ani.app.torrent.anitorrent.session

import me.him188.ani.app.torrent.anitorrent.HandleId
import me.him188.ani.app.torrent.anitorrent.binding.peer_info_t
import me.him188.ani.app.torrent.api.peer.PeerInfo
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes

class SwigPeerInfo(
    private val native: peer_info_t,
) : PeerInfo {
    override val handle: HandleId = native.torrent_handle_id
    override val id: String get() = native.peer_id
    override val client: String get() = native.client
    override val ipAddr: String get() = native.ip_addr
    override val ipPort: Int get() = native.ip_port
    override val progress: Float = native.progress
    override val totalDownload: FileSize = native.total_download.bytes
    override val totalUpload: FileSize = native.total_upload.bytes
    override val flags: Long = native.flags
}