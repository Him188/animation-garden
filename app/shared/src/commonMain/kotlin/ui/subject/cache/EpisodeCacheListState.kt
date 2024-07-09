package me.him188.ani.app.ui.subject.cache

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.source.media.cache.MediaCacheEngine
import me.him188.ani.app.data.source.media.cache.MediaCacheStorage
import me.him188.ani.app.data.source.media.cache.requester.CacheRequestStage
import me.him188.ani.app.data.source.media.cache.requester.EpisodeCacheRequester
import me.him188.ani.app.data.source.media.cache.requester.trySelectSingle
import me.him188.ani.app.data.source.media.selector.MediaSelector
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.datasources.api.Media
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

@Stable
interface EpisodeCacheListState {
    /**
     * 该条目的所有剧集的缓存状态列表. 初始值为 `emptyList()`, 表示正在加载.
     */
    val episodes: List<EpisodeCacheState>

    /**
     * 是否有任何一个剧集正在进行缓存操作. 当剧集列表仍然在加载时, 该值总是为 `false`.
     */
    val anyEpisodeActionRunning: Boolean


    /**
     * 当前正在展示 [currentSelectMediaTask] 或 [currentSelectMediaTask] 的 [EpisodeCacheState]
     */
    val currentEpisode: EpisodeCacheState?

    /**
     * 当前需要展示给用户选择的 [MediaSelector]
     */
    val currentSelectMediaTask: SelectMediaTask?
    fun selectMedia(media: Media)
    fun cancelMediaSelector(task: SelectMediaTask)


    /**
     * 当前需要展示给用户选择的存储位置列表
     */
    val currentSelectStorageTask: SelectStorageTask?
    fun selectStorage(storage: MediaCacheStorage)
    fun cancelStorageSelector(task: SelectStorageTask)

    fun cancelRequest()


    /**
     * 开始请求缓存一个剧集.
     *
     * 将会加载条目和剧集元数据, 尝试自动选择缓存. 在无法自动选择时, 将会展示 [MediaSelector].
     * @param autoSelectCached [CacheRequestStage.SelectMedia.tryAutoSelectByCachedSeason]
     */
    fun requestCache(episode: EpisodeCacheState, autoSelectCached: Boolean)

    /**
     * 删除一个剧集的现有缓存.
     */
    fun deleteCache(episode: EpisodeCacheState)
}

/**
 * 单个条目的缓存管理页面的状态
 *
 * ## 流程
 *
 * 1. 用户点击一个剧集的下载按钮, 调用 [requestCache]
 * 2. [requestCache] 调用 [onRequestCache] 发起请求 ([EpisodeCacheRequester]). [onRequestCache] 将会自动尝试选择目标 media 和 storage.
 * 3. 如果没有自动选择 media, [currentSelectMediaTask] 会变为非 `null`, UI 弹出 media selector, 待用户选择后调用 [selectMedia]
 * 4. 如果没有自动选择 storage, [currentSelectStorageTask] 会变为非 `null`, UI 对应弹出选择, 待用户选择后调用 [selectStorage]
 * 5. 当 media 和 storage 都选择完成, [onRequestCacheComplete] 被调用, 开始缓存
 *
 * @param onRequestCache 当需要开始为一个剧集创建缓存时调用. 用于从用户侧收集目标 [Media] 和 [MediaCacheStorage].
 * 通常使用 [EpisodeCacheRequester] 发起 [EpisodeCacheRequester.request], 然后尝试自动选择缓存.
 *
 * @param onRequestCacheComplete 当成功收集到用户选择的 [Media] 和 [MediaCacheStorage] 时调用.
 * 通常操作 [MediaCacheEngine] 开始缓存.
 *
 * @param onDeleteCache 当需要删除一个剧集的现有缓存时调用.
 */ // See 连续缓存季度全集剧集 #376
