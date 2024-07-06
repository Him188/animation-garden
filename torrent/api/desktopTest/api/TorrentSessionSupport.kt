package me.him188.ani.app.torrent.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import me.him188.ani.app.torrent.api.handle.TorrentThread
import me.him188.ani.app.torrent.libtorrent4j.DefaultTorrentDownloadSession
import me.him188.ani.app.torrent.libtorrent4j.Libtorrent4jTorrentDownloadSession
import me.him188.ani.app.torrent.libtorrent4j.handle.TorrentAddEvent
import org.junit.jupiter.api.io.TempDir
import java.io.File

@OptIn(TorrentThread::class)
internal abstract class TorrentSessionSupport {
    @TempDir
    lateinit var tempDir: File

    inline fun CoroutineScope.withSession(
        session: DefaultTorrentDownloadSession = Libtorrent4jTorrentDownloadSession(
            torrentName = "test",
            saveDirectory = tempDir,
            onClose = {},
            onDelete = {},
            isDebug = true,
            parentCoroutineContext = SupervisorJob(),
        ),
        block: DefaultTorrentDownloadSession.() -> Unit
    ) {
        coroutineContext.job.invokeOnCompletion {
            session.close()
        }
        session.block()
    }

    inline fun DefaultTorrentDownloadSession.setHandle(
        name: String = "test",
        builderAction: TestAniTorrentHandle.() -> Unit = {},
    ): TestAniTorrentHandle {
        val handle = TestAniTorrentHandle(name)
        listener.onEvent(
            TorrentAddEvent(handle.apply(builderAction)),
        )
        return handle
    }

    inline fun DefaultTorrentDownloadSession.setHandle(
        handle: TestAniTorrentHandle,
        builderAction: TestAniTorrentHandle.() -> Unit = {},
    ): TestAniTorrentHandle {
        listener.onEvent(
            TorrentAddEvent(handle.apply(builderAction)),
        )
        return handle
    }
}