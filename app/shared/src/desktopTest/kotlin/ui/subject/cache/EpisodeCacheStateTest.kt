@file:OptIn(ExperimentalStdlibApi::class)

package me.him188.ani.app.ui.subject.cache

import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.source.media.EpisodeCacheStatus
import me.him188.ani.app.data.source.media.cache.TestMediaCacheStorage
import me.him188.ani.app.data.source.media.cache.requester.EpisodeCacheRequest
import me.him188.ani.app.data.source.media.cache.requester.EpisodeCacheRequester
import me.him188.ani.app.data.source.media.cache.requester.trySelectSingle
import me.him188.ani.app.data.source.media.fetch.MediaFetcherConfig
import me.him188.ani.app.data.source.media.fetch.MediaSourceMediaFetcher
import me.him188.ani.app.data.source.media.framework.TestMediaList
import me.him188.ani.app.data.source.media.framework.TestMediaSelector
import me.him188.ani.app.data.source.media.instance.createTestMediaSourceInstance
import me.him188.ani.app.data.source.media.selector.MediaSelector
import me.him188.ani.app.data.source.media.selector.MediaSelectorFactory
import me.him188.ani.app.ui.framework.runComposeStateTest
import me.him188.ani.app.ui.framework.takeSnapshot
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.paging.SinglePagePagedSource
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.TestHttpMediaSource
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.junit.jupiter.api.TestInstance
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * @see EpisodeCacheState
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class EpisodeCacheStateTest {
    private val infoFlow = MutableStateFlow(
        EpisodeCacheInfo(
            sort = EpisodeSort(1),
            ep = EpisodeSort(1),
            title = "第一集的标题",
            watchStatus = UnifiedCollectionType.DOING,
            hasPublished = true,
        ),
    )
    private val cacheStatusFlow: MutableStateFlow<EpisodeCacheStatus> = MutableStateFlow(
        EpisodeCacheStatus.Cached(300.megaBytes),
    )

    private suspend fun TestScope.createEpisodeCacheState() = TestEpisodeCacheState(
        1,
        cacheRequesterLazy = { createTestEpisodeCacheRequester() },
        infoFlow = infoFlow,
        cacheStatusFlow = cacheStatusFlow,
        parentCoroutineContext = currentCoroutineContext(),
    ).apply {
        testScheduler.runCurrent() // start `launch` and `produceState`
    }

    private fun createTestEpisodeCacheRequester(): EpisodeCacheRequester {
        return EpisodeCacheRequester(
            flowOf(
                MediaSourceMediaFetcher(
                    configProvider = { MediaFetcherConfig.Default },
                    mediaSources = listOf(
                        createTestMediaSourceInstance(
                            TestHttpMediaSource(
                                fetch = {
                                    SinglePagePagedSource {
                                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                                    }
                                },
                            ),
                        ),
                    ),
                    flowContext = EmptyCoroutineContext,
                ),
            ),
            mediaSelectorFactory = object : MediaSelectorFactory {
                override fun create(
                    subjectId: Int,
                    mediaList: Flow<List<Media>>,
                    flowCoroutineContext: CoroutineContext
                ): MediaSelector = TestMediaSelector(mediaList)
            },
            storagesLazy = flowOf(listOf(TestMediaCacheStorage())),
        )
    }

    @Test
    fun `initial state`() = runTest {
        val state = createEpisodeCacheState()
        testScheduler.advanceUntilIdle()
        Snapshot.current.dispose()

        assertEquals(infoFlow.value, state.info)
        assertEquals(false, state.isInfoLoading)
        assertEquals(null, state.currentSelectMediaTask)
        assertEquals(null, state.currentSelectStorageTask)
        assertIs<EpisodeCacheStatus.Cached>(state.cacheStatus)
        assertEquals(true, state.canCache)
        assertEquals(false, state.actionTasker.isRunning)
        assertEquals(false, state.showProgressIndicator)

        state.backgroundScope.cancel()
    }

    ///////////////////////////////////////////////////////////////////////////
    // canCache
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `canCache is true for WISH`() = runTest {
        infoFlow.value = infoFlow.value.copy(watchStatus = UnifiedCollectionType.WISH)
        val state = createEpisodeCacheState()
        assertEquals(true, state.canCache)
        state.backgroundScope.cancel()
    }

    @Test
    fun `canCache is true for DOING`() = runTest {
        infoFlow.value = infoFlow.value.copy(watchStatus = UnifiedCollectionType.DOING)
        val state = createEpisodeCacheState()
        assertEquals(true, state.canCache)
        state.backgroundScope.cancel()
    }

    @Test
    fun `canCache is true for ON_HOLD`() = runTest {
        infoFlow.value = infoFlow.value.copy(watchStatus = UnifiedCollectionType.ON_HOLD)
        val state = createEpisodeCacheState()
        assertEquals(true, state.canCache)
        state.backgroundScope.cancel()
    }

    @Test
    fun `canCache is true for NOT_COLLECTED`() = runTest {
        infoFlow.value = infoFlow.value.copy(watchStatus = UnifiedCollectionType.NOT_COLLECTED)
        val state = createEpisodeCacheState()
        assertEquals(true, state.canCache)
        state.backgroundScope.cancel()
    }

    @Test
    fun `canCache is false for DONE`() = runTest {
        infoFlow.value = infoFlow.value.copy(watchStatus = UnifiedCollectionType.DONE)
        val state = createEpisodeCacheState()
        assertEquals(false, state.canCache)
        state.backgroundScope.cancel()
    }

    @Test
    fun `canCache is false for DROPPED`() = runTest {
        infoFlow.value = infoFlow.value.copy(watchStatus = UnifiedCollectionType.DROPPED)
        val state = createEpisodeCacheState()
        assertEquals(false, state.canCache)
        state.backgroundScope.cancel()
    }

    ///////////////////////////////////////////////////////////////////////////
    // showProgressIndicator
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `showProgressIndicator is initially false`() = runComposeStateTest {
        val state = createEpisodeCacheState()
        assertEquals(false, state.showProgressIndicator)
        state.backgroundScope.cancel()
    }

    @Test
    fun `showProgressIndicator is true when actionTasker is running`() = runComposeStateTest {
        val state = createEpisodeCacheState()
        val deferred = CompletableDeferred<Unit>()
        state.actionTasker.launch(start = CoroutineStart.UNDISPATCHED) {
            deferred.await()
        }
        takeSnapshot()

        assertEquals(true, state.showProgressIndicator)
        deferred.complete(Unit)
        state.actionTasker.join()

        takeSnapshot()
        assertEquals(false, state.showProgressIndicator)
        state.backgroundScope.cancel()
    }

    @Test
    fun `showProgressIndicator is true when SelectMedia`() = runComposeStateTest {
        val state = createEpisodeCacheState()
        state.cacheRequester
            .request(EpisodeCacheRequest(SubjectInfo.Empty, EpisodeInfo(1)))

        takeSnapshot()
        assertEquals(true, state.showProgressIndicator)
        state.backgroundScope.cancel()
    }

    @Test
    fun `showProgressIndicator is true when SelectStorage`() = runComposeStateTest {
        val state = createEpisodeCacheState()
        state.cacheRequester
            .request(EpisodeCacheRequest(SubjectInfo.Empty, EpisodeInfo(1)))
            .select(TestMediaList[0])

        takeSnapshot()

        state.backgroundScope.cancel()
    }

    @Test
    fun `showProgressIndicator is false when Done`() = runComposeStateTest {
        val state = createEpisodeCacheState()
        state.cacheRequester
            .request(EpisodeCacheRequest(SubjectInfo.Empty, EpisodeInfo(1)))
            .select(TestMediaList[0])
            .trySelectSingle()

        takeSnapshot()

        assertEquals(false, state.showProgressIndicator)
        state.backgroundScope.cancel()
    }

    ///////////////////////////////////////////////////////////////////////////
    // currentSelectMediaTask
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `currentSelectMediaTask is null when no task`() = runComposeStateTest {
        val state = createEpisodeCacheState()
        assertEquals(null, state.currentSelectMediaTask)
        state.backgroundScope.cancel()
    }

    @Test
    fun `currentSelectMediaTask is not null when SelectMedia`() = runComposeStateTest {
        val state = createEpisodeCacheState()
        state.cacheRequester
            .request(EpisodeCacheRequest(SubjectInfo.Empty, EpisodeInfo(1)))

        takeSnapshot()

        assertEquals(true, state.currentSelectMediaTask != null)
        state.backgroundScope.cancel()
    }

    @Test
    fun `currentSelectMediaTask is not null when SelectStorage`() = runComposeStateTest {
        val state = createEpisodeCacheState()
        state.cacheRequester
            .request(EpisodeCacheRequest(SubjectInfo.Empty, EpisodeInfo(1)))
            .select(TestMediaList[0])

        takeSnapshot()

        assertEquals(true, state.currentSelectMediaTask != null)
        state.backgroundScope.cancel()
    }

    @Test
    fun `currentSelectMediaTask is null when Done`() = runComposeStateTest {
        val state = createEpisodeCacheState()
        state.cacheRequester
            .request(EpisodeCacheRequest(SubjectInfo.Empty, EpisodeInfo(1)))
            .select(TestMediaList[0])
            .trySelectSingle()

        takeSnapshot()

        assertEquals(null, state.currentSelectMediaTask)
        state.backgroundScope.cancel()
    }
}
