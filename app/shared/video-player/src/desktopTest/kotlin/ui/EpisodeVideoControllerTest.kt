package me.him188.ani.app.videoplayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.test.swipe
import me.him188.ani.app.data.models.preference.VideoScaffoldConfig
import me.him188.ani.app.ui.doesNotExist
import me.him188.ani.app.ui.exists
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme
import me.him188.ani.app.ui.subject.episode.EpisodeVideoImpl
import me.him188.ani.app.ui.subject.episode.TAG_EPISODE_VIDEO_TOP_BAR
import me.him188.ani.app.ui.subject.episode.statistics.VideoLoadingState
import me.him188.ani.app.videoplayer.ui.guesture.GestureFamily
import me.him188.ani.app.videoplayer.ui.guesture.VIDEO_GESTURE_MOUSE_MOVE_SHOW_CONTROLLER_DURATION
import me.him188.ani.app.videoplayer.ui.progress.MediaProgressSliderState
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults
import me.him188.ani.app.videoplayer.ui.progress.TAG_PROGRESS_SLIDER_PREVIEW_POPUP
import me.him188.ani.app.videoplayer.ui.progress.rememberMediaProgressSliderState
import me.him188.ani.app.videoplayer.ui.state.DummyPlayerState
import me.him188.ani.app.videoplayer.ui.top.PlayerTopBar
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.danmaku.ui.DanmakuHostState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

private const val TAG_DETACHED_PROGRESS_SLIDER = "detachedProgressSlider"

/**
 * 测试显示/隐藏进度条和 [GestureFamily]
 */
class EpisodeVideoControllerTest {
    private companion object {
        private val NORMAL_INVISIBLE = ControllerVisibility(
            topBar = false,
            bottomBar = false,
            floatingBottomEnd = true,
            rhsBar = false,
            detachedSlider = false,
        )

        private val NORMAL_VISIBLE = ControllerVisibility(
            topBar = true,
            bottomBar = true,
            floatingBottomEnd = false,
            rhsBar = true,
            detachedSlider = false,
        )

        private val PREVIEW_DETACHED_SLIDER = ControllerVisibility(
            topBar = false,
            bottomBar = false,
            floatingBottomEnd = false,
            rhsBar = false,
            detachedSlider = true,
        )
    }


    private val controllerState = VideoControllerState(ControllerVisibility.Invisible)
    private val playerState = DummyPlayerState()
    private lateinit var progressSliderState: MediaProgressSliderState

    private val SemanticsNodeInteractionsProvider.detachedProgressSlider
        get() = onNodeWithTag(TAG_DETACHED_PROGRESS_SLIDER, useUnmergedTree = true)
    private val SemanticsNodeInteractionsProvider.topBar
        get() = onNodeWithTag(TAG_EPISODE_VIDEO_TOP_BAR, useUnmergedTree = true)
    private val SemanticsNodeInteractionsProvider.previewPopup
        get() = onNodeWithTag(TAG_PROGRESS_SLIDER_PREVIEW_POPUP, useUnmergedTree = true)

    @Composable
    private fun Player(gestureFamily: GestureFamily) {
        ProvideCompositionLocalsForPreview(colorScheme = aniDarkColorTheme()) {
            val progressSliderState = rememberMediaProgressSliderState(
                playerState,
                onPreview = {
                    // not yet supported
                },
                onPreviewFinished = {
                    playerState.seekTo(it)
                },
            )
            SideEffect { this.progressSliderState = progressSliderState }
            EpisodeVideoImpl(
                playerState = playerState,
                expanded = true,
                hasNextEpisode = true,
                onClickNextEpisode = {},
                videoControllerState = controllerState,
                title = { PlayerTopBar() },
                danmakuHostState = remember { DanmakuHostState() },
                danmakuEnabled = false,
                onToggleDanmaku = {},
                videoLoadingState = { VideoLoadingState.Succeed(isBt = true) },
                danmakuConfig = { DanmakuConfig.Default },
                onClickFullScreen = {},
                onExitFullscreen = {},
                danmakuEditor = {},
                configProvider = { VideoScaffoldConfig.Default },
                sideSheets = {},
                onShowMediaSelector = {},
                onShowSelectEpisode = {},
                onClickScreenshot = {},
                detachedProgressSlider = {
                    PlayerControllerDefaults.MediaProgressSlider(
                        progressSliderState,
                        cacheProgressState = playerState.cacheProgress,
                        Modifier.testTag(TAG_DETACHED_PROGRESS_SLIDER),
                        enabled = false,
                    )
                },
                progressSliderState = progressSliderState,
                danmakuFrozen = true,
                gestureFamily = gestureFamily,
            )
        }
    }


