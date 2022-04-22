package com.tools.android.translator.ui.view

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import com.tools.android.translator.R
import com.tools.android.translator.base.AnimatorListener

/**
 * Created on 2022/4/22
 * Describe:
 */
class LanguagePanel: FrameLayout, View.OnClickListener {

    constructor(context: Context) : super(context, null) {
        visibility = View.INVISIBLE
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        visibility = View.INVISIBLE
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        visibility = View.INVISIBLE
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initViews()
        tvSave.setOnClickListener(this)
        cancelView.setOnClickListener(this)
        panel.post {
            hasInflated = true
            visibility = View.GONE
        }
    }

    private var animSet: AnimatorSet? = null
    private var hasInflated = false
    private lateinit var tvSave: TextView
    private lateinit var panel: View
    private lateinit var cancelView: View

    private fun initViews() {
        tvSave = findViewById(R.id.tv_save)
        panel = findViewById(R.id.panel)
        cancelView = findViewById(R.id.cancel_top)
    }

    override fun onClick(v: View?) {
        val isSave = v?.id == R.id.tv_save
        if (isSave || v?.id == R.id.cancel_top) {
            if (isSave) save()
            collapse()
        }
    }

    fun expand() {
        if (!hasInflated) {
            panel.post {
                hasInflated = true
                startAnim(true)
            }
        } else {
            startAnim(true)
        }
    }

    fun collapse(animEnd: (() -> Unit)? = null): Boolean {
        if (visibility == View.VISIBLE) {
            startAnim(false, animEnd)
            return true
        }

        return false
    }

    private fun save() {

    }

    private fun startAnim(isExpand: Boolean, animEnd: (() -> Unit)? = null) {
        if (isExpand) {
            visibility = View.INVISIBLE
        }
        val padHeight = panel.measuredHeight.toFloat()
        val start: Float
        val end: Float
        if (isExpand) {
            start = padHeight
            end = 0f
        } else {
            start = 0f
            end = padHeight
        }

        val transAnim = ObjectAnimator.ofFloat(panel, "translationY", start, end)

        val fromColor: Int
        val toColor: Int
        if (isExpand) {
            fromColor = Color.parseColor("#00000000")
            toColor = Color.parseColor("#8020252C")
        } else {
            fromColor = Color.parseColor("#8020252C")
            toColor = Color.parseColor("#00000000")
        }

        val alphaAnim = ValueAnimator.ofInt(fromColor, toColor)
        alphaAnim.setEvaluator(ArgbEvaluator())
        alphaAnim.addUpdateListener {
            val color = it.animatedValue as Int
            cancelView.setBackgroundColor(color)
        }

        animSet = AnimatorSet()
        if (isExpand || animEnd == null) {
            animSet!!.duration = 800L
        } else {
            animSet!!.duration = 1000L
        }

        animSet!!.interpolator = DecelerateInterpolator(1.2f)
        animSet!!.play(transAnim).with(alphaAnim)
        animSet!!.addListener(object : AnimatorListener() {
            override fun onAnimationStart(animation: Animator?) {
                if (isExpand) visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isExpand) visibility = View.GONE
                animEnd?.invoke()
            }
        })
        animSet!!.start()
    }
}