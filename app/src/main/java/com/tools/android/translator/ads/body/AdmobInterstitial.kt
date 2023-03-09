package com.tools.android.translator.ads.body

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.tools.android.translator.ads.AdCenter
import com.tools.android.translator.ads.AdPos
import com.tools.android.translator.ads.ConfigId
import com.tools.android.translator.support.ReferrerManager

/**
 * Created on 2022/4/27
 * Describe:
 */
class AdmobInterstitial(adPos: AdPos, configId: ConfigId): InterstitialAds(adPos, configId) {

    private var mInterstitial: InterstitialAd? = null
    private val showListener = object :FullScreenContentCallback() {
        override fun onAdShowedFullScreenContent() {
            actShown?.invoke()
        }

        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            actDismiss?.invoke()
        }

        override fun onAdDismissedFullScreenContent() {
            actDismiss?.invoke()
        }

        override fun onAdClicked() {
            actClick?.invoke()
        }
    }

    override fun show(activity: Activity): Boolean {
        if (mInterstitial == null) return false
        if (adPos!=AdPos.OPEN &&!ReferrerManager.canShowInterstitialAd()){
            actDismiss?.invoke()
            return false
        }
        mInterstitial?.fullScreenContentCallback = showListener
        mInterstitial?.show(activity)
        return true
    }

    override fun buildInAd(ad: Any) {
        mInterstitial = ad as? InterstitialAd
    }
}