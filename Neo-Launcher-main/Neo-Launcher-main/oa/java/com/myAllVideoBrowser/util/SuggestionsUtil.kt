package com.myAllVideoBrowser.util

import com.myAllVideoBrowser.data.local.model.Suggestion
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SuggestionsUtils {
    companion object {
        fun getSuggestions(okHttpClient: OkHttpClient, input: String): Flowable<List<Suggestion>> {
            return Flowable.create({ emitter ->
                val encodedQuery = URLEncoder.encode(input, StandardCharsets.UTF_8.toString())
                val request = Request.Builder()
                    .url("https://suggestqueries.google.com/complete/search?client=firefox&q=$encodedQuery")
                    .build()
                val response = okHttpClient.newCall(request).execute()
                    .use { response -> response.body.string() }

                val result: ArrayList<Suggestion> = ArrayList()

                val jsn = JSONArray(response.toString())
                val suggestions = jsn.optJSONArray(1) ?: JSONArray()
                for (i in 0 until suggestions.length()) {
                    try {
                        val phrase = suggestions.getString(i)
                        result.add(Suggestion(content = phrase))
                    } catch (_: Throwable) {

                    }
                }
                emitter.onNext(result)
            }, BackpressureStrategy.LATEST)
        }
    }
}