@Stable
class EpisodeCacheListStateImpl(
    episodesLazy: Flow<List<EpisodeCacheState>>,
    private val onRequestCache: suspend (episode: EpisodeCacheState, autoSelectByCached: Boolean) -> CacheRequestStage?,
    private val onRequestCacheComplete: suspend (episode: EpisodeCacheTargetInfo) -> Unit,
    private val onDeleteCache: suspend (episode: EpisodeCacheState) -> Unit,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext), EpisodeCacheListState {
    override val episodes: List<EpisodeCacheState> by episodesLazy.produceState(emptyList())

    override val anyEpisodeActionRunning: Boolean by derivedStateOf {
        episodes.any { it.actionTasker.isRunning }
    }

    override val currentEpisode: EpisodeCacheState? by derivedStateOf {
        episodes.firstOrNull { it.currentSelectMediaTask != null || it.currentSelectStorageTask != null }
    }
    override val currentSelectMediaTask: SelectMediaTask? by derivedStateOf {
        episodes.firstNotNullOfOrNull { it.currentSelectMediaTask }
    }

    override fun selectMedia(media: Media) {
        val episode = currentEpisode ?: return
        episode.actionTasker.launch {
            (episode.cacheRequester.stage.value as? CacheRequestStage.SelectMedia)?.select(media)
                ?.trySelectSingle()
                ?.let { done ->
                    callComplete(episode, done)
                }
        }
    }

    override fun cancelMediaSelector(task: SelectMediaTask) {
        val episode = task.episode
        episode.actionTasker.launch {
            (episode.cacheRequester.stage.value as? CacheRequestStage.SelectMedia)?.cancel()
        }
    }

    override val currentSelectStorageTask: SelectStorageTask? by derivedStateOf {
        episodes.firstNotNullOfOrNull { it.currentSelectStorageTask }
    }

    override fun selectStorage(storage: MediaCacheStorage) {
        val episode = currentEpisode ?: return
        episode.actionTasker.launch {
            (episode.cacheRequester.stage.value as? CacheRequestStage.SelectStorage)
                ?.select(storage)
                ?.let { done ->
                    callComplete(episode, done)
                }
        }
    }

    override fun cancelStorageSelector(task: SelectStorageTask) {
        val episode = task.episode
        episode.actionTasker.launch {
            (episode.cacheRequester.stage.value as? CacheRequestStage.SelectStorage)
                ?.cancel()
                ?.mediaSelector?.unselect() // 取消选中曾经选中的 Media, 否则那个 Media 会一直显示进度条 
        }
    }

    override fun cancelRequest() {
        val episode = currentEpisode ?: return
        episode.actionTasker.launch {
            episode.cacheRequester.cancelRequest()
        }
    }

    override fun requestCache(episode: EpisodeCacheState, autoSelectCached: Boolean) {
        // 当已经有任务时, 忽略请求
        if (episode.actionTasker.isRunning) return
        episode.actionTasker.launch {
            // TODO: 处理错误 
            onRequestCache(episode, autoSelectCached)?.let {
                episode.currentSelectStorageTask
                if (it is CacheRequestStage.Done) {
                    callComplete(episode, it)
                }
            }
        }
    }

    private suspend inline fun EpisodeCacheListStateImpl.callComplete(
        episode: EpisodeCacheState,
        done: CacheRequestStage.Done
    ) {
        onRequestCacheComplete(
            EpisodeCacheTargetInfo(
                episode = episode,
                request = done.request,
                media = done.media,
                storage = done.storage,
                metadata = done.metadata,
            ),
        )
    }

    override fun deleteCache(episode: EpisodeCacheState) {
        if (episode.actionTasker.isRunning) return
        episode.actionTasker.launch {
            try {
                onDeleteCache(episode)
            } catch (_: CancellationException) {
                // 用户主动取消
            } catch (e: Exception) {
                // TODO: 处理 requestDelete exception
                throw e
                // errorMessage.value = ErrorMessage.simple("删除缓存失败", e)
            }
        }
    }
}
