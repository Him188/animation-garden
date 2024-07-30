package me.him188.ani.app.ui.settings.framework

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.datasources.api.source.asAutoCloseable
import me.him188.ani.utils.ktor.createDefaultHttpClient
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.platform.annotations.TestOnly
import org.koin.core.component.KoinComponent
import kotlin.jvm.JvmField
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Stable
abstract class AbstractSettingsViewModel : AbstractViewModel(), KoinComponent {
    protected val httpClient by lazy {
        createDefaultHttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 30_000
            }
        }.also {
            addCloseable(it.asAutoCloseable())
        }
    }

    private inline fun <T> propertyDelegateProvider(
        crossinline createProperty: (property: KProperty<*>) -> T,
    ): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, T>> {
        return PropertyDelegateProvider { _, property ->
            val value = createProperty(property)
            ReadOnlyProperty { _, _ ->
                value
            }
        }
    }

    /**
     * @param placeholder 可以是 [T] 的 supertype, 包括 `null`.
     */
    fun <T : P, P> settings(
        pref: me.him188.ani.app.data.repository.Settings<T>,
        placeholder: P,
    ) =
        propertyDelegateProvider {
            Settings(it.name, pref, placeholder)
        }

    @Stable
    inner class Settings<T : P, P>(
        private val debugName: String,
        private val pref: me.him188.ani.app.data.repository.Settings<T>,
        private val placeholder: P,
    ) : State<P> {
        val loading by derivedStateOf {
            value === placeholder
        }

        private val tasker = MonoTasker(backgroundScope)
        fun update(value: T) {
            tasker.launch {
                logger.info { "Updating $debugName: $value" }
                pref.set(value)
            }
        }

        fun updateDebounced(value: T, delay: Duration = 500.milliseconds) {
            tasker.launch {
                delay(delay)
                logger.info { "Updating $debugName: $value" }
                pref.set(value)
            }
        }

        // 在 preview 时传入, 避免一直显示加载中状态
        @Suppress("PropertyName")
        @JvmField
        @TestOnly
        var _valueOverride: T? = null
        private val valueDelegate = pref.flow.produceState(placeholder)

        @OptIn(TestOnly::class)
        override val value: P
            get() = _valueOverride ?: valueDelegate.value
    }

    /**
     * 创建一个单个的测试器, 需要使用 `val tester by connectionTester {}`
     */
    fun connectionTester(
        testConnection: suspend () -> ConnectionTestResult,
    ) = propertyDelegateProvider {
        SingleTester(ConnectionTester(it.name, testConnection), backgroundScope)
    }

    @Stable
    class SingleTester<T>(
        tester: Tester<T>,
        backgroundScope: CoroutineScope,
    ) : Testers<T>(listOf(tester), backgroundScope) {
        val tester get() = testers.single()
    }

    @Stable
    open class Testers<T>(
        val testers: List<Tester<T>>,
        backgroundScope: CoroutineScope,
    ) {
        private val testScope = MonoTasker(backgroundScope)
        fun testAll() {
            testScope.launch {
                supervisorScope {
                    testers.forEach {
                        launch {
                            it.test()
                        }
                    }
                }
            }
        }

        fun cancel() {
            testScope.cancel()
        }

        fun toggleTest() {
            if (testers.any { it.isTesting }) {
                cancel()
            } else {
                testAll()
            }
        }

        val anyTesting by derivedStateOf {
            testers.any { it.isTesting }
        }
    }
}