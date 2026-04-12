package com.myAllVideoBrowser.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.myAllVideoBrowser.data.local.room.entity.DownloadUrlsConverter
import com.myAllVideoBrowser.data.local.room.entity.FormatsConverter
import com.myAllVideoBrowser.data.local.room.entity.VideFormatEntityList
import com.myAllVideoBrowser.data.local.room.entity.VideoInfo
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

class RoomConverter {
    private val gson = Gson()
    private val formatsConverter = FormatsConverter()
    private val downloadUrlsConverter = DownloadUrlsConverter()

    @TypeConverter
    fun convertJsonToVideo(json: String): VideoInfo {
        if (json.isBlank()) {
            return VideoInfo()
        }

        return runCatching {
            parseVideoInfo(json)
        }.getOrElse {
            VideoInfo()
        }
    }

    @TypeConverter
    fun convertListVideosToJson(video: VideoInfo): String {
        return gson.toJson(
            JsonObject().apply {
                addProperty("id", video.id)
                addProperty("title", video.title)
                addProperty("ext", video.ext)
                addProperty("thumbnail", video.thumbnail)
                addProperty("duration", video.duration)
                addProperty("originalUrl", video.originalUrl)
                add("formats", gson.toJsonTree(video.formats))
                addProperty("isRegular", video.isRegularDownload)
                addProperty("isLive", video.isLive)
                addProperty("isDetectedBySuperX", video.isDetectedBySuperX)
                addProperty("downloadUrls", downloadUrlsConverter.fromSource(video.downloadUrls))
            }
        )
    }

    private fun parseVideoInfo(json: String): VideoInfo {
        val root = JsonParser.parseString(json)
        if (!root.isJsonObject) {
            return gson.fromJson(json, VideoInfo::class.java)
        }

        val jsonObject = root.asJsonObject
        val formats = parseFormats(jsonObject.get("formats"))
        val originalUrl = jsonObject.optString("originalUrl")
        val downloadUrls = parseDownloadUrls(jsonObject.get("downloadUrls"), formats)
        val fallbackUrl = originalUrl.ifBlank {
            downloadUrls.firstOrNull()?.url?.toString().orEmpty()
        }

        return VideoInfo(
            id = jsonObject.optString("id").ifBlank { VideoInfo().id },
            downloadUrls = downloadUrls,
            title = jsonObject.optString("title"),
            ext = jsonObject.optString("ext"),
            thumbnail = jsonObject.optString("thumbnail"),
            duration = jsonObject.optLong("duration"),
            originalUrl = fallbackUrl,
            formats = formats,
            isRegularDownload = jsonObject.optBoolean("isRegular"),
            isLive = jsonObject.optBoolean("isLive"),
            isDetectedBySuperX = jsonObject.optBoolean("isDetectedBySuperX")
        )
    }

    private fun parseFormats(formatsElement: JsonElement?): VideFormatEntityList {
        if (formatsElement == null || formatsElement.isJsonNull) {
            return VideFormatEntityList(emptyList())
        }

        return runCatching {
            when {
                formatsElement.isJsonObject -> {
                    formatsConverter.convertJSONStringToFormatList(formatsElement.toString())
                }

                formatsElement.isJsonArray -> {
                    val wrapped = JsonObject().apply { add("formats", formatsElement) }
                    formatsConverter.convertJSONStringToFormatList(wrapped.toString())
                }

                else -> VideFormatEntityList(emptyList())
            }
        }.getOrDefault(VideFormatEntityList(emptyList()))
    }

    private fun parseDownloadUrls(
        downloadUrlsElement: JsonElement?,
        formats: VideFormatEntityList
    ): List<Request> {
        val parsed = when {
            downloadUrlsElement == null || downloadUrlsElement.isJsonNull -> emptyList()
            downloadUrlsElement.isJsonPrimitive && downloadUrlsElement.asJsonPrimitive.isString -> {
                runCatching {
                    downloadUrlsConverter.toSource(downloadUrlsElement.asString)
                }.getOrDefault(emptyList())
            }

            downloadUrlsElement.isJsonArray -> {
                downloadUrlsElement.asJsonArray.mapNotNull { requestElement ->
                    parseLegacyRequest(requestElement)
                }
            }

            downloadUrlsElement.isJsonObject -> {
                listOfNotNull(parseLegacyRequest(downloadUrlsElement))
            }

            else -> emptyList()
        }

        if (parsed.isNotEmpty()) {
            return parsed
        }

        return formats.formats.mapNotNull { format ->
            val url = format.url?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            runCatching {
                Request.Builder()
                    .url(url)
                    .headers((format.httpHeaders ?: emptyMap()).toHeaders())
                    .get()
                    .build()
            }.getOrNull()
        }
    }

