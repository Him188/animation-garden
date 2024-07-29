/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.datasources.api.topic.titles

import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FrameRate
import me.him188.ani.datasources.api.topic.MediaOrigin
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage

/**
 * 只解析剧集, 分辨率等必要信息, 不解析标题. 拥有更高正确率
 */
class LabelFirstRawTitleParser : RawTitleParser() {
    override fun parse(
        text: String,
        allianceName: String?,
        builder: ParsedTopicTitle.Builder,
    ) {
        Session(builder).run {
            val words = mutableListOf<String>()
            for (s in text.splitWords()) {
                if (s.isBlank()) continue
                if (newAnime.matches(s)) {
                    builder.tags.add(s)
                    continue
                }
                val word = s.remove("招募").remove("招新").trim()
                words.add(word)
            }

            // 第一遍, 解析剧集, 分辨率, 字幕等
            for (word in words) {
                parseWord(word)
            }

            // 第二遍, 如果没有解析到剧集, 找是不是有 "BDRip", 判定为季度全集
            if (builder.episodeRange == null) {
                words.forEach { word ->
                    if (word.contains("BD", ignoreCase = true)
                        || word.contains("Blu-Ray", ignoreCase = true)
                    ) {
                        builder.episodeRange = EpisodeRange.unknownSeason()
                    }
                }
            }
        }
        return
    }

    class Session(
        private val builder: ParsedTopicTitle.Builder,
    ) {
        fun parseWord(word: String): Boolean {
            var anyMatched = false
            anyMatched = anyMatched or word.parseSubtitleLanguages()
            anyMatched = anyMatched or word.parseResolution()
            anyMatched = anyMatched or word.parseFrameRate()
            anyMatched = anyMatched or word.parseEpisode()
            anyMatched = anyMatched or word.parseMediaOrigin()

            return anyMatched
        }


        private fun String.parseSubtitleLanguages(): Boolean {
            var any = false
            if (this.splitToSequence(" ").any { it.equals("Baha", ignoreCase = true) }
                && builder.subtitleLanguages.isEmpty()) {
                builder.subtitleLanguages.add(SubtitleLanguage.ChineseTraditional)
            }
            for (entry in SubtitleLanguage.matchableEntries) {
                if (entry.matches(this)) {
                    builder.subtitleLanguages.add(entry)
                    any = true
                }
            }
            return any
        }

        private fun String.parseResolution(): Boolean {
            return Resolution.tryParse(this)?.let {
                builder.resolution = it
            } != null
        }

        private fun String.parseFrameRate(): Boolean {
            return FrameRate.tryParse(this)?.let {
                builder.frameRate = it
            } != null
        }

        private fun String.parseMediaOrigin(): Boolean {
            return MediaOrigin.tryParse(this)?.let {
                builder.mediaOrigin = it
            } != null
        }

        private fun String.parseEpisode(): Boolean {
            if (this.contains("x264", ignoreCase = true)
                || this.contains("x265", ignoreCase = true)
            ) return false

            val str = episodeRemove.fold(this) { acc, regex -> acc.remove(regex) }
            str.toFloatOrNull()?.let {
                builder.episodeRange = EpisodeRange.single(str)
                return true
            }
//            collectionPattern.find(str)?.let { result ->
//                val startGroup = result.groups["start"]
//                val endGroup = result.groups["end"]
//                val extraGroup = result.groups["extra"]
//
//                if (extraGroup == null && (startGroup == null || endGroup == null)) {
//                    return@let
//                }
//                val start = startGroup?.value
//                val end = endGroup?.value
//
//                var range: EpisodeRange
//                if (start != null && end != null) {
//                    start.getPrefix()?.let { prefix ->
//                        if (!end.startsWith(prefix)) {
//                            // "SP1-5"
//                            builder.episodeRange = EpisodeRange.range(start, prefix + end)
//                            return true
//                        }
//                    }
//
//                    if (end.startsWith("0") && !start.startsWith("0")) {
//                        // "Hibike! Euphonium 3 - 02"
//                        builder.episodeRange = EpisodeRange.single(EpisodeSort(end))
//                        return true
//                    }
//
//                    range = EpisodeRange.range(start, end)
//                } else {
//                    range = EpisodeRange.empty()
//                }
//
//                if (extraGroup != null) {
//                    for (extra in result.groups.indexOf(extraGroup)..<result.groups.size) {
//                        range = EpisodeRange.combined(
//                            range,
//                            EpisodeRange.single(EpisodeSort(result.groups[extra]!!.value.removePrefix("+")))
//                        )
//                    }
//                }
//                builder.episodeRange = range
//                return true
//            }
            collectionPattern.find(str)?.let { result ->
                val start = result.groups["start"]?.value ?: return@let
                val end = result.groups["end"]?.value ?: return@let
                start.getPrefix()?.let { prefix ->
                    if (!end.startsWith(prefix)) {
                        // "SP1-5"
                        builder.episodeRange = EpisodeRange.range(start, prefix + end)
                        return true
                    }
                }

                if (end.startsWith("0") && !start.startsWith("0")) {
                    // "Hibike! Euphonium 3 - 02"
                    builder.episodeRange = EpisodeRange.single(EpisodeSort(end))
                    return true
                }

                val extra = result.groups["extra"]?.value
                if (extra != null) {
                    builder.episodeRange = EpisodeRange.combined(
                        EpisodeRange.range(start, end),
                        EpisodeRange.single(EpisodeSort(extra.removePrefix("+"))),
                    )
                } else {
                    builder.episodeRange = EpisodeRange.range(start, end)
                }
                return true
            }
            seasonPattern.find(str)?.let { result ->
                builder.episodeRange =
                    EpisodeRange.combined(
                        result.groups.drop(1).mapNotNull { group ->
                            group?.value?.removePrefix("+")?.takeIf { it.isNotBlank() }
                        }.map {
                            if (it.startsWith("S", ignoreCase = true) && !it.startsWith("SP", ignoreCase = true)) {
                                EpisodeRange.season(it.drop(1).toIntOrNull())
                            } else {
                                EpisodeRange.single(it)
                            }
                        },
                    )
                return true
            }
            if (str.contains("SP", ignoreCase = true) // 包括 "Special"
                || str.contains("OVA", ignoreCase = true)
                || str.contains("小剧场")
                || str.contains("特别篇")
                || str.contains("番外篇")
                || str.contains("OAD", ignoreCase = true)
            ) {
                builder.episodeRange = EpisodeRange.single(this)
                return true
            }
            return false
        }

    }
}

