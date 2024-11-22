/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItemsWithLifecycle
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.data.models.subject.FollowedSubjectInfo
import me.him188.ani.app.data.models.subject.subjectInfo
import me.him188.ani.app.data.models.trending.TrendingSubjectInfo
import me.him188.ani.app.domain.session.AuthState
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.navigation.SubjectDetailPreload
import me.him188.ani.app.ui.adaptive.AniTopAppBar
import me.him188.ani.app.ui.adaptive.AniTopAppBarDefaults
import me.him188.ani.app.ui.adaptive.NavTitleHeader
import me.him188.ani.app.ui.exploration.followed.FollowedSubjectsLazyRow
import me.him188.ani.app.ui.exploration.trends.TrendingSubjectsCarousel
import me.him188.ani.app.ui.foundation.animation.SharedTransitionKeys
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.layout.currentWindowAdaptiveInfo1
import me.him188.ani.app.ui.foundation.layout.isAtLeastMedium
import me.him188.ani.app.ui.foundation.layout.paneHorizontalPadding
import me.him188.ani.app.ui.foundation.session.SelfAvatar
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.search.isLoadingFirstPageOrRefreshing

@Stable
class ExplorationPageState(
    val authState: AuthState,
    selfInfoState: State<UserInfo?>,
    val trendingSubjectInfoPager: LazyPagingItems<TrendingSubjectInfo>,
    val followedSubjectsPager: Flow<PagingData<FollowedSubjectInfo>>,
) {
    val selfInfo by selfInfoState

    /**
     * 保存进入 SubjectDetailPage 时的 [SharedTransitionKeys]. 用于在从 SubjectDetailPage 返回时
     * 配置正确的 [renderInSharedTransitionScopeOverlay] 和动画.
     *
     * 无论是否使用 shared transition, 从 ExplorationPage 导航到 SubjectDetailPage 前需要更新此属性,
     * 否则从 SubjectDetailPage 返回会出现 overlay 动画错误.
     */
    var currentSharedTransitionKey: String? by mutableStateOf(null)

    val trendingSubjectsCarouselState = CarouselState(
        itemCount = {
            if (trendingSubjectInfoPager.isLoadingFirstPageOrRefreshing) {
                8
            } else {
                trendingSubjectInfoPager.itemCount
            }
        },
    )
    val followedSubjectsLazyRowState = LazyListState()


    val pageScrollState = ScrollState(0)
}

@Composable
fun ExplorationPage(
    state: ExplorationPageState,
    onSearch: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forPageContent(),
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AniThemeDefaults.pageContentBackgroundColor,
        topBar = {
            AniTopAppBar(
                title = { AniTopAppBarDefaults.Title("探索") },
                Modifier.fillMaxWidth(),
                actions = {
                    if (currentWindowAdaptiveInfo1().windowSizeClass.windowWidthSizeClass.isAtLeastMedium) {
                        IconButton(onClick = onClickSettings) {
                            Icon(Icons.Rounded.Settings, "设置")
                        }
                    }
                },
                avatar = { recommendedSize ->
                    SelfAvatar(state.authState, state.selfInfo, size = recommendedSize)
                },
                searchIconButton = {
                    IconButton(onSearch) {
                        Icon(Icons.Rounded.Search, "搜索")
                    }
                },
                searchBar = {
                    IconButton(onSearch) {
                        Icon(Icons.Rounded.Search, "搜索")
                    }
                },
                windowInsets = AniWindowInsets.forTopAppBarWithoutDesktopTitle(),
            )
        },
        contentWindowInsets = windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { topBarPadding ->
        val horizontalPadding = currentWindowAdaptiveInfo1().windowSizeClass.paneHorizontalPadding
        val horizontalContentPadding =
            PaddingValues(horizontal = horizontalPadding)

        val navigator = LocalNavigator.current
        Column(Modifier.padding(topBarPadding).verticalScroll(state.pageScrollState)) {
            NavTitleHeader(
                title = { Text("最高热度") },
                contentPadding = horizontalContentPadding,
            )

            TrendingSubjectsCarousel(
                state.trendingSubjectInfoPager,
                onClick = {
                    state.currentSharedTransitionKey =
                        SharedTransitionKeys.explorationTrendingSubjectToSubjectDetailCover(it.bangumiId)
                    navigator.navigateSubjectDetails(
                        subjectId = it.bangumiId,
                        preload = SubjectDetailPreload(
                            id = it.bangumiId,
                            name = it.nameCn,
                            coverUrl = it.imageLarge,
                        ),
                    )
                },
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
                carouselState = state.trendingSubjectsCarouselState,
            )

            NavTitleHeader(
                title = { Text("继续观看") },
                contentPadding = horizontalContentPadding,
            )
            val followedSubjectsPager = state.followedSubjectsPager.collectAsLazyPagingItemsWithLifecycle()
            FollowedSubjectsLazyRow(
                followedSubjectsPager,
                onClick = {
                    state.currentSharedTransitionKey =
                        SharedTransitionKeys.explorationFollowedSubjectToSubjectDetailCover(it.subjectInfo.subjectId)
                    navigator.navigateSubjectDetails(
                        subjectId = it.subjectInfo.subjectId,
                        preload = SubjectDetailPreload(
                            it.subjectInfo.subjectId,
                            it.subjectInfo.name,
                            it.subjectInfo.nameCn,
                            it.subjectInfo.imageLarge,
                        ),
                        sharedTransitionCoverKey = SharedTransitionKeys
                            .explorationFollowedSubjectToSubjectDetailCover(it.subjectInfo.subjectId),
                        sharedTransitionBoundKey = SharedTransitionKeys
                            .explorationFollowedSubjectToSubjectDetailBound(it.subjectInfo.subjectId),
                    )
                },
                onPlay = {
                    it.subjectProgressInfo.nextEpisodeIdToPlay?.let { it1 ->
                        navigator.navigateEpisodeDetails(
                            it.subjectInfo.subjectId,
                            it1,
                        )
                    }
                },
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
                lazyListState = state.followedSubjectsLazyRowState,
                currentSharedTransitionKey = state.currentSharedTransitionKey,
            )
        }
    }
}
