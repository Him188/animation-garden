/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

@file:OptIn(TestOnly::class)

package me.him188.ani.app.ui.subject.collection

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItemsWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.data.models.subject.SubjectCollectionInfo
import me.him188.ani.app.data.models.subject.TestSubjectCollections
import me.him188.ani.app.data.models.subject.TestSubjectProgressInfos
import me.him188.ani.app.ui.foundation.ProvideFoundationCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.collection.components.rememberTestEditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressButton
import me.him188.ani.app.ui.subject.collection.progress.rememberTestSubjectProgressState
import me.him188.ani.utils.platform.annotations.TestOnly

@PreviewLightDark
@Composable
private fun PreviewSubjectCollectionsColumnPhone() {
    ProvideFoundationCompositionLocalsForPreview {
        SubjectCollectionsColumn(
            items = rememberTestItems(),
            item = { TestSubjectCollectionItem(it) },
        )
    }
}

@Composable
private fun rememberTestItems() =
    remember { MutableStateFlow(PagingData.from(TestSubjectCollections)) }.collectAsLazyPagingItemsWithLifecycle()

@PreviewLightDark
@Composable
private fun PreviewSubjectCollectionsColumnEmptyButLoading() {
    ProvideFoundationCompositionLocalsForPreview {
        SubjectCollectionsColumn(
            items = rememberTestItems(),
            item = { TestSubjectCollectionItem(it) },
            Modifier.fillMaxWidth(),
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewSubjectCollectionsColumnEmpty() {
    ProvideFoundationCompositionLocalsForPreview {
        SubjectCollectionsColumn(
            items = rememberTestItems(),
            item = { TestSubjectCollectionItem(it) },
            Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(TestOnly::class)
@Composable
private fun TestSubjectCollectionItem(it: SubjectCollectionInfo) {
    SubjectCollectionItem(
        item = it,
        editableSubjectCollectionTypeState = rememberTestEditableSubjectCollectionTypeState(),
        onClick = { },
        onShowEpisodeList = { },
        playButton = {
            SubjectProgressButton(
                state = rememberTestSubjectProgressState(
                    when (it.subjectId % 4) {
                        0 -> TestSubjectProgressInfos.NotOnAir
                        1 -> TestSubjectProgressInfos.ContinueWatching2
                        2 -> TestSubjectProgressInfos.Watched2
                        else -> TestSubjectProgressInfos.Done
                    },
                ),
                {},
            )
        },
        blurEnabled = true,
    )
}

@Preview(
    heightDp = 1600, widthDp = 1600,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
@Preview(
    heightDp = 1600, widthDp = 1600,
)
@Composable
private fun PreviewSubjectCollectionsColumnDesktopLarge() {
    ProvideFoundationCompositionLocalsForPreview {
        SubjectCollectionsColumn(
            items = rememberTestItems(),
            item = { TestSubjectCollectionItem(it) },
        )
    }
}
