/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.desktop

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.SystemTheme
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import me.him188.ani.app.data.models.preference.DarkMode
import me.him188.ani.app.data.persistent.dataStores
import me.him188.ani.app.data.repository.SavedWindowState
import me.him188.ani.app.data.repository.WindowStateRepository
import me.him188.ani.app.data.repository.WindowStateRepositoryImpl
import me.him188.ani.app.data.repository.user.SettingsRepository
import me.him188.ani.app.desktop.storage.AppFolderResolver
import me.him188.ani.app.desktop.storage.AppInfo
import me.him188.ani.app.desktop.window.WindowFrame
import me.him188.ani.app.domain.media.fetch.MediaSourceManager
import me.him188.ani.app.domain.media.resolver.DesktopWebMediaResolver
import me.him188.ani.app.domain.media.resolver.HttpStreamingMediaResolver
import me.him188.ani.app.domain.media.resolver.LocalFileMediaResolver
import me.him188.ani.app.domain.media.resolver.MediaResolver
import me.him188.ani.app.domain.media.resolver.TorrentMediaResolver
import me.him188.ani.app.domain.session.SessionManager
import me.him188.ani.app.domain.settings.ProxyProvider
import me.him188.ani.app.domain.torrent.DefaultTorrentManager
import me.him188.ani.app.domain.torrent.TorrentManager
import me.him188.ani.app.domain.update.UpdateManager
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.navigation.DesktopBrowserNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.navigation.NavRoutes
import me.him188.ani.app.platform.AniBuildConfigDesktop
import me.him188.ani.app.platform.AniCefApp
import me.him188.ani.app.platform.AppStartupTasks
import me.him188.ani.app.platform.AppTerminator
import me.him188.ani.app.platform.DefaultAppTerminator
import me.him188.ani.app.platform.DesktopContext
import me.him188.ani.app.platform.ExtraWindowProperties
import me.him188.ani.app.platform.GrantedPermissionManager
import me.him188.ani.app.platform.JvmLogHelper
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.PermissionManager
import me.him188.ani.app.platform.PlatformWindow
import me.him188.ani.app.platform.createAppRootCoroutineScope
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.platform.getCommonKoinModule
import me.him188.ani.app.platform.notification.NoopNotifManager
import me.him188.ani.app.platform.notification.NotifManager
import me.him188.ani.app.platform.startCommonKoinModule
import me.him188.ani.app.platform.window.LocalTitleBarThemeController
import me.him188.ani.app.platform.window.setTitleBar
import me.him188.ani.app.tools.update.DesktopUpdateInstaller
import me.him188.ani.app.tools.update.UpdateInstaller
import me.him188.ani.app.torrent.anitorrent.AnitorrentLibraryLoader
import me.him188.ani.app.ui.foundation.LocalWindowState
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.layout.LocalPlatformWindow
import me.him188.ani.app.ui.foundation.layout.isSystemInFullscreen
import me.him188.ani.app.ui.foundation.navigation.LocalOnBackPressedDispatcherOwner
import me.him188.ani.app.ui.foundation.navigation.SkikoOnBackPressedDispatcherOwner
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.foundation.theme.LocalThemeSettings
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.Toast
import me.him188.ani.app.ui.foundation.widgets.ToastViewModel
import me.him188.ani.app.ui.foundation.widgets.Toaster
import me.him188.ani.app.ui.main.AniApp
import me.him188.ani.app.ui.main.AniAppContent
import me.him188.ani.desktop.generated.resources.Res
import me.him188.ani.desktop.generated.resources.a_round
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.io.toKtPath
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.platform.currentPlatform
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.openani.mediamp.MediampPlayerFactory
import org.openani.mediamp.MediampPlayerFactoryLoader
import org.openani.mediamp.compose.MediampPlayerSurfaceProviderLoader
import org.openani.mediamp.vlc.VlcMediampPlayer
import org.openani.mediamp.vlc.VlcMediampPlayerFactory
import org.openani.mediamp.vlc.compose.VlcMediampPlayerSurfaceProvider
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess
import kotlin.time.measureTime


private val logger by lazy { logger("Ani") }
private inline val toplevelLogger get() = logger

