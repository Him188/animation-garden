package me.him188.ani.app.ui.settings.rendering

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.Res
import me.him188.ani.app.acg_rip
import me.him188.ani.app.bangumi
import me.him188.ani.app.data.source.media.MediaCacheManager.Companion.LOCAL_FS_MEDIA_SOURCE_ID
import me.him188.ani.app.dmhy
import me.him188.ani.app.mikan
import me.him188.ani.app.mxdongman
import me.him188.ani.app.ntdm
import me.him188.ani.app.nyafun
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.datasources.acgrip.AcgRipMediaSource
import me.him188.ani.datasources.bangumi.BangumiSubjectProvider
import me.him188.ani.datasources.dmhy.DmhyMediaSource
import me.him188.ani.datasources.ikaros.IkarosMediaSource
import me.him188.ani.datasources.jellyfin.EmbyMediaSource
import me.him188.ani.datasources.jellyfin.JellyfinMediaSource
import me.him188.ani.datasources.mikan.MikanCNMediaSource
import me.him188.ani.datasources.mikan.MikanMediaSource
import me.him188.ani.datasources.mxdongman.MxdongmanMediaSource
import me.him188.ani.datasources.ntdm.NtdmMediaSource
import me.him188.ani.datasources.nyafun.NyafunMediaSource
import org.jetbrains.compose.resources.painterResource


/*
 * !! 提示: 如果你的 Res 没有找到, 在项目根目录执行以下命令即可: 
 *     ./gradlew build
 */

@Stable
fun renderMediaSource(
    id: String
): String = when (id) {
    DmhyMediaSource.ID -> "動漫花園"
    AcgRipMediaSource.ID -> "ACG.RIP"
    MikanMediaSource.ID -> "Mikan"
    MikanCNMediaSource.ID -> "Mikan (中国大陆)"
    BangumiSubjectProvider.ID -> "Bangumi"
    NyafunMediaSource.ID -> "Nyafun"
    MxdongmanMediaSource.ID -> "MX 动漫"
    NtdmMediaSource.ID -> "NT动漫"
    JellyfinMediaSource.ID -> "Jellyfin"
    EmbyMediaSource.ID -> "Emby"
    LOCAL_FS_MEDIA_SOURCE_ID -> "本地"
    else -> id
}

@Stable
fun renderMediaSourceDescription(
    id: String
): String? = when (id) {
    DmhyMediaSource.ID -> "dmhy.org"
    AcgRipMediaSource.ID -> "acg.rip"
    MikanMediaSource.ID -> "mikanani.me"
    MikanCNMediaSource.ID -> "mikanime.tv"
    BangumiSubjectProvider.ID -> "bgm.tv"
    NyafunMediaSource.ID -> "nyafun.net"
    MxdongmanMediaSource.ID -> "mxdm4.com"
    IkarosMediaSource.ID -> "ikaros.run"
    NtdmMediaSource.ID -> "ntdm.tv"
    LOCAL_FS_MEDIA_SOURCE_ID -> null
    else -> null
}

@Composable
fun getMediaSourceIconResource(
    id: String?
): Painter? {
    if (LocalIsPreviewing.current) { // compose resources does not support preview
        return null
    }
    return when (id) {
        DmhyMediaSource.ID -> painterResource(Res.drawable.dmhy)
        AcgRipMediaSource.ID -> painterResource(Res.drawable.acg_rip)
        MikanMediaSource.ID, MikanCNMediaSource.ID -> painterResource(Res.drawable.mikan)
        BangumiSubjectProvider.ID -> painterResource(Res.drawable.bangumi)
        NyafunMediaSource.ID -> painterResource(Res.drawable.nyafun)
        MxdongmanMediaSource.ID -> painterResource(Res.drawable.mxdongman)
        NtdmMediaSource.ID -> painterResource(Res.drawable.ntdm)
        JellyfinMediaSource.ID -> rememberVectorPainter(Icons.Rounded.Jellyfin)
        EmbyMediaSource.ID -> rememberVectorPainter(Icons.Rounded.Emby)
        else -> null
    }
}

@Composable
fun MediaSourceIcon(
    id: String,
    modifier: Modifier = Modifier,
    url: String? = null,
) {
    if (url != null) {
        AsyncImage(
            url,
            null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center,
        )
    } else {
        val ic = getMediaSourceIconResource(id)
        Image(
            ic
                ?: rememberVectorPainter(Icons.Rounded.DisplaySettings),
            null,
            modifier,
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center,
            colorFilter = if (ic == null) ColorFilter.tint(MaterialTheme.colorScheme.onSurface) else null,
        )
    }
}

/**
 * 宽度不固定
 */
@Composable
fun SmallMediaSourceIcon(
    id: String?,
    modifier: Modifier = Modifier,
    allowText: Boolean = true,
) {
    Box(modifier.clip(MaterialTheme.shapes.extraSmall).height(24.dp)) {
        val icon = getMediaSourceIconResource(id)
        if (icon == null) {
            if (allowText && id != null) {
                Text(
                    renderMediaSource(id),
                    Modifier.height(24.dp),
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            } else {
                Icon(Icons.Rounded.DisplaySettings, id)
            }
        } else {
            Image(icon, null, Modifier.size(24.dp))
        }
    }
}
