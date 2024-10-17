/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.search

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideFoundationCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.layout.CarouselItemDefaults.Text
import me.him188.ani.app.ui.foundation.layout.Zero
import me.him188.ani.app.ui.foundation.preview.PreviewSizeClasses
import me.him188.ani.utils.platform.annotations.TestOnly

@OptIn(TestOnly::class)
@Composable
@PreviewSizeClasses
@Preview
fun PreviewSearchPage() = ProvideFoundationCompositionLocalsForPreview {
    val scope = rememberCoroutineScope()
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
        SearchPage(
            createTestSearchPageState(scope),
            WindowInsets.Zero,
            { Text("Hello, World!") },
        )
    }
}
