package com.tools.android.translator.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ActivityUtils
import com.tools.android.translator.App
import com.tools.android.translator.ads.AdCenter
import com.tools.android.translator.ads.AdPos
import com.tools.android.translator.ads.AdsListener
import com.tools.android.translator.ads.body.Ad
import com.tools.android.translator.ads.body.InterstitialAds
import com.tools.android.translator.base.AnimatorListener
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityLoadingBinding
import com.tools.android.translator.support.ReferrerManager
import com.tools.android.translator.ui.translate.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created on 2022/4/20
 * Describe:
 */
class LoadingActivity: BaseBindingActivity<ActivityLoadingBinding>() {

    override fun obtainBinding(): ActivityLoadingBinding {
        return ActivityLoadingBinding.inflate(layoutInflater)
    }

    private var isAdImpression = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReferrerManager.readReferrer()
        runLoading(10_000L) {
            if (isAdImpression) return@runLoading
            if (isPaused()) return@runLoading
            enterMain()
        }
        isAdImpression = false
        lifecycleScope.launch {
            AdCenter.preloadAd(AdPos.MAIN)
            AdCenter.preloadAd(AdPos.TRANS)
            AdCenter.preloadAd(AdPos.CONNECT)
            AdCenter.preloadAd(AdPos.RESULT)
            AdCenter.preloadAd(AdPos.SERVER_HOME)
            AdCenter.preloadAd(AdPos.HOME)

            delay(880L)
            AdCenter.loadAd(this@LoadingActivity, AdPos.OPEN, listener)
        }
    }

    private val listener = object :AdsListener() {
        override fun onAdLoaded(ad: Ad) {
            if (isPaused()) {
                AdCenter.add2cache(AdPos.OPEN, ad)
                return
            }
            if (ad is InterstitialAds) {
                ad.show(this@LoadingActivity)
            }
        }

        override fun onAdError(err: String?) {
            if (isPaused()) finish() else enterMain()
        }

        override fun onAdShown() {
            isAdImpression = true
        }

        override fun onAdDismiss() {
            if (!App.ins.isAppForeground()) return
            enterMain()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        valueAni?.cancel()
        valueAni = null
    }

    private fun enterMain() {
        if (!ActivityUtils.isActivityExistsInStack(HomeActivity::class.java)){
            startActivity(Intent(this,HomeActivity::class.java))
        }
        finish()
    }

    private var valueAni: ValueAnimator? = null
    private fun runLoading(long: Long, action: () -> Unit) {
        valueAni?.cancel()
        valueAni = ValueAnimator.ofInt(binding.progressBar.progress, 100)
        valueAni?.duration = long
        valueAni?.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener{
            override fun onAnimationUpdate(p0: ValueAnimator?) {
                (p0?.animatedValue as? Int)?.apply {
                    binding.progressBar.progress = this
                }
            }
        })
        valueAni?.addListener(object : AnimatorListener() {
            private var isCanceled = false
            override fun onAnimationCancel(animation: Animator?) {
                isCanceled = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isCanceled) {
                    action.invoke()
                }
            }
        })
        valueAni?.start()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode== KeyEvent.KEYCODE_BACK){
            return true
        }
        return false
    }

    companion object {
        fun restart(activity: Activity) {
            val intent = Intent(activity, LoadingActivity::class.java)
            intent.putExtra("restart", true)
            activity.startActivity(intent)
        }
    }
}