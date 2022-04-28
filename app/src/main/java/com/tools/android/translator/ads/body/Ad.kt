package com.tools.android.translator.ads.body

import com.tools.android.translator.ads.AdPos
import com.tools.android.translator.ads.AdsListener
import com.tools.android.translator.ads.ConfigId

/**
 * Created on 2022/4/26
 * Describe:
 */
abstract class Ad(val adPos: AdPos, val configId: ConfigId) {

    protected var actShown: (() -> Unit)? = null
    protected var actClick: (() -> Unit)? = null
    protected var actDismiss: (() -> Unit)? = null

    abstract fun buildInAd(ad: Any)

    fun defineListener(adsListener: AdsListener) {
        actShown = { adsListener.onAdShown() }
        actClick = { adsListener.onAdClick() }
        actDismiss = { adsListener.onAdDismiss() }
    }

    open fun onDestroy() {

    }
}