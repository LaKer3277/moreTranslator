package com.tools.android.translator.upload.http

import com.tools.android.translator.App
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Created on 2022/4/27
 * Describe:
 */
class HttpClient {

    companion object {
        val ins: HttpClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            HttpClient()
        }
    }

    private var mClient: OkHttpClient
    private val MEDIA_TYPE_JSON = "application/json; charset=utf-8"
    private val MEDIA_TYPE_TEXT = "text/plain; charset=utf-8"

    init {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
        builder.addNetworkInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request().newBuilder()
                    .addHeader("Connection", "close")
                    .build()
                return chain.proceed(request)
            }
        })
        mClient = if (App.isRelease) {
            builder.build()
        } else {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build()
        }
    }

    fun postAsync(url: String, headers: HashMap<String, String>? = null, params: String? = null, paramsType: String = MEDIA_TYPE_JSON, listener: IHttpCallback) {
        val builder = Request.Builder().url(url)
        headers?.forEach { (t, u) -> builder.header(t, u) }
        params?.let {
            builder.post(it.toRequestBody(paramsType.toMediaTypeOrNull()))
        }
        var response: Response? = null
        try {
            response = mClient.newCall(builder.build()).execute()
            val bodyString = response.body?.string()
            if (response.isSuccessful) {
                listener.onSuccess(response.code, bodyString)
            } else {
                listener.onFailed(response.code, bodyString)
            }
        } catch (e: Exception) {
            listener.onFailed(-1, "exception")
        } finally {
            response?.body?.close()
        }
    }

    fun getSync(url: String, listener: IHttpCallback) {
        val builder = Request.Builder().url(url)
        var response: Response? = null
        try {
            //将请求添加到请求队列等待执行，并返回执行后的Response对象
            response = mClient.newCall(builder.build()).execute()
            //获取Http Status Code.其中200表示成功
            if (response.code == 200) {
                //这里需要注意，response.body().string()是获取返回的结果，此句话只能调用一次，再次调用获得不到结果。
                //所以先将结果使用result变量接收
                listener.onSuccess(response.code, response.body?.string())
            } else {
                listener.onFailed(response.code, response.body?.string())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            response?.body?.close()
        }
    }

}