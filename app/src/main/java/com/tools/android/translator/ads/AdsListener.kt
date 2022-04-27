package com.tools.android.translator.ads

import com.tools.android.translator.ads.body.Ad

/**
 * Created on 2022/4/26
 * Describe:
 */
open class AdsListener {

    open fun onAdLoaded(ad: Ad) {}

    open fun onAdError(err: String?) {}

    open fun onAdShown() {}

    open fun onAdClick() {}

    open fun onAdDismiss() {}

}