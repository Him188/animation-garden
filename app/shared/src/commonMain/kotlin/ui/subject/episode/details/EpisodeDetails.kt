package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Outbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.models.subject.SubjectAiringInfo
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.ui.subject.collection.EditableSubjectCollectionTypeButton
import me.him188.ani.app.ui.subject.collection.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.collection.OnAirLabel
import me.him188.ani.app.ui.subject.details.components.OutlinedTag
import me.him188.ani.app.ui.subject.details.components.renderSubjectSeason
import me.him188.ani.app.ui.subject.episode.EpisodePresentation
import me.him188.ani.app.ui.subject.rating.EditableRating
import me.him188.ani.app.ui.subject.rating.EditableRatingState
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.FileSize.Companion.Unspecified
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage
import kotlin.coroutines.CoroutineContext

@Stable
class EpisodeDetailsState(
    episodePresentation: Flow<EpisodePresentation>,
    subjectInfo: Flow<SubjectInfo>,
    airingInfo: Flow<SubjectAiringInfo>,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    private val episode by episodePresentation.produceState(EpisodePresentation.Placeholder)
    private val subject by subjectInfo.produceState(SubjectInfo.Empty)
    val airingInfo by airingInfo.produceState(SubjectAiringInfo.EmptyCompleted)

    val subjectId by derivedStateOf { subject.id }
    val coverImageUrl by derivedStateOf { subject.imageLarge }
    val episodeTitle by derivedStateOf { episode.title }
    val episodeSort by derivedStateOf { episode.sort }
    val subjectTitle by derivedStateOf { subject.displayName }
}

/**
 * 番剧详情内容, 包含条目的基本信息, 选集, 评分.
 */
@Composable
fun EpisodeDetails(
    state: EpisodeDetailsState,
    episodeCarouselState: EpisodeCarouselState,
    editableRatingState: EditableRatingState,
    editableSubjectCollectionTypeState: EditableSubjectCollectionTypeState,

    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    var showMore by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
//    ShareEpisodeDropdown(
//        showMore,
//        { showMore = false },
//        onClickCopyLink = {
//            GlobalContext.get().get<BrowserNavigator>()
//                .copyToClipboard(context, "magnet:?xt=urn:btih:1234567890")
//        },
//        onClickDownload = {
//            GlobalContext.get().get<BrowserNavigator>()
//                .openBrowser(context, state.ori)
//        },
//    )
    val navigator = LocalNavigator.current
    EpisodeDetailsScaffold(
        coverImageUrl = state.coverImageUrl,
        subjectTitle = { Text(state.subjectTitle) },
        subjectSeasonTags = {
            OutlinedTag { Text(renderSubjectSeason(state.airingInfo.airDate)) }
            OnAirLabel(
                state.airingInfo,
                Modifier.align(Alignment.CenterVertically),
                style = LocalTextStyle.current,
                statusColor = LocalContentColor.current,
            )
        },
        episodeCarousel = { contentPadding ->
            EpisodeCarousel(
                episodeCarouselState,
                contentPadding = contentPadding,
            )
        },
        subjectRating = {
            EditableRating(editableRatingState)
        },
        subjectCollectionActionButton = {
            EditableSubjectCollectionTypeButton(editableSubjectCollectionTypeState)
        },
        onClickCache = {
            navigator.navigateSubjectCaches(state.subjectId)
        },
        onClickShare = {
        },
        modifier = modifier,
        horizontalPadding = horizontalPadding,
    )
}

