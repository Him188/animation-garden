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


import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 *
 *
 * @param date
 * @param id
 * @param image
 * @param infobox
 * @param locked
 * @param name
 * @param nsfw
 * @param platform
 * @param redirect
 * @param summary
 * @param typeID
 */
@Serializable

data class BangumiNextSubject(

    @SerialName(value = "date") @Required val date: kotlin.String,

    @SerialName(value = "id") @Required val id: kotlin.Int,

    @SerialName(value = "image") @Required val image: kotlin.String,

    @SerialName(value = "infobox") @Required val infobox: kotlin.String,

    @SerialName(value = "locked") @Required val locked: kotlin.Boolean,

    @SerialName(value = "name") @Required val name: kotlin.String,

    @SerialName(value = "nsfw") @Required val nsfw: kotlin.Boolean,

    @SerialName(value = "platform") @Required val platform: kotlin.Int,

    @SerialName(value = "redirect") @Required val redirect: kotlin.Int,

    @SerialName(value = "summary") @Required val summary: kotlin.String,

    @SerialName(value = "typeID") @Required val typeID: kotlin.Int

)

