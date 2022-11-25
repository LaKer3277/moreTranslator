package com.tools.android.translator.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_LEFT
import com.tools.android.translator.ads.body.Ad
import com.tools.android.translator.ads.body.AdmobInterstitial
import com.tools.android.translator.ads.body.AdmobNative
import com.tools.android.translator.ads.body.AdmobOpen

/**
 * Created on 2022/4/27
 * Describe:
 */
open class AdmobCenter {

    fun loadOpen(ctx: Context, adPos: AdPos, configId: ConfigId, load: (ad: Ad?) -> Unit ) {
        val admobOpen = AdmobOpen(adPos, configId)
        val loadCallback = object :AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                load.invoke(null)
            }

            override fun onAdLoaded(p0: AppOpenAd) {
                admobOpen.buildInAd(p0)
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
                admobInterstitial.buildInAd(p0)
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
                admobNative.buildInAd(it)
                load.invoke(admobNative)
            }
            .withAdListener(admobNative.adListener)
            .withNativeAdOptions(NativeAdOptions.Builder().setAdChoicesPlacement(ADCHOICES_TOP_LEFT).build())
            .build()
            .loadAd(AdRequest.Builder().build())
    }

}