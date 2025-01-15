/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
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

package me.him188.ani.client.apis

import me.him188.ani.client.models.AniLatestVersionInfo
import me.him188.ani.client.models.AniReleaseUpdatesDetailedResponse
import me.him188.ani.client.models.AniReleaseUpdatesResponse

import me.him188.ani.client.infrastructure.*
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.request.forms.formData
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json
import io.ktor.http.ParametersBuilder
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

open class UpdatesAniApi : ApiClient {

    constructor(
        baseUrl: String = ApiClient.BASE_URL,
        httpClientEngine: HttpClientEngine? = null,
        httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
        jsonSerializer: Json = ApiClient.JSON_DEFAULT
    ) : super(
        baseUrl = baseUrl,
        httpClientEngine = httpClientEngine,
        httpClientConfig = httpClientConfig,
        jsonBlock = jsonSerializer,
    )

    constructor(
        baseUrl: String,
        httpClient: HttpClient
    ) : super(baseUrl = baseUrl, httpClient = httpClient)

    /**
     * 获取可更新的版本详情
     * 返回所有大于当前版本的更新版本的详细信息，包括版本号、下载地址、发布时间以及更新内容。
     * @param clientVersion 客户端当前版本号。不合法的版本号会导致服务器返回 461 Invalid Client Version 错误。
     * @param clientPlatform 客户端平台，例：windows, android。不合法的值会导致服务器返回空的版本号列表。
     * @param clientArch 客户端架构，例：x86_64, aarch64。不合法的值会导致服务器返回空的版本号列表。
     * @param releaseClass 更新版本的发布类型，可选值：alpha, beta, rc, stable。不合法的发布类型会导致服务器返回 400 Bad Request 错误。
     * @return AniReleaseUpdatesDetailedResponse
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getDetailedUpdates(
        clientVersion: kotlin.String,
        clientPlatform: kotlin.String,
        clientArch: kotlin.String,
        releaseClass: kotlin.String
    ): HttpResponse<AniReleaseUpdatesDetailedResponse> {

        val localVariableAuthNames = listOf<String>()

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        clientVersion?.apply { localVariableQuery["clientVersion"] = listOf("$clientVersion") }
        clientPlatform?.apply { localVariableQuery["clientPlatform"] = listOf("$clientPlatform") }
        clientArch?.apply { localVariableQuery["clientArch"] = listOf("$clientArch") }
        releaseClass?.apply { localVariableQuery["releaseClass"] = listOf("$releaseClass") }
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/v1/updates/incremental/details",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames,
        ).wrap()
    }


    /**
     * 获取最新版本下载链接
     * 返回最新版本的下载链接及二维码及二维码，不包括版本更新信息。
     * @param releaseClass 版本的发布类型，可选值：alpha, beta, rc, stable，默认值为 stable。不合法的发布类型会导致服务器返回 400 Bad Request 错误。 (optional)
     * @return AniLatestVersionInfo
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getLatestVersion(releaseClass: kotlin.String? = null): HttpResponse<AniLatestVersionInfo> {

        val localVariableAuthNames = listOf<String>()

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        releaseClass?.apply { localVariableQuery["releaseClass"] = listOf("$releaseClass") }
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/v1/updates/latest",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames,
        ).wrap()
    }


    /**
     * 获取可更新的版本号列表
     * 返回所有大于当前版本的更新版本号。
     * @param clientVersion 客户端当前版本号。不合法的版本号会导致服务器返回 461 Invalid Client Version 错误。
     * @param clientPlatform 客户端平台，例：windows, android。不合法的值会导致服务器返回空的版本号列表。
     * @param clientArch 客户端架构，例：x86_64, aarch64。不合法的值会导致服务器返回空的版本号列表。
     * @param releaseClass 更新版本的发布类型，可选值：alpha, beta, rc, stable。不合法的发布类型会导致服务器返回 400 Bad Request 错误。
     * @return AniReleaseUpdatesResponse
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getUpdates(
        clientVersion: kotlin.String,
        clientPlatform: kotlin.String,
        clientArch: kotlin.String,
        releaseClass: kotlin.String
    ): HttpResponse<AniReleaseUpdatesResponse> {

        val localVariableAuthNames = listOf<String>()

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        clientVersion?.apply { localVariableQuery["clientVersion"] = listOf("$clientVersion") }
        clientPlatform?.apply { localVariableQuery["clientPlatform"] = listOf("$clientPlatform") }
        clientArch?.apply { localVariableQuery["clientArch"] = listOf("$clientArch") }
        releaseClass?.apply { localVariableQuery["releaseClass"] = listOf("$releaseClass") }
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/v1/updates/incremental",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames,
        ).wrap()
    }


}
