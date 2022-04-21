package com.tools.android.translator.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import com.tools.android.translator.base.AnimatorListener
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityLoadingBinding
import com.tools.android.translator.ui.translate.MainActivity

/**
 * Created on 2022/4/20
 * Describe:
 */
class LoadingActivity: BaseBindingActivity<ActivityLoadingBinding>() {

    override fun obtainBinding(): ActivityLoadingBinding {
        return ActivityLoadingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runLoading(2000L) {
            if (isPaused()) return@runLoading
            enterMain()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        valueAni?.cancel()
        valueAni = null
    }

    private fun enterMain() {
         val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
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
}