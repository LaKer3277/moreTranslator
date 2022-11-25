package com.tools.android.translator.ads

import android.content.Context
import android.util.Log
import com.tools.android.translator.App
import com.tools.android.translator.ads.body.Ad
import com.tools.android.translator.support.RemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created on 2022/4/26
 * Describe:
 */
object AdCenter: AdmobCenter(), CoroutineScope by MainScope() {

    private const val tag = "AdCenter"
    private val cacheAds = HashMap<String, Ad>()
    private val isRequesting = HashMap<String, String>()

    private fun resetRequesting(adPos: AdPos, add: Boolean) {
        if (add) {
            isRequesting[adPos.pos] = "yes"
        } else {
            isRequesting.remove(adPos.pos)
        }
    }

    @Synchronized
    fun getCache(adPos: AdPos): Ad? {
        return cacheAds.remove(adPos.pos)
    }

    @Synchronized
    fun add2cache(adPos: AdPos, ad: Ad) {
        cacheAds[adPos.pos] = ad
    }

    @Synchronized
    fun hasCached(adPos: AdPos): Boolean {
        Log.i(tag,"=hasCached=${cacheAds.containsKey(adPos.pos)}==")
        return cacheAds.containsKey(adPos.pos)
    }

    fun preloadAd(adPos: AdPos) {
        if (hasCached(adPos)) return
        Log.i(tag, "preload: ${adPos.pos}")
        loadAd(App.ins, adPos, object :AdsListener() {
            override fun onAdLoaded(ad: Ad) {
                add2cache(adPos, ad)
            }
        }, forceLoad = false)
    }

    fun loadAd(ctx: Context, adPos: AdPos, adsListener: AdsListener, justCache: Boolean = false, forceLoad: Boolean = true) {
        Log.i(tag, "load: ${adPos.pos}")
        val cache = getCache(adPos)
        if (cache != null) {
            cache.defineListener(adsListener)
            adsListener.onAdLoaded(cache)
            return
        }
        if (justCache) {
            adsListener.onAdError("noCache")
            return
        }

        if ((dailyClickUpper > 0 && AdConfig.ins.getClickCount() > dailyClickUpper)
            || dailyShownUpper > 0 && AdConfig.ins.getShownCount() > dailyShownUpper) {
            adsListener.onAdError("hasLimited")
            return
        }

        if (!forceLoad) {
            synchronized(isRequesting) {
                if (!isRequesting[adPos.pos].isNullOrEmpty()) return
            }
        }

        var configPos = adsConfig[adPos.pos]
        if (configPos == null || configPos.isEmpty()) {
            tryParseConfig()
        }
        configPos = adsConfig[adPos.pos]
        if (configPos == null || configPos.isEmpty()) {
            adsListener.onAdError("noConfig")
            return
        }
        val lists = arrayListOf<ConfigId>()
        lists.addAll(configPos.ids)

        resetRequesting(adPos, true)
        launch { traversalId(ctx.applicationContext, adPos, lists, adsListener) }
    }

    private fun traversalId(ctx: Context, adPos: AdPos, configIds: ArrayList<ConfigId>, adsListener: AdsListener) {
        if (configIds.isNullOrEmpty()) {
            resetRequesting(adPos, false)
            adsListener.onAdError("Request All Done")
            return
        }

        fun checkIt(ad: Ad?) {
            if (ad != null) {
                ad.defineListener(adsListener)
                adsListener.onAdLoaded(ad)
                resetRequesting(adPos, false)
                return
            }
            traversalId(ctx, adPos, configIds, adsListener)
        }

        val removeAt = configIds.removeAt(0)
        Log.i(tag, "requesting ad: ${adPos.pos} - ${removeAt.id}")
        when (removeAt.type) {
            "o" -> loadOpen(ctx, adPos, removeAt) {
                checkIt(it)
            }

            "i" -> loadInterstitial(ctx, adPos, removeAt) {

                checkIt(it)
            }

            "n" -> loadNative(ctx, adPos, removeAt) {
                checkIt(it)
            }

            else -> traversalId(ctx, adPos, configIds, adsListener)
        }
    }

    private val adsConfig = HashMap<String, ConfigPos>()
    private var dailyShownUpper = 30
    private var dailyClickUpper = 8

    @Synchronized
    private fun tryParseConfig() {
        val remoteAdConfig = RemoteConfig.ins.getAdsConfig()

        fun parsePosition(adPos: AdPos, jsonArray: JSONArray?) {
            if (jsonArray == null) return
            val listIds = arrayListOf<ConfigId>()
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.optJSONObject(i) ?: continue
                listIds.add(ConfigId(
                    id = json.optString("xmcd"),
                    type = json.optString("xmco"),
                    priority = json.optInt("xmcn")
                ))
            }
            listIds.sortBy { it.priority * -1 }
            adsConfig[adPos.pos] = ConfigPos(adPos, listIds)
        }

        try {
            val jsonObject = JSONObject(remoteAdConfig)
            dailyShownUpper = jsonObject.optInt("iTran_zks", 30)
            dailyClickUpper = jsonObject.optInt("iTran_ydj", 10)

            parsePosition(AdPos.MAIN, jsonObject.optJSONArray(AdPos.MAIN.pos))
            parsePosition(AdPos.OPEN, jsonObject.optJSONArray(AdPos.OPEN.pos))
            parsePosition(AdPos.TRANS, jsonObject.optJSONArray(AdPos.TRANS.pos))
            parsePosition(AdPos.CONNECT, jsonObject.optJSONArray(AdPos.CONNECT.pos))
            parsePosition(AdPos.RESULT, jsonObject.optJSONArray(AdPos.RESULT.pos))
            parsePosition(AdPos.SERVER_HOME, jsonObject.optJSONArray(AdPos.SERVER_HOME.pos))
            parsePosition(AdPos.BACK, jsonObject.optJSONArray(AdPos.BACK.pos))
        } catch (e: Exception) {

        }
    }
}