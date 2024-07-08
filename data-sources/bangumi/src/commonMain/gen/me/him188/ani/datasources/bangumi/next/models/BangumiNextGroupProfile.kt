/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package me.him188.ani.datasources.bangumi.next.models

import me.him188.ani.datasources.bangumi.next.models.BangumiNextGroup
import me.him188.ani.datasources.bangumi.next.models.BangumiNextGroupMember
import me.him188.ani.datasources.bangumi.next.models.BangumiNextTopic

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 *
 *
 * @param group
 * @param inGroup 是否已经加入小组
 * @param recentAddedMembers
 * @param topics
 * @param totalTopics
 */
@Serializable

data class BangumiNextGroupProfile(

    @SerialName(value = "group") @Required val group: BangumiNextGroup,

    /* 是否已经加入小组 */
    @SerialName(value = "inGroup") @Required val inGroup: kotlin.Boolean,

    @SerialName(value = "recentAddedMembers") @Required val recentAddedMembers: kotlin.collections.List<BangumiNextGroupMember>,

    @SerialName(value = "topics") @Required val topics: kotlin.collections.List<BangumiNextTopic>,

    @SerialName(value = "totalTopics") @Required val totalTopics: kotlin.Int

)

