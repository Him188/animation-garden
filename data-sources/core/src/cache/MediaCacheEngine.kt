package me.him188.ani.datasources.core.cache

import kotlinx.coroutines.flow.Flow
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import kotlin.coroutines.CoroutineContext

/**
 * 资源缓存引擎, 负责 [MediaCache] 的创建.
 *
 * [MediaCacheEngine] 的作用可以简单理解为是使用 libtorrent4j (内嵌) 还是 qBittorrent (本机局域网) 来下载种子.
 * 虽然目前不支持缓存 WEB 视频, 但未来增加一个 [MediaCacheEngine] 实现即可支持.
 *
 * ### 元数据管理
 *
 * [Media] 和 [MediaCacheMetadata] 是一个 [MediaCache] 必要的数据.
 * [Media] 表示该 [MediaCache] 缓存的是哪个视频和这个视频自身的信息, [MediaCacheMetadata] 则包含该视频属于哪个番剧的哪一集等来源信息.
 *
 * [MediaCacheEngine] 不考虑缓存元数据的存储方式, 即不考虑目前有多少缓存.
 * 它只根据 [Media] 与 [MediaCacheMetadata] 来启动或恢复下载任务并返回一个 [MediaCache] 示例.
 * [MediaCacheStorage] 负责持久化 [Media] 与 [MediaCacheMetadata], 然后调用 [MediaCacheEngine.restore] 恢复下载任务.
 *
 * ### 下载数据存储位置
 *
 * [MediaCacheEngine] 决定种子数据的实际存储位置, 但该目录不一定包含视频文件. TODO
 */
interface MediaCacheEngine {
    val isEnabled: Flow<Boolean> // todo 这有点奇怪

    /**
     * 此引擎的总体传输统计
     */
    val stats: MediaStats

    /**
     * 是否支持给定缓存给定的 [Media].
     * 当且仅当返回 `true` 时, [restore] 和 [createCache] 才可以被调用.
     */
    fun supports(media: Media): Boolean

    /**
     * 使用给定的 [Media] 信息 [origin] 以及缓存元数据 [metadata], 恢复一个 [MediaCache]
     * Restores a cache that was created by [createCache].
     *
     * @param metadata from `MediaCache.media.cacheMetadata` from [createCache]
     *
     * Returns `null` if the cache was deleted or invalid.
     * @throws UnsupportedOperationException if [supports] returned false
     */
    suspend fun restore(
        origin: Media,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache?

    /**
     * 创建一个新的返回
     * @throws UnsupportedOperationException if [supports] returned false
     */
    suspend fun createCache(
        origin: Media,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache

    /**
     * 删除所有未在 [all] 中找到对应 [MediaCache] 的文件. 这通常包括在线播放的视频. 不会包括通过缓存功能创建的.
     */
    suspend fun deleteUnusedCaches(all: List<MediaCache>)
}