object AniDesktop {
//    init {
    // 如果要在视频上面显示弹幕或者播放按钮需要在启动的时候设置 system's blending 并且使用1.6.1之后的 Compose 版本
    // system's blending 在windows 上还是有问题，使用 EmbeddedMediaPlayerComponent 还是不会显示视频，但是在Windows 系统上使用 CallbackMediaPlayerComponent 就没问题。
    // See https://github.com/open-ani/ani/issues/115#issuecomment-2092567727
//        System.setProperty("compose.interop.blending", "true")
//    }

    init {
        System.setProperty("native.encoding", "UTF-8")
    }


    private fun calculateWindowSize(
        desiredWidth: Dp,
        desiredHeight: Dp,
        screenSize: DpSize = ScreenUtils.getScreenSize()
    ): DpSize {
        return DpSize(
            width = if (desiredWidth > screenSize.width) screenSize.width else desiredWidth,
            height = if (desiredHeight > screenSize.height) screenSize.height else desiredHeight,
        )
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val originalExceptionHandler = Thread.currentThread().uncaughtExceptionHandler
        Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { t, e ->
            logger.error(e) { "!!!ANI FATAL EXCEPTION!!!" }
            e.printStackTrace()
            Thread.sleep(1000) // wait for logging to finish
            originalExceptionHandler.uncaughtException(t, e)
            Thread.sleep(5000)
            exitProcess(1)
        }

        val projectDirectories = AppFolderResolver.INSTANCE.resolve(
            AppInfo(
                "me",
                "Him188",
                if (AniBuildConfigDesktop.isDebug) "Ani-debug" else "Ani",
            ),
        )
        val dataDir = projectDirectories.data
        val cacheDir = projectDirectories.cache

        val logsDir = dataDir.resolve("logs").toFile().apply { mkdirs() }

        Log4j2Config.configureLogging(logsDir)

        if (AniBuildConfigDesktop.isDebug) {
            logger.info { "Debug mode enabled" }
        }
        logger.info { "Ani platform: ${currentPlatform()}, version: ${currentAniBuildConfig.versionName}" }

        logger.info { "dataDir: file://${dataDir.absolutePathString().replace(" ", "%20")}" }
        logger.info { "cacheDir: file://${cacheDir.absolutePathString().replace(" ", "%20")}" }
        logger.info { "logsDir: file://${logsDir.absolutePath.replace(" ", "%20")}" }
        val coroutineScope = createAppRootCoroutineScope()

        coroutineScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                JvmLogHelper.deleteOldLogs(logsDir.toPath())
            }.onFailure {
                logger.error(it) { "Failed to delete old logs" }
            }
        }


        val defaultSize = DpSize(1301.dp, 855.dp)
        // Get the screen size as a Dimension object
        val windowState = WindowState(
            size = kotlin.runCatching {
                calculateWindowSize(defaultSize.width, defaultSize.height)
            }.onFailure {
                logger.error(it) { "Failed to calculate window size" }
            }.getOrElse {
                defaultSize
            },
            position = WindowPosition.Aligned(Alignment.Center),
        )
        val context = DesktopContext(
            windowState,
            dataDir.toFile(),
            cacheDir.toFile(),
            logsDir,
            ExtraWindowProperties(),
        )

        val time = measureTime {
            SingleInstanceChecker.instance.ensureSingleInstance()
        }
        logger.info { "Single instance check took $time" }

        coroutineScope.launch(Dispatchers.IO) {
            // since 3.4.0, anitorrent 增加后不兼容 QB 数据
            cacheDir.toFile().resolve("torrent").let {
                if (it.exists()) {
                    it.deleteRecursively()
                }
            }
        }

        val koin = startKoin {
            modules(getCommonKoinModule({ context }, coroutineScope))
            modules(
                module {
//                single<SubjectNavigator> { AndroidSubjectNavigator() }
//                single<AuthorizationNavigator> { AndroidAuthorizationNavigator() }
//                single<BrowserNavigator> { AndroidBrowserNavigator() }
                    single<TorrentManager> {
                        DefaultTorrentManager.create(
                            coroutineScope.coroutineContext,
                            get(),
                            proxyProvider = get<ProxyProvider>().proxy,
                            get(),
                            get(),
                            baseSaveDir = {
                                val saveDir = runBlocking {
                                    get<SettingsRepository>().mediaCacheSettings.flow.first().saveDir
                                        ?.let(::Path)
                                } ?: context.torrentDataCacheDir.toKtPath()
                                toplevelLogger.info { "TorrentManager saveDir: $saveDir" }
                                saveDir.inSystem
                            },
                        )
                    }
                    single<MediampPlayerFactory<*>> {
                        MediampPlayerFactoryLoader.register(VlcMediampPlayerFactory())
                        MediampPlayerSurfaceProviderLoader.register(VlcMediampPlayerSurfaceProvider())
                        MediampPlayerFactoryLoader.first()
                    }
                    single<BrowserNavigator> { DesktopBrowserNavigator() }
                    factory<MediaResolver> {
                        MediaResolver.from(
                            get<TorrentManager>().engines
                                .map { TorrentMediaResolver(it) }
                                .plus(LocalFileMediaResolver())
                                .plus(HttpStreamingMediaResolver())
                                .plus(
                                    DesktopWebMediaResolver(
                                        context,
                                        get<MediaSourceManager>().webVideoMatcherLoader,
                                    ),
                                ),
                        )
                    }
                    single<UpdateInstaller> { DesktopUpdateInstaller.currentOS() }
                    single<PermissionManager> { GrantedPermissionManager }
                    single<NotifManager> { NoopNotifManager }
                    single<WindowStateRepository> { WindowStateRepositoryImpl(context.dataStores.savedWindowStateStore) }
                    single<AppTerminator> { DefaultAppTerminator }
                },
            )
        }.startCommonKoinModule(coroutineScope)


        // Startup ok, run test task if needed
        System.getenv("ANIMEKO_DESKTOP_TEST_TASK")?.let { taskName ->
            logger.info { "Running test task: $taskName" }

            val argc = System.getenv("ANIMEKO_DESKTOP_TEST_ARGC")?.toIntOrNull() ?: 0
            val args = (0 until argc).mapNotNull { i ->
                System.getenv("ANIMEKO_DESKTOP_TEST_ARGV_$i")
            }
            TestTasks.handleTestTask(taskName, args, context)
        }

        // Initialize CEF application.
        coroutineScope.launch {
            AnitorrentLibraryLoader.loadLibraries() 
            // Load anitorrent libraries before JCEF, so they won't load at the same time.
            // We suspect concurrent loading of native libraries may cause some issues #1121.
            
            val proxySettings = koin.koin.get<ProxyProvider>()
                .proxy.first()

            AniCefApp.initialize(
                logDir = dataDir.toFile().resolve("logs"),
                cacheDir = cacheDir.toFile().resolve("jcef-cache"),
                proxyServer = proxySettings?.url,
                proxyAuthUsername = proxySettings?.authorization?.username,
                proxyAuthPassword = proxySettings?.authorization?.password,
            )

            // 预先加载 VLC, https://github.com/open-ani/ani/issues/618
            kotlin.runCatching {
                VlcMediampPlayer.prepareLibraries()
            }.onFailure {
                logger.error(it) { "Failed to prepare VLC" }
            }
        }

        coroutineScope.launch {
            kotlin.runCatching {
                val desktopUpdateInstaller = koin.koin.get<UpdateInstaller>() as DesktopUpdateInstaller
                desktopUpdateInstaller.deleteOldUpdater()
            }.onFailure {
                logger.error(it) { "Failed to delete update installer" }
            }

            kotlin.runCatching {
                koin.koin.get<UpdateManager>().deleteInstalledFiles()
            }.onFailure {
                logger.error(it) { "Failed to delete installed files" }
            }
        }

        val navigator = AniNavigator()

        coroutineScope.launch {
            navigator.awaitNavController()
            val sessionManager by koin.koin.inject<SessionManager>()
            AppStartupTasks.verifySession(sessionManager, navigator)
        }

        val windowStateRepository = koin.koin.get<WindowStateRepository>()
        val savedWindowState: SavedWindowState? = runBlocking {
            windowStateRepository.flow.firstOrNull()
        }

        val systemThemeDetector = SystemThemeDetector()

        application {
            WindowStateRecorder(
                windowState = windowState,
                saved = savedWindowState,
                update = {
                    runBlocking {
                        windowStateRepository.update(it)
                    }
                },
            )

            Window(
                onCloseRequest = { exitApplication() },
                state = windowState,
                title = "Ani",
                icon = painterResource(Res.drawable.a_round),
            ) {
                val lifecycleOwner = LocalLifecycleOwner.current
                val backPressedDispatcherOwner = remember {
                    SkikoOnBackPressedDispatcherOwner(navigator, lifecycleOwner)
                }

                SideEffect {
                    // 防止闪眼
                    window.background = java.awt.Color.BLACK
                    window.contentPane.background = java.awt.Color.BLACK

                    logger.info {
                        "renderApi: " + this.window.renderApi
                    }
                }

                val systemTheme by systemThemeDetector.current.collectAsStateWithLifecycle()
                CompositionLocalProvider(
                    LocalContext provides context,
                    LocalWindowState provides windowState,
                    LocalPlatformWindow provides remember(window.windowHandle) {
                        PlatformWindow(
                            windowHandle = window.windowHandle,
                            windowScope = this,
                        )
                    },
                    LocalOnBackPressedDispatcherOwner provides backPressedDispatcherOwner,
                    @OptIn(InternalComposeUiApi::class)
                    LocalSystemTheme provides systemTheme,
                ) {

                    WindowFrame(
                        windowState = windowState,
                        onCloseRequest = { exitApplication() },
                    ) {
                        MainWindowContent(navigator)
                    }

                }
            }

        }
        // unreachable here
    }
}