    /**
     * @see GestureFamily.clickToToggleController
     */
    @Test
    fun `touch - clickToToggleController - show`() = runSkikoComposeUiTest {
        setContent {
            Player(GestureFamily.TOUCH)
        }
        runOnIdle {
            assertEquals(
                NORMAL_INVISIBLE,
                controllerState.visibility,
            )
        }

        onRoot().performClick()
        runOnIdle {
            waitUntil { topBar.exists() }
            assertEquals(
                NORMAL_VISIBLE,
                controllerState.visibility,
            )
        }
    }

    /**
     * @see GestureFamily.clickToToggleController
     */
    @Test
    fun `touch - clickToToggleController - hide`() = runSkikoComposeUiTest {
        setContent {
            Player(GestureFamily.TOUCH)
        }
        runOnIdle {
            assertEquals(NORMAL_INVISIBLE, controllerState.visibility)
        }

        onRoot().performClick()
        runOnIdle {
            waitUntil { topBar.exists() }
            assertEquals(NORMAL_VISIBLE, controllerState.visibility)
        }

        onRoot().performClick()
        runOnIdle {
            waitUntil { topBar.doesNotExist() }
            assertEquals(NORMAL_INVISIBLE, controllerState.visibility)
        }
    }

    /**
     * @see GestureFamily.swipeToSeek
     */
    @Test
    fun `touch - swipeToSeek shows detached slider`() = runSkikoComposeUiTest {
        setContent {
            Player(GestureFamily.TOUCH)
        }
        waitForIdle()
        val root = onAllNodes(isRoot()).onFirst()
        val detachedProgressSlider =
            onNodeWithTag(TAG_DETACHED_PROGRESS_SLIDER, useUnmergedTree = true)

        // 初始没有进度条
        runOnIdle {
            assertEquals(NORMAL_INVISIBLE, controllerState.visibility)
            detachedProgressSlider.assertDoesNotExist()
        }

        // 按下手指并移动, 显示独立进度条
        root.performTouchInput {
            down(centerLeft)
            moveBy(Offset(width / 2f, 0f))
        }
        runOnIdle {
            waitUntil { detachedProgressSlider.exists() }
            assertEquals(PREVIEW_DETACHED_SLIDER, controllerState.visibility)
//            root.assertScreenshot("/screenshots/EpisodeVideoControllerTest.touch___swipeToSeek_shows_detached_slider.png")
        }

        // 松开手指
        root.performTouchInput {
            up()
        }
        runOnIdle {
            waitUntil { detachedProgressSlider.doesNotExist() }
            assertEquals(NORMAL_INVISIBLE, controllerState.visibility)
        }
    }

