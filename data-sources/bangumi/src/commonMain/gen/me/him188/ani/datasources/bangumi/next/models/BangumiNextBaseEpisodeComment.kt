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
 * @param content
 * @param createdAt
 * @param creatorID
 * @param epID
 * @param id
 * @param relatedID
 * @param state
 * @param user
 */
@Serializable

data class BangumiNextBaseEpisodeComment(

    @SerialName(value = "content") @Required val content: kotlin.String,

    @SerialName(value = "createdAt") @Required val createdAt: kotlin.Int,

    @SerialName(value = "creatorID") @Required val creatorID: kotlin.Int,

    @SerialName(value = "epID") @Required val epID: kotlin.Int,

    @SerialName(value = "id") @Required val id: kotlin.Int,

    @SerialName(value = "relatedID") @Required val relatedID: kotlin.Int,

    @SerialName(value = "state") @Required val state: kotlin.Int,

    @SerialName(value = "user") @Required val user: BangumiNextGetSubjectEpisodeComments200ResponseInnerAllOfUser?

)

