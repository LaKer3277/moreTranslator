package com.tools.android.translator.ads.body

import android.app.Activity
import com.tools.android.translator.ads.AdPos
import com.tools.android.translator.ads.ConfigId

/**
 * Created on 2022/4/27
 * Describe:
 */
abstract class InterstitialAds(adPos: AdPos, configId: ConfigId): Ad(adPos, configId) {

    abstract fun show(activity: Activity): Boolean
}