private fun String.getPrefix(): String? {
    if (this.isEmpty()) return null
    if (this[0].isDigit()) return null
    val index = this.indexOfFirst { it.isDigit() }
    if (index == -1) return null
    return this.substring(0, index)
}

// 第02話V2版
// 02V2
private val episodeRemove = listOf(
    Regex("""第"""),
    Regex("""_?(?:完|END)|\(完\)""", RegexOption.IGNORE_CASE),
    Regex("""[话集話]"""),
    Regex("""_?v[0-9]""", RegexOption.IGNORE_CASE),
    Regex("""版"""),
)

private val newAnime = Regex("(?:★?|★(.*)?)([0-9]|[一二三四五六七八九十]{0,4}) ?[月年] ?(?:新番|日剧)★?")

// 性能没问题, 测了一般就 100 steps
@Suppress("RegExpRedundantEscape") // required on android
private val brackets =
    Regex("""\[(?<v1>.+?)\]|\((?<v2>.+?)\)|\{(?<v3>.+?)\}|【(?<v4>.+?)】|（(?<v5>.+?)）|「(?<v6>.+?)」|『(?<v7>.+?)』""")

//private val brackets = listOf(
//    "[" to "]",
//    "【" to "】",
//    "（" to "）",
//    "(" to ")",
//    "『" to "』",
//    "「" to "」",
//    "〖" to "〗",
//    "〈" to "〉",
//    "《" to "》",
//    "〔" to "〕",
//    "〘" to "〙",
//    "〚" to "〛",
//)

private val collectionPattern = Regex(
//    """((?<start>(?:SP)?\d{1,4})\s?(?:-{1,2}|~|～)\s?(?<end>\d{1,4}))?(?:TV|BDrip|BD)?(?<extra>\+.+)*""",
    """(?<start>(?:SP)?\d{1,4})\s?(?:-{1,2}|~|～)\s?(?<end>\d{1,4})(?:TV|BDrip|BD)?(?<extra>\+.+)?""",
    RegexOption.IGNORE_CASE,
)

private val seasonPattern = Regex("""(S\d)(?:(\+S\d)|(\+S\w)|(\+\w+))*""", RegexOption.IGNORE_CASE)

private fun String.remove(str: String) = replace(str, "", ignoreCase = true)
private fun String.remove(regex: Regex) = replace(regex) { "" }

private val DEFAULT_SPLIT_WORDS_DELIMITER = charArrayOf('/', '\\', '|', ' ')

internal fun String.splitWords(vararg delimiters: Char = DEFAULT_SPLIT_WORDS_DELIMITER): Sequence<String> {
    val text = this
    return sequence {
        var index = 0
        for (result in brackets.findAll(text)) {
            if (index < result.range.first) {
                yieldAll(
                    text.substring(index until result.range.first)
                        .splitToSequence(delimiters = delimiters),
                )
            }
            index = result.range.last + 1


            val groups = result.groups
            val tag = groups["v1"]
                ?: groups["v2"]
                ?: groups["v3"]
                ?: groups["v4"]
                ?: groups["v5"]
                ?: groups["v6"]
                ?: groups["v7"]
            // can be "WebRip 1080p HEVC-10bit AAC" or "简繁内封字幕"
            yield(tag!!.value)
        }
        if (index < text.length) {
            yieldAll(
                text.substring(index until text.length)
                    .splitToSequence(delimiters = delimiters),
            )
        }
    }
}
