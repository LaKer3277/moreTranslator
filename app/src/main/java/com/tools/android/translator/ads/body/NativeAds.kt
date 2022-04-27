package com.tools.android.translator.ads.body

import android.view.View
import android.widget.TextView
import com.tools.android.translator.ads.AdPos
import com.tools.android.translator.ads.ConfigId

/**
 * Created on 2022/4/27
 * Describe:
 */
abstract class NativeAds(adPos: AdPos, configId: ConfigId): Ad(adPos, configId) {

    abstract fun showTitle(parentV: View?, textView: TextView)
    abstract fun showBody(parentV: View?, textView: TextView)
    abstract fun showImage(parentV: View?, mediaView: View)
    abstract fun showIcon(parentV: View?, mediaView: View)
    abstract fun showCta(parentV: View?, view: View)
    abstract fun register(parentV: View)

}