/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

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

package me.him188.ani.client.models


import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 
 *
 * @param sequelSubjects
 * @param seriesMainSubjectIds 
 * @param subjectId 
 */
@Serializable

data class AniSubjectRelations(

    @SerialName(value = "sequelSubjects") @Required val sequelSubjects: kotlin.collections.List<kotlin.Int>,

    @SerialName(value = "seriesMainSubjectIds") @Required val seriesMainSubjectIds: kotlin.collections.List<kotlin.Int>,

    @SerialName(value = "subjectId") @Required val subjectId: kotlin.Int

)