@Composable
fun EpisodeDetailsScaffold(
    coverImageUrl: String?,
    subjectTitle: @Composable () -> Unit,
    subjectSeasonTags: @Composable FlowRowScope.() -> Unit,
    subjectRating: @Composable () -> Unit,
    subjectCollectionActionButton: @Composable () -> Unit,
    episodeCarousel: @Composable (PaddingValues) -> Unit,
    onClickCache: () -> Unit,
    onClickShare: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    Column(modifier) {
        @Composable
        fun SectionTitle(
            modifier: Modifier = Modifier,
            actions: @Composable RowScope.() -> Unit = {},
            content: @Composable () -> Unit,
        ) {
            Row(
                modifier.heightIn(min = 40.dp).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                    Row(Modifier.weight(1f)) {
                        content()
                    }
                    Row(Modifier.padding(start = 16.dp)) {
                        actions()
                    }
                }
            }
        }

        // header
        Box {
            Column(
                Modifier.padding(horizontal = horizontalPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row {
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row {
                            ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                                SelectionContainer { subjectTitle() }
                            }
                        }

                        Row {
                            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                                FlowRow(
                                    Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                                ) {
                                    subjectSeasonTags()
                                }
                            }

                            Box(Modifier.padding(start = 16.dp).align(Alignment.Bottom)) {
                                subjectRating()
                            }
                        }
                    }
                }

                Row(Modifier) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClickCache) {
                            Icon(Icons.Rounded.Download, null)
                        }
                        IconButton(onClickShare) {
                            Icon(Icons.Rounded.Outbox, null)
                        }
                    }
                    Box(Modifier.padding(start = 16.dp)) {
                        subjectCollectionActionButton()
                    }
                }
            }
        }

        SectionTitle(
            Modifier.padding(top = 8.dp),
//            actions = {
//                IconButton({}) {
//                    Icon(Icons.AutoMirrored.Rounded.List, null)
//                }
//            },
        ) {
            Text("选集")
        }

        Row {
            episodeCarousel(PaddingValues(horizontal = horizontalPadding))
        }
    }
}

@Composable
private fun ShareEpisodeDropdown(
    showMore: Boolean,
    onDismissRequest: () -> Unit,
    onClickCopyLink: () -> Unit,
    onClickDownload: () -> Unit,
    onClickOriginalPage: () -> Unit,
) {
    DropdownMenu(showMore, onDismissRequest) {
        DropdownMenuItem(
            text = { Text("复制磁力链接") },
            onClick = onClickCopyLink,
            leadingIcon = { Icon(Icons.Rounded.ContentCopy, null) },
        )
        DropdownMenuItem(
            text = { Text("使用其他应用打开") },
            onClick = onClickDownload,
            leadingIcon = { Icon(Icons.Rounded.Outbox, null) },
        )
        DropdownMenuItem(
            text = { Text("访问原始页面") },
            onClick = onClickOriginalPage,
            leadingIcon = { Icon(Icons.Rounded.ArrowOutward, null) },
        )
    }
}

@Stable
private fun Media.render(): String {
    val properties = this.properties
    return listOfNotNull(
        properties.resolution,
        properties.subtitleLanguageIds.joinToString("/") { renderSubtitleLanguage(it) }
            .takeIf { it.isNotBlank() },
        properties.size.takeIf { it != 0.bytes && it != Unspecified },
        properties.alliance,
    ).joinToString(" · ")
}

fun renderSubtitleLanguage(id: String): String {
    return when (id) {
        SubtitleLanguage.ChineseCantonese.id -> "粤语"
        SubtitleLanguage.ChineseSimplified.id -> "简中"
        SubtitleLanguage.ChineseTraditional.id -> "繁中"
        SubtitleLanguage.Japanese.id -> "日语"
        SubtitleLanguage.English.id -> "英语"
        else -> id
    }
}

fun renderResolution(id: String): String {
    return Resolution.tryParse(id)?.displayName ?: id
}

/**
 * 显示正在播放的那行字
 */
@Composable
private fun NowPlayingLabel(
    isPlaying: Media?,
    filename: String?,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        ProvideTextStyle(MaterialTheme.typography.labelMedium) {
            if (isPlaying != null) {
                Column {
                    Row {
                        Text(
                            "正在播放: ",
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            remember(isPlaying) { isPlaying.render() },
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }


                    if (filename != null) {
                        SelectionContainer {
                            Text(
                                filename,
                                Modifier.padding(top = 8.dp),
                                color = LocalContentColor.current.slightlyWeaken(),
                            )
                        }
                    }
                }
            } else {
                Text("请选择数据源")
            }
        }
    }
}