    private fun parseLegacyRequest(requestElement: JsonElement): Request? {
        if (!requestElement.isJsonObject) {
            return null
        }

        val requestObject = requestElement.asJsonObject
        val url = extractUrl(requestObject).ifBlank { return null }
        val method = requestObject.optString("method").ifBlank { "GET" }.uppercase(Locale.US)
        val headers = parseHeaders(requestObject.get("headers")).toHeaders()
        val bodyContent = extractBody(requestObject.get("body"))

        return runCatching {
            Request.Builder()
                .url(url)
                .headers(headers)
                .method(method, createRequestBody(method, bodyContent))
                .build()
        }.getOrNull()
    }

    private fun extractUrl(requestObject: JsonObject): String {
        val urlElement = requestObject.get("url") ?: return ""
        if (urlElement.isJsonPrimitive) {
            return urlElement.asString
        }

        if (!urlElement.isJsonObject) {
            return ""
        }

        val nested = urlElement.asJsonObject
        return nested.optString("url").ifBlank {
            nested.entrySet()
                .mapNotNull { (_, value) ->
                    value.takeIf { it.isJsonPrimitive && it.asString.startsWith("http") }?.asString
                }
                .firstOrNull()
                .orEmpty()
        }
    }

    private fun parseHeaders(headersElement: JsonElement?): Map<String, String> {
        if (headersElement == null || headersElement.isJsonNull) {
            return emptyMap()
        }

        if (headersElement.isJsonPrimitive && headersElement.asJsonPrimitive.isString) {
            return runCatching {
                parseHeaders(JsonParser.parseString(headersElement.asString))
            }.getOrDefault(emptyMap())
        }

        if (!headersElement.isJsonObject) {
            return emptyMap()
        }

        val headersObject = headersElement.asJsonObject
        val namesAndValues = headersObject.get("namesAndValues")
        if (namesAndValues != null && namesAndValues.isJsonArray) {
            val flattened = namesAndValues.asJsonArray.mapNotNull { element ->
                element.takeIf { it.isJsonPrimitive }?.asString
            }
            return flattened.chunked(2).associate { chunk ->
                chunk.first() to chunk.getOrElse(1) { "" }
            }
        }

        return headersObject.entrySet().associate { (key, value) ->
            key to if (value.isJsonNull) "" else value.asString
        }
    }

    private fun extractBody(bodyElement: JsonElement?): String {
        if (bodyElement == null || bodyElement.isJsonNull) {
            return ""
        }

        return when {
            bodyElement.isJsonPrimitive -> bodyElement.asString
            bodyElement.isJsonObject -> bodyElement.asJsonObject.optString("content")
            else -> ""
        }
    }

    private fun createRequestBody(method: String, bodyContent: String): okhttp3.RequestBody? {
        val normalizedMethod = method.uppercase(Locale.US)
        val requiresBody = normalizedMethod in setOf("POST", "PUT", "PATCH", "PROPPATCH", "REPORT")
        val normalizedContent = bodyContent.takeIf { it.isNotBlank() }

        return when {
            normalizedContent != null -> normalizedContent.toRequestBody(null)
            requiresBody -> ByteArray(0).toRequestBody(null)
            else -> null
        }
    }

    private fun JsonObject.optString(name: String): String {
        val value = get(name) ?: return ""
        return if (value.isJsonNull) "" else runCatching { value.asString }.getOrDefault("")
    }

    private fun JsonObject.optLong(name: String): Long {
        val value = get(name) ?: return 0L
        return if (value.isJsonNull) 0L else runCatching { value.asLong }.getOrDefault(0L)
    }

    private fun JsonObject.optBoolean(name: String): Boolean {
        val value = get(name) ?: return false
        return if (value.isJsonNull) false else runCatching { value.asBoolean }.getOrDefault(false)
    }
}
