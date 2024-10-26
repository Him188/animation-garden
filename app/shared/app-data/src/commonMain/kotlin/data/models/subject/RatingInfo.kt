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
import androidx.room.Embedded
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class RatingInfo(
    val rank: Int,
    val total: Int,
    @Embedded(prefix = "count_")
    val count: RatingCounts,
    val score: String, // big decimal
) {
    val scoreFloat get() = score.toFloat()

    companion object {
        @Stable
        val Empty = RatingInfo(0, 0, RatingCounts.Zero, "0")
    }
}

@Serializable
@Immutable
class RatingCounts(
    @SerialName(value = "1") val s1: Int = 0,
    @SerialName(value = "2") val s2: Int = 0,
    @SerialName(value = "3") val s3: Int = 0,
    @SerialName(value = "4") val s4: Int = 0,
    @SerialName(value = "5") val s5: Int = 0,
    @SerialName(value = "6") val s6: Int = 0,
    @SerialName(value = "7") val s7: Int = 0,
    @SerialName(value = "8") val s8: Int = 0,
    @SerialName(value = "9") val s9: Int = 0,
    @SerialName(value = "10") val s10: Int = 0
) {
    fun get(score: Int): Int {
        return when (score) {
            1 -> s1
            2 -> s2
            3 -> s3
            4 -> s4
            5 -> s5
            6 -> s6
            7 -> s7
            8 -> s8
            9 -> s9
            10 -> s10
            else -> {
                throw IllegalArgumentException("Invalid score: $score")
            }
        }
    }

    companion object {
        @Stable
        val Zero = RatingCounts()
    }
}
