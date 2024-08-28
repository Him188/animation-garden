package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import me.him188.ani.app.Res
import me.him188.ani.app.bangumi
import me.him188.ani.app.data.models.preference.ProxySettings
import me.him188.ani.app.data.source.danmaku.AniBangumiSeverBaseUrls
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.ConnectionTesterResultIndicator
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextButtonItem
import me.him188.ani.app.ui.settings.framework.components.TextFieldItem
import me.him188.ani.app.ui.settings.framework.components.TextItem
import me.him188.ani.utils.ktor.ClientProxyConfigValidator
import org.jetbrains.compose.resources.painterResource

@Composable
fun NetworkSettingsTab(
    vm: NetworkSettingsViewModel = rememberViewModel { NetworkSettingsViewModel() },
    modifier: Modifier = Modifier,
) {
    val proxySettings by vm.proxySettings

    SettingsTab(modifier) {
        GlobalProxyGroup(proxySettings, vm)
        MediaSourceGroup(vm)
        OtherTestGroup(vm)
        DanmakuGroup(vm)
    }
}

@Composable
private fun SettingsScope.OtherTestGroup(vm: NetworkSettingsViewModel) {
    Group(
        title = { Text("其他测试") },
    ) {
        for (tester in vm.otherTesters.testers) {
            TextItem(
                description = { Text("提供观看记录数据") },
                icon = {
                    Box(Modifier.clip(MaterialTheme.shapes.extraSmall).size(48.dp)) {
                        Image(
                            painterResource(Res.drawable.bangumi), null,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                        )
                    }
                },
                action = {
                    ConnectionTesterResultIndicator(tester, showTime = false)
                },
                title = {
                    Text("Bangumi")
                },
            )
        }

        TextButtonItem(
            onClick = {
                vm.otherTesters.toggleTest()
            },
            title = {
                if (vm.otherTesters.anyTesting) {
                    Text("终止测试")
                } else {
                    Text("开始测试")
                }
            },
        )
    }
}

@Composable
private fun SettingsScope.GlobalProxyGroup(
    proxySettings: ProxySettings,
    vm: NetworkSettingsViewModel
) {
    Group(
        title = { Text("全局代理设置") },
        description = {
            Text("应用于所有数据源以及 Bangumi")
        },
    ) {
        SwitchItem(
            checked = proxySettings.default.enabled,
            onCheckedChange = {
                vm.proxySettings.update(proxySettings.copy(default = proxySettings.default.copy(enabled = it)))
            },
            title = { Text("启用代理") },
            Modifier.placeholder(vm.proxySettings.loading),
            description = { Text("启用后下面的配置才生效") },
        )

        HorizontalDividerItem()

        TextFieldItem(
            proxySettings.default.config.url,
            title = { Text("代理地址") },
            Modifier.placeholder(vm.proxySettings.loading),
            description = {
                Text(
                    "示例: http://127.0.0.1:7890 或 socks5://127.0.0.1:1080",
                )
            },
            onValueChangeCompleted = {
                vm.proxySettings.update(
                    proxySettings.copy(
                        default = proxySettings.default.copy(
                            config = proxySettings.default.config.copy(
                                url = it,
                            ),
                        ),
                    ),
                )
            },
            isErrorProvider = {
                !ClientProxyConfigValidator.isValidProxy(it)
            },
            sanitizeValue = { it.trim() },
        )

        HorizontalDividerItem()

        val username by remember {
            derivedStateOf {
                proxySettings.default.config.authorization?.username ?: ""
            }
        }

        val password by remember {
            derivedStateOf {
                proxySettings.default.config.authorization?.password ?: ""
            }
        }

        TextFieldItem(
            username,
            title = { Text("用户名") },
            Modifier.placeholder(vm.proxySettings.loading),
            description = { Text("可选") },
            placeholder = { Text("无") },
            onValueChangeCompleted = {
                vm.proxySettings.update(
                    proxySettings.copy(
                        default = proxySettings.default.copy(
                            config = proxySettings.default.config.copy(
                                authorization = proxySettings.default.config.authorization?.copy(
                                    username = it,
                                ),
                            ),
                        ),
                    ),
                )
            },
            sanitizeValue = { it },
        )

        HorizontalDividerItem()

        TextFieldItem(
            password,
            title = { Text("密码") },
            Modifier.placeholder(vm.proxySettings.loading),
            description = { Text("可选") },
            placeholder = { Text("无") },
            onValueChangeCompleted = {
                vm.proxySettings.update(
                    proxySettings.copy(
                        default = proxySettings.default.copy(
                            config = proxySettings.default.config.copy(
                                authorization = proxySettings.default.config.authorization?.copy(
                                    password = password,
                                ),
                            ),
                        ),
                    ),
                )
            },
            sanitizeValue = { it },
        )
    }
}

@Composable
private fun SettingsScope.DanmakuGroup(vm: NetworkSettingsViewModel) {
    Group(
        title = { Text("弹幕") },
    ) {
        val danmakuSettings by vm.danmakuSettings
        SwitchItem(
            checked = danmakuSettings.useGlobal,
            onCheckedChange = { vm.danmakuSettings.update(danmakuSettings.copy(useGlobal = it)) },
            title = { Text("全球加速") },
            Modifier.placeholder(vm.danmakuSettings.loading),
            description = { Text("提升在获取弹幕数据的速度\n在中国大陆内启用会减速") },
        )

        SubGroup {
            Group(
                title = { Text("连接速度测试") },
                useThinHeader = true,
            ) {
                for (tester in vm.danmakuServerTesters.testers) {
                    val currentlySelected by derivedStateOf {
                        vm.danmakuSettings.value.useGlobal == (tester.id == AniBangumiSeverBaseUrls.GLOBAL)
                    }
                    TextItem(
                        description = when {
                            currentlySelected -> {
                                { Text("当前使用") }
                            }

                            tester.id == AniBangumiSeverBaseUrls.GLOBAL -> {
                                { Text("建议在其他地区使用") }
                            }

                            else -> {
                                { Text("建议在中国大陆和香港使用") }
                            }
                        },
                        icon = {
                            if (tester.id == AniBangumiSeverBaseUrls.GLOBAL)
                                Icon(
                                    Icons.Rounded.Public, null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            else Text("CN", fontFamily = FontFamily.Monospace)

                        },
                        action = {
                            ConnectionTesterResultIndicator(
                                tester,
                                showTime = true,
                            )
                        },
                        title = {
                            val textColor =
                                if (currentlySelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Unspecified
                                }
                            if (tester.id == AniBangumiSeverBaseUrls.GLOBAL) {
                                Text("全球", color = textColor)
                            } else {
                                Text("中国大陆", color = textColor)
                            }
                        },
                    )
                }

                TextButtonItem(
                    onClick = {
                        vm.danmakuServerTesters.toggleTest()
                    },
                    title = {
                        if (vm.danmakuServerTesters.anyTesting) {
                            Text("终止测试")
                        } else {
                            Text("开始测试")
                        }
                    },
                )
            }

        }
    }
}

