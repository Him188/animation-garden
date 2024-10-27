/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.models.subject

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Tag(
    val name: String,
    val count: Int,
)

/**
 * 是否为公共标签.
 */
@Stable
val Tag.isCanonical: Boolean
    get() = CanonicalTagKind.matchOrNull(name) != null

/**
 * 获取标签的种类, 如果不是公共标签则返回 `null`.
 */
@Stable
val Tag.kind: CanonicalTagKind?
    get() = CanonicalTagKind.matchOrNull(name)

/**
 * Bangumi 定义的公共标签种类.
 */
// https://bgm.tv/wiki/tag/list
// Generated by GPT-4o
// Use list because we want to keep the order
@Immutable
sealed class CanonicalTagKind(val values: List<String>) {
    /**
     * 分类
     */
    @Immutable
    data object Category : CanonicalTagKind(
        listOf("短片", "剧场版", "TV", "OVA", "MV", "CM", "WEB", "PV", "动态漫画"),
    )

    /**
     * 来源
     */
    @Immutable
    data object Source : CanonicalTagKind(
        listOf("原创", "漫画改", "游戏改", "小说改"),
    )

    /**
     * 类型
     */
    @Immutable
    data object Genre : CanonicalTagKind(
        listOf(
            "科幻", "喜剧", "百合", "校园", "惊悚", "后宫", "机战", "悬疑", "恋爱", "奇幻",
            "推理", "运动", "耽美", "音乐", "战斗", "冒险", "萌系", "穿越", "玄幻", "乙女",
            "恐怖", "历史", "日常", "剧情", "武侠", "美食", "职场",
        ),
    )

    /**
     * 地区
     */
    @Immutable
    data object Region : CanonicalTagKind(
        listOf(
            "欧美", "日本", "美国", "中国", "法国", "韩国", "俄罗斯", "英国",
            "苏联", "香港", "捷克", "台湾",
        ),
    )

    /**
     * 分级
     */
    @Immutable
    data object Rating : CanonicalTagKind(
        listOf("R18"),
    )

    /**
     * 受众
     */
    @Immutable
    data object Audience : CanonicalTagKind(
        listOf("BL", "GL", "子供向", "女性向", "少女向", "少年向", "青年向"),
    )

    /**
     * 设定
     */
    @Immutable
    data object Setting : CanonicalTagKind(
        listOf(
            "魔法少女", "超能力", "偶像", "网游", "末世", "乐队",
            "赛博朋克", "宫廷", "都市", "异世界", "性转", "龙傲天", "凤傲天",
        ),
    )

    /**
     * 角色
     */
    @Immutable
    data object Character : CanonicalTagKind(
        listOf(
            "制服", "兽耳", "伪娘", "吸血鬼", "妹控", "萝莉", "傲娇", "女仆", "巨乳", "电波",
            "动物", "正太", "兄控", "僵尸", "群像", "美少女", "美少年",
        ),
    )

    /**
     * 情绪
     */
    @Immutable
    data object Emotion : CanonicalTagKind(
        listOf("热血", "治愈", "温情", "催泪", "纯爱", "友情", "致郁"),
    )

    /**
     * 技术
     */
    @Immutable
    data object Technology : CanonicalTagKind(
        listOf("黑白", "3D", "水墨", "定格", "粘土", "剪纸", "转描", "三渲二"),
    )

    /**
     * 系列
     */
    @Immutable
    data object Series : CanonicalTagKind(
        listOf(
            "高达", "东方", "Fate", "空之境界", "柯南", "光之美少女", "哆啦A梦",
            "物语系列", "刀剑神域", "进击的巨人",
        ),
    )

    @Stable
    companion object {
        @Stable
        val entries by lazy {
            listOf(
                Category, Source, Genre, Region, Rating, Audience,
                Setting, Character, Emotion, Technology, Series,
            )
        }

        // A map to directly associate tags with their corresponding CanonicalTagKind for fast querying
        private val tagMap: Map<String, CanonicalTagKind> by lazy {
            val entries = entries
            buildMap {
                for (kind in entries) {
                    for (value in kind.values) {
                        put(value, kind)
                    }
                }
            }
        }

        /**
         * 获取匹配的 [CanonicalTagKind], 如果没有匹配则返回 `null`.
         */
        fun matchOrNull(tag: String): CanonicalTagKind? = tagMap[tag]
    }
}
