/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.wizard.step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.BangumiNextIconColor
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.rendering.BangumiNext
import me.him188.ani.app.ui.wizard.WizardLayoutParams
import me.him188.ani.utils.platform.currentPlatform
import me.him188.ani.utils.platform.isAndroid

@Composable
private fun RegisterTip(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Lightbulb,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
internal fun BangumiAuthorize(
    onClickAuthorize: () -> Unit,
    onClickNeedHelp: () -> Unit,
    modifier: Modifier = Modifier,
    layoutParams: WizardLayoutParams = WizardLayoutParams.Default
) {
    SettingsTab(modifier) {
        Row(
            modifier = Modifier
                .padding(horizontal = layoutParams.horizontalPadding)
                .padding(top = 16.dp, bottom = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.BangumiNext,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = BangumiNextIconColor,
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = layoutParams.horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Ani 的追番进度管理服务由 Bangumi 提供",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "Bangumi 番组计划 是一个中文 ACGN 互联网分享与交流项目，不提供资源下载。" +
                            "登录 Bangumi 账号方可使用收藏、记录观看进度等功能。",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Bangumi 注册提示",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                remember {
                    buildList {
                        add("请使用常见邮箱注册，例如 QQ, 网易, Outlook")
                        add("如果提示激活失败，请尝试删除激活码的最后一个字再手动输入")
                        if (currentPlatform().isAndroid()) {
                            add("如果浏览器提示网站被屏蔽或登录成功后无法跳转，请尝试在系统设置更换默认浏览器")
                        }
                    }
                }.forEach {
                    RegisterTip(it, modifier = Modifier.fillMaxWidth())
                }
            }
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onClickAuthorize,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 720.dp),
                ) {
                    Text("启动浏览器授权")
                }
                TextButton(
                    onClick = onClickNeedHelp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 720.dp),
                ) {
                    Text("无法登录？点击获取帮助")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}