@OptIn(InternalComposeUiApi::class)
@Composable
private fun FrameWindowScope.MainWindowContent(
    aniNavigator: AniNavigator,
) {
    val settingsRepository = KoinPlatform.getKoin().get<SettingsRepository>()
    AniApp {
        window.setTitleBar(AniThemeDefaults.navigationContainerColor, isSystemInDarkTheme())
        val themeSettings = LocalThemeSettings.current
        val titleBarThemeController = LocalTitleBarThemeController.current
        val systemTheme = LocalSystemTheme.current
        titleBarThemeController?.isDark = when (themeSettings.darkMode) {
            DarkMode.AUTO -> systemTheme == SystemTheme.Dark
            DarkMode.LIGHT -> false
            DarkMode.DARK -> true
        }
        Box(
            Modifier
                .ifThen(!isSystemInFullscreen()) {
                    statusBarsPadding() // Windows 有, macOS 没有
                }
                .fillMaxSize(),
        ) {
            Box(Modifier.fillMaxSize()) {
                val paddingByWindowSize by animateDpAsState(0.dp)

                val vm = viewModel { ToastViewModel() }

                val showing by vm.showing.collectAsStateWithLifecycle()
                val content by vm.content.collectAsStateWithLifecycle()

                CompositionLocalProvider(
                    LocalNavigator provides aniNavigator,
                    LocalToaster provides object : Toaster {
                        override fun toast(text: String) {
                            vm.show(text)
                        }
                    },
                ) {
                    Box(Modifier.padding(all = paddingByWindowSize)) {
                        val uiSettings by settingsRepository.uiSettings.flow.collectAsStateWithLifecycle(null)
                        uiSettings?.let {
                            AniAppContent(aniNavigator, NavRoutes.Main(it.mainSceneInitialPage))
                        }
                        Toast({ showing }, { Text(content) })
                    }
                }
            }
        }
    }
}

@Composable
private fun WindowStateRecorder(
    windowState: WindowState,
    saved: SavedWindowState?,
    update: (SavedWindowState) -> Unit,
) {
    // 记录窗口大小
    DisposableEffect(Unit) {
        if (saved != null) {
            val savedWindowState = WindowState(
                position = WindowPosition(
                    x = saved.x,
                    y = saved.y,
                ),
                size = DpSize(
                    width = saved.width,
                    height = saved.height,
                ),
            )
            //保存的窗口尺寸和大小全都合规时，才使用，否则使用默认设置
            val minimumSize = DpSize(800.dp, 600.dp)
            val screenSize = ScreenUtils.getScreenSize()
            if (
                (savedWindowState.size.width > minimumSize.width || savedWindowState.size.height > minimumSize.height)
                && savedWindowState.position.x < screenSize.width && savedWindowState.position.y < screenSize.height
            ) {
                windowState.apply {
                    position = savedWindowState.position
                    size = savedWindowState.size
                }
            }
        }

        onDispose {
            update(
                SavedWindowState(
                    x = windowState.position.x,
                    y = windowState.position.y,
                    width = windowState.size.width,
                    height = windowState.size.height,
                ),
            )
        }
    }
}
