package com.tools.android.translator.ads

import android.content.Context
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

    private val cacheAds = HashMap<String, Ad>()

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
        return cacheAds.containsKey(adPos.pos)
    }

    fun preloadAd(adPos: AdPos) {
        if (hasCached(adPos)) return
        loadAd(App.ins, adPos, object :AdsListener() {
            override fun onAdLoaded(ad: Ad) {
                add2cache(adPos, ad)
            }
        })
    }

    fun loadAd(ctx: Context, adPos: AdPos, adsListener: AdsListener, justCache: Boolean = false) {
        val cache = getCache(adPos)
        if (cache != null) {
            cache.resetListener(adsListener)
            adsListener.onAdLoaded(cache)
            return
        }
        if (justCache) {
            adsListener.onAdError("noCache")
            return
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
        launch { traversalId(ctx.applicationContext, adPos, lists, adsListener) }
    }

    private fun traversalId(ctx: Context, adPos: AdPos, configIds: ArrayList<ConfigId>, adsListener: AdsListener) {
        if (configIds.isNullOrEmpty()) {
            adsListener.onAdError("Request All Done")
            return
        }

        fun checkIt(ad: Ad?) {
            if (ad != null) {
                adsListener.onAdLoaded(ad)
                return
            }
            traversalId(ctx, adPos, configIds, adsListener)
        }

        val removeAt = configIds.removeAt(0)
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
                    id = json.optString("ltof"),
                    type = json.optString("odkg"),
                    priority = json.optInt("nbm")
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
        } catch (e: Exception) {}
    }
}