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

import me.him188.ani.datasources.bangumi.next.models.BangumiNextGetSubjectEpisodeComments200ResponseInnerAllOfUser

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 *
 *
 * @param comment
 * @param rate
 * @param updatedAt
 * @param user
 */
@Serializable

data class BangumiNextSubjectInterestCommentListInner(

    @SerialName(value = "comment") @Required val comment: kotlin.String,

    @SerialName(value = "rate") @Required val rate: kotlin.Int,

    @SerialName(value = "updatedAt") @Required val updatedAt: kotlin.Int,

    @SerialName(value = "user") @Required val user: BangumiNextGetSubjectEpisodeComments200ResponseInnerAllOfUser?

)

