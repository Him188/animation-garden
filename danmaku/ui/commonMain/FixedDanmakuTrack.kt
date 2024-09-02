package me.him188.ani.danmaku.ui

import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.Stable

/**
 * FixedDanmakuTrack 中的弹幕在以下情况会移除:
 * - tick 中的逻辑帧检测
 * - [FixedDanmakuTrack.place] 覆盖了正在显示的弹幕
 * - 调用 [DanmakuTrack.clearAll]
 * 移除时必须调用 [onRemoveDanmaku] 避免内存泄露.
 */
@Stable
internal class FixedDanmakuTrack<T : SizeSpecifiedDanmaku>(
    val trackIndex: Int,
    val fromBottom: Boolean,
    private val frameTimeNanosState: LongState,
    private val trackHeight: IntState,
    private val hostHeight: IntState,
    // 顶部或底部弹幕的显示时间，现在还不能自定义
    private val durationMillis: LongState,
    // 某个弹幕需要消失, 必须调用此函数避免内存泄漏.
    private val onRemoveDanmaku: (FixedDanmaku<T>) -> Unit
) : DanmakuTrack<T, FixedDanmaku<T>> {
    private var currentDanmaku: FixedDanmaku<T>? = null
    
    override fun place(danmaku: T, placeTimeNanos: Long): FixedDanmaku<T> {
        check(placeTimeNanos == DanmakuTrack.NOT_PLACED || placeTimeNanos >= 0) {
            "placeTimeNanos must be NOT_PLACED or non-negative, but had $placeTimeNanos"
        }
        val upcomingDanmaku = FixedDanmaku(danmaku, placeTimeNanos, trackIndex, trackHeight, hostHeight, fromBottom)
        currentDanmaku?.let(onRemoveDanmaku)
        currentDanmaku = upcomingDanmaku
        return upcomingDanmaku
    }

    override fun canPlace(
        danmaku: T,
        placeTimeNanos: Long
    ): Boolean {
        check(placeTimeNanos == DanmakuTrack.NOT_PLACED || placeTimeNanos >= 0) {
            "placeTimeNanos must be NOT_PLACED or non-negative, but had $placeTimeNanos"
        }
        // 当前如果有正在显示的弹幕则一定不可放置
        if (currentDanmaku != null) return false
        // 未放置的弹幕一定可以放置
        if (placeTimeNanos == DanmakuTrack.NOT_PLACED) return true
        // 当前没有正在显示的弹幕并且弹幕可以被显示
        return frameTimeNanosState.value - placeTimeNanos < durationMillis.value
    }

    override fun clearAll() {
        currentDanmaku?.let(onRemoveDanmaku)
        currentDanmaku = null
    }

    override fun tick() {
        val current = currentDanmaku ?: return
        val danmakuTime = current.placeFrameTimeNanos
        if (frameTimeNanosState.value - danmakuTime >= durationMillis.value * 1_000_000) {
            onRemoveDanmaku(current)
            currentDanmaku = null
        }
    }

    override fun toString(): String {
        return "FixedTrack(index=${trackIndex}, " +
                "placeTime=${currentDanmaku?.placeFrameTimeNanos?.div(1_000_000)})"
    }
}

@Stable
internal class FixedDanmaku<T : SizeSpecifiedDanmaku>(
    var danmaku: T,
    var placeFrameTimeNanos: Long,
    internal val trackIndex: Int,
    private val trackHeight: IntState,
    private val hostHeight: IntState,
    internal val fromBottom: Boolean,
) {
    /**
     * calculate pos y lazily in ui loop
     */
    var y: Float = Float.NaN
        internal set

    internal fun calculatePosY(): Float {
        return if (fromBottom) {
            hostHeight.value - (trackIndex + 1) * trackHeight.value.toFloat()
        } else {
            trackIndex * trackHeight.value.toFloat()
        }
    }

    override fun toString(): String {
        return "FixedDanmaku(width=${danmaku.danmakuWidth}, y=$y)"
    }
}