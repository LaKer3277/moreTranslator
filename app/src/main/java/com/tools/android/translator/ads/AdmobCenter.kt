package com.tools.android.translator.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_LEFT
import com.tools.android.translator.ads.body.Ad
import com.tools.android.translator.ads.body.AdmobInterstitial
import com.tools.android.translator.ads.body.AdmobNative
import com.tools.android.translator.ads.body.AdmobOpen
import com.tools.android.translator.server.ConnectServerManager
import com.tools.android.translator.tba.CommonJson
import com.tools.android.translator.tba.OkGoManager
import com.tools.android.translator.tba.TbaJson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Created on 2022/4/27
 * Describe:
 */
open class AdmobCenter {
    val loadAdIpMap= hashMapOf<String,String>()
    val loadAdCityMap= hashMapOf<String,String?>()

    fun loadOpen(ctx: Context, adPos: AdPos, configId: ConfigId, load: (ad: Ad?) -> Unit ) {
        val admobOpen = AdmobOpen(adPos, configId)
        val loadCallback = object :AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                load.invoke(null)
            }

            override fun onAdLoaded(p0: AppOpenAd) {
                Log.i("AdCenter", "load success : ${adPos.pos}")
                admobOpen.buildInAd(p0)
                setLoadAdIpCityName(adPos)
                p0.setOnPaidEventListener {
                    uploadAdEvent(ctx, adPos, it, p0.responseInfo, configId)
                }
                load.invoke(admobOpen)
            }
        }
        AppOpenAd.load(
            ctx.applicationContext,
            configId.id,
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            loadCallback
        )
    }

    fun loadInterstitial(ctx: Context, adPos: AdPos, configId: ConfigId, load: (ad: Ad?) -> Unit ) {
        val admobInterstitial = AdmobInterstitial(adPos, configId)
        InterstitialAd.load(ctx, configId.id, AdRequest.Builder().build(), object :InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
                Log.i("AdCenter", "load success : ${adPos.pos}")
                admobInterstitial.buildInAd(p0)
                setLoadAdIpCityName(adPos)
                p0.setOnPaidEventListener {
                    uploadAdEvent(ctx, adPos, it, p0.responseInfo, configId)
                }
                load.invoke(admobInterstitial)
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                load.invoke(null)
            }
        })
    }

    fun loadNative(ctx: Context, adPos: AdPos, configId: ConfigId, load: (ad: Ad?) -> Unit ) {
        val admobNative = AdmobNative(adPos, configId)
        admobNative.actLoadAdError = {
            load.invoke(null)
        }
        AdLoader.Builder(ctx, configId.id)
            .forNativeAd {
                Log.i("AdCenter", "load success : ${adPos.pos}")
                admobNative.buildInAd(it)
                setLoadAdIpCityName(adPos)
                it.setOnPaidEventListener { value->
                    uploadAdEvent(ctx, adPos, value, it.responseInfo,configId)
                }
                load.invoke(admobNative)
            }
            .withAdListener(admobNative.adListener)
            .withNativeAdOptions(NativeAdOptions.Builder().setAdChoicesPlacement(ADCHOICES_TOP_LEFT).build())
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    private fun uploadAdEvent(
        context: Context,
        adPos: AdPos,
        value: AdValue,
        responseInfo: ResponseInfo,
        configId: ConfigId
    ){
        GlobalScope.launch {
            TbaJson.getIp {
                val commonJson = CommonJson.getCommonJson(context, TbaJson.ip)
                commonJson.put("rill",value.valueMicros)
                commonJson.put("poisson",value.precisionType.toString())
                commonJson.put("smudge",loadAdIpMap[adPos.pos]?:"")
                commonJson.put("strophe",getCurrentIp())
                commonJson.put("bradley","drop")
                commonJson.put("cannibal","admob")
                commonJson.put("woodcut",configId.id)
                commonJson.put("ad_sense","")
                commonJson.put("glacial",adPos.pos)
                commonJson.put("tableaux", getAdType(configId))
                commonJson.put("playroom", MobileAds.getVersionString())
                commonJson.put("warp",getAdNetWork(responseInfo.mediationAdapterClassName))
                val begat = JSONObject()
                begat.put("itran_load_ip",loadAdCityMap[adPos.pos]?:"null")
                begat.put("itran_impress_ip",getCurrentCityName())

                commonJson.put("begat",begat)
                OkGoManager.uploadEvent(commonJson,false)
            }
        }

    }

    private fun getAdType(configId: ConfigId):String{
        when(configId.type){
            "o"->return "open"
            "i"->return "Interstitial"
            "n"->return "native"
        }
        return ""
    }

    private fun getAdNetWork(string: String):String{
        if(string.contains("facebook")) return "facebook"
        else if(string.contains("admob")) return "admob"
        return ""
    }

    private fun setLoadAdIpCityName(adPos: AdPos){
        loadAdIpMap[adPos.pos]=getCurrentIp()
        loadAdCityMap[adPos.pos]=getCurrentCityName()
    }

    private fun getCurrentCityName() = if(ConnectServerManager.isConnected()){
        if (ConnectServerManager.serverBean.isFast()){
            ConnectServerManager.fastBean.cheng
        }else{
            ConnectServerManager.serverBean.cheng
        }

    }else{
        "null"
    }

    private fun getCurrentIp()=if(ConnectServerManager.isConnected()){
        if (ConnectServerManager.serverBean.isFast()){
            ConnectServerManager.fastBean.ip
        }else{
            ConnectServerManager.serverBean.ip
        }
    }else{
        TbaJson.ip
    }
}