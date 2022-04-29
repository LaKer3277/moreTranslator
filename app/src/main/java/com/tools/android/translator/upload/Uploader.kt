package com.tools.android.translator.upload

import android.os.Build
import android.util.Base64
import com.tools.android.translator.App
import com.tools.android.translator.BuildConfig
import com.tools.android.translator.support.Devices
import com.tools.android.translator.upload.http.HttpClient
import com.tools.android.translator.upload.http.IHttpCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.HashMap

/**
 * Created on 2022/4/27
 * Describe:
 */
class Uploader: CoroutineScope by GlobalScope {

    companion object {
        val ins: Uploader by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { Uploader() }
        var hitIt = false
    }

    private val atomicUploaded = AtomicBoolean(false)
    private var localIP = App.ins.localUserIp
    fun doStart() {
        if (atomicUploaded.getAndSet(true)) return
        launch {
            acquireIp()
            delay(1000L)
            uploadAppendSession()
        }
    }

    private fun uploadAppendSession() {
        val selection = "wsxqazedcrfvbhiujmoknwsxqazedcrfvbhiujmoknwsxqazedcrfvbhiujmokn"
        val random = Random().nextInt(12)
        val demonsttems = selection.substring(random, random * 2)
        val indicrter = selection.substring(random, random * 3)
        val burgltesy = selection.substring(random * 2, random * 3)
        val coorddles = demonsttems + burgltesy

        val deviceId = Devices.getAndroidID(App.ins)
        val verName = BuildConfig.VERSION_NAME.split(".")
        val kilo = StringBuilder()
        for (element in verName) {
            kilo.append(element).append("864")
        }
        val httpUrl = "https://aabkv.com/dt?anticients=Android&clasings=${deviceId}&natris=fracnth&kilomicate=${kilo.substring(0, kilo.length - 3)}"
        val header = HashMap<String, String>().apply {
            put("comphead", Locale.getDefault().country)
            put("wricates", "0")
            put("ketitask", App.ins.deviceId)
            put("foings", localIP)
        }

        val bodyJSON = JSONObject()
        try {
            bodyJSON.put("apprapower", JSONObject().apply {
                put("corms", "")
                put("mansday", "")
                put("clasings", deviceId)
            })
            bodyJSON.put(
                "aioff",
                "${System.currentTimeMillis()}=&${App.ins.packageName}=&${App.ins.firstCountry}=&${Build.VERSION.RELEASE}"
            )
            bodyJSON.put("intenor", JSONObject().apply {
                put("effammer", Build.MODEL)
                put("enlishes", "")
                put("canvel", "")
            })
            bodyJSON.put("indicrter", indicrter)
            bodyJSON.put("coorddles", coorddles)
            bodyJSON.put("burgltesy", burgltesy)
            bodyJSON.put("demonsttems", demonsttems)
            bodyJSON.put("dogs", "")
            bodyJSON.put("beeep", "")
            bodyJSON.put("vesfiers", System.currentTimeMillis().toString())

            var bodyString = Base64.encodeToString(bodyJSON.toString().toByteArray(), Base64.NO_WRAP)
            bodyString = bodyString.replace("=", "")
            val fir = "gXzq32N3gaYa1qgSgT3B"
            val sec = "vXC1o40vwOPZEAZPxbsi"
            bodyString = bodyString.substring(0, 74) + fir + bodyString.substring(74, bodyString.length)
            bodyString += sec

            bodyString = bodyString.reversed()
            HttpClient.ins.postAsync(httpUrl, headers = header, params = bodyString, listener = object :IHttpCallback{
                override fun onSuccess(code: Int, msg: String?) {
                    hitIt = "explosion" == msg
                }

                override fun onFailed(code: Int, msg: String?) {
                }
            })
        } catch (e: Exception) {}
    }

    private fun acquireIp() {
        val http = "https://api.myip.com/"
        HttpClient.ins.getSync(http, object :IHttpCallback {
            override fun onSuccess(code: Int, msg: String?) {
                try {
                    val json = JSONObject(msg ?: "")
                    json.optString("ip").let {
                        localIP = it
                        App.ins.localUserIp = it
                    }
                    if (App.ins.firstCountry.isNullOrEmpty()) {
                        json.optString("cc").let {
                            App.ins.firstCountry = it
                        }
                    }
                } catch (e: Exception) {}
            }

            override fun onFailed(code: Int, msg: String?) {
            }
        })
    }
}