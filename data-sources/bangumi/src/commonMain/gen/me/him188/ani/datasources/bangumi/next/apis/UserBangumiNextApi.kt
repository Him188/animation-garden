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

package me.him188.ani.datasources.bangumi.next.apis

import me.him188.ani.datasources.bangumi.next.models.BangumiNextClearNoticeRequest
import me.him188.ani.datasources.bangumi.next.models.BangumiNextCurrentUser
import me.him188.ani.datasources.bangumi.next.models.BangumiNextErrorResponse
import me.him188.ani.datasources.bangumi.next.models.BangumiNextListNotice200Response
import me.him188.ani.datasources.bangumi.next.models.BangumiNextLoginRequestBody
import me.him188.ani.datasources.bangumi.next.models.BangumiNextUser

import me.him188.ani.datasources.bangumi.next.infrastructure.*
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.request.forms.formData
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json
import io.ktor.http.ParametersBuilder
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

open class UserBangumiNextApi : ApiClient {

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
     * 标记通知为已读
     * 标记通知为已读  不传id时会清空所有未读通知
     * @param bangumiNextClearNoticeRequest  (optional)
     * @return void
     */
    open suspend fun clearNotice(bangumiNextClearNoticeRequest: BangumiNextClearNoticeRequest? = null): HttpResponse<Unit> {

        val localVariableAuthNames = listOf<String>("CookiesSession")

        val localVariableBody = bangumiNextClearNoticeRequest

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.POST,
            "/p1/clear-notify",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return jsonRequest(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames,
        ).wrap()
    }


    /**
     *
     *
     * @return BangumiNextCurrentUser
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getCurrentUser(): HttpResponse<BangumiNextCurrentUser> {

        val localVariableAuthNames = listOf<String>("CookiesSession")

        val localVariableBody =
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/p1/me",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames,
        ).wrap()
    }


    /**
     * 获取未读通知
     *
     * @param limit max 40 (optional, default to 20)
     * @param unread  (optional)
     * @return BangumiNextListNotice200Response
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun listNotice(
        limit: kotlin.Int? = 20,
        unread: kotlin.Boolean? = null
    ): HttpResponse<BangumiNextListNotice200Response> {

        val localVariableAuthNames = listOf<String>("CookiesSession")

        val localVariableBody =
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        limit?.apply { localVariableQuery["limit"] = listOf("$limit") }
        unread?.apply { localVariableQuery["unread"] = listOf("$unread") }
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/p1/notify",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames,
        ).wrap()
    }


    /**
     *
     * 需要 [turnstile](https://developers.cloudflare.com/turnstile/get-started/client-side-rendering/)  next.bgm.tv 域名对应的 site-key 为 &#x60;0x4AAAAAAABkMYinukE8nzYS&#x60;  dev.bgm38.com 域名使用测试用的 site-key &#x60;1x00000000000000000000AA&#x60;
     * @param bangumiNextLoginRequestBody  (optional)
     * @return BangumiNextUser
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun login(bangumiNextLoginRequestBody: BangumiNextLoginRequestBody? = null): HttpResponse<BangumiNextUser> {

        val localVariableAuthNames = listOf<String>()

        val localVariableBody = bangumiNextLoginRequestBody

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.POST,
            "/p1/login",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
        )

        return jsonRequest(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames,
        ).wrap()
    }


    /**
     *
     * 登出
     * @return kotlin.String
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun logout(): HttpResponse<kotlin.String> {

        val localVariableAuthNames = listOf<String>()

        val localVariableBody =
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.POST,
            "/p1/logout",
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