    /**
     * @see GestureFamily.swipeToSeek
     */
    @Test
    fun `touch - swipe when controller is already fully visible`() = runSkikoComposeUiTest {
        setContent {
            Player(GestureFamily.TOUCH)
        }
        waitForIdle()
        val root = onAllNodes(isRoot()).onFirst()

        root.performClick() // 显示全部控制器
        runOnIdle {
            waitUntil { topBar.exists() }
            detachedProgressSlider.assertDoesNotExist()
        }

        root.performTouchInput {
            down(centerLeft)
            moveBy(Offset(width / 2f, 0f))
        }
        runOnIdle {
            waitUntil { previewPopup.doesNotExist() } // TODO: 当滑动进度条时会显示 popup 后, 修改这里 
            detachedProgressSlider.assertDoesNotExist()
            assertEquals(NORMAL_VISIBLE, controllerState.visibility)
        }

        root.performTouchInput {
            up()
        }
        runOnIdle {
            waitUntil { previewPopup.doesNotExist() }
            detachedProgressSlider.assertDoesNotExist()
            assertEquals(NORMAL_VISIBLE, controllerState.visibility)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // mouse
    ///////////////////////////////////////////////////////////////////////////

    /**
     * [GestureFamily.MOUSE] 在屏幕中间滑动鼠标, 会临时显示几秒控制器. 几秒后自动隐藏.
     *
     * @see GestureFamily.mouseHoverForController
     */
    @Test
    fun `mouse - mouseHoverForController - center screen`() = runSkikoComposeUiTest {
        setContent {
            Player(GestureFamily.MOUSE)
        }
        runOnIdle {
            assertEquals(
                NORMAL_INVISIBLE,
                controllerState.visibility,
            )
        }

        mainClock.autoAdvance = false // 三秒后会自动隐藏, 这里不能让他自动前进时间
        onRoot().performTouchInput { // 似乎 mouseInput 不太工作, 但我们就用 touch 也能实现
            swipe(centerLeft, center)
        }
        runOnIdle {
            waitUntil { topBar.exists() }
            assertEquals(
                NORMAL_VISIBLE,
                controllerState.visibility,
            )
        }


        mainClock.advanceTimeBy(VIDEO_GESTURE_MOUSE_MOVE_SHOW_CONTROLLER_DURATION.inWholeMilliseconds)
        mainClock.autoAdvance = true
        runOnIdle {
            waitUntil { topBar.doesNotExist() }
            assertEquals(
                NORMAL_INVISIBLE,
                controllerState.visibility,
            )
        }
    }

    /**
     * 鼠标悬浮在控制器上, 会保持显示
     */
    @Test
    fun `mouse - hover to always on - bottom bar`() = runSkikoComposeUiTest {
        testRequestAlwaysOn(
            performGesture = {
                // 鼠标移动到控制器上
                onRoot().performMouseInput {
                    moveTo(bottomCenter) // 肯定在 bottomBar 区域内
                }
            },
            expectAlwaysOn = true,
        )
    }

    /**
     * 鼠标悬浮在控制器上, 会保持显示
     */
    @Test
    fun `mouse - hover to always on - top bar`() = runSkikoComposeUiTest {
        testRequestAlwaysOn(
            performGesture = {
                // 鼠标移动到控制器上
                onRoot().performMouseInput {
                    moveTo(topCenter) // 肯定在 topBar 区域内
                }
            },
            expectAlwaysOn = true,
        )
    }

    /**
     * 手指单击控制器, 不会触发保持显示
     */
    @Test
    fun `mouse - clicking does not request always on - bottom bar`() = runSkikoComposeUiTest {
        testRequestAlwaysOn(
            performGesture = {
                // 手指单击控制器
                onRoot().performTouchInput {
                    click(bottomCenter) // 肯定在 bottomBar 区域内
                }
            },
            expectAlwaysOn = false,
        )
    }

    /**
     * 手指单击控制器, 不会触发保持显示
     */
    @Test
    fun `mouse - clicking does not request always on - top bar`() = runSkikoComposeUiTest {
        testRequestAlwaysOn(
            performGesture = {
                // 手指单击控制器
                onRoot().performTouchInput {
                    click(topCenter) // 肯定在 topBar 区域内
                }
            },
            expectAlwaysOn = false,
        )
    }

    private fun SkikoComposeUiTest.testRequestAlwaysOn(
        performGesture: () -> Unit,
        expectAlwaysOn: Boolean = false,
    ) {
        setContent {
            Player(GestureFamily.MOUSE)
        }
        runOnIdle {
            assertEquals(
                NORMAL_INVISIBLE,
                controllerState.visibility,
            )
        }

        // 显示控制器
        mainClock.autoAdvance = false
        onRoot().performTouchInput {
            swipe(centerLeft, center)
        }
        runOnIdle {
            waitUntil { topBar.exists() }
            assertEquals(
                NORMAL_VISIBLE,
                controllerState.visibility,
            )
        }

        performGesture()

        mainClock.advanceTimeBy((VIDEO_GESTURE_MOUSE_MOVE_SHOW_CONTROLLER_DURATION + 1.seconds).inWholeMilliseconds)
        mainClock.autoAdvance = true
        // 手指点击不应当显示
        runOnIdle {
            assertEquals(expectAlwaysOn, controllerState.alwaysOn)
            if (expectAlwaysOn) {
                waitUntil { topBar.exists() }
                assertEquals(
                    NORMAL_VISIBLE,
                    controllerState.visibility,
                )
            } else {
                waitUntil { topBar.doesNotExist() }
                assertEquals(
                    NORMAL_INVISIBLE,
                    controllerState.visibility,
                )
            }
        }
    }
}
