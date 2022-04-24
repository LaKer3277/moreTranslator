package com.tools.android.translator.ui.view

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tools.android.translator.App
import com.tools.android.translator.R
import com.tools.android.translator.base.AnimatorListener
import com.tools.android.translator.translate.Language
import com.tools.android.translator.translate.languageList
import com.tools.android.translator.ui.adapt.LanguageAdapter

/**
 * Created on 2022/4/22
 * Describe:
 */
class LanguagePanel: FrameLayout, View.OnClickListener {

    companion object {
        //当前是否是源语言
        var isCurrentSource = true
    }

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
        initViews(context)
        tvSave.setOnClickListener(this)
        cancelView.setOnClickListener(this)
        panel.post {
            hasInflated = true
            visibility = View.GONE
        }
    }

    private var iLangChoice: LanguageAdapter.ILangChoice? = null

    private var animSet: AnimatorSet? = null
    private var hasInflated = false
    private lateinit var tvSave: TextView
    private lateinit var panel: View
    private lateinit var cancelView: View

    private lateinit var tvRecent: TextView
    private lateinit var rvRecent: RecyclerView
    private lateinit var recentAdapter: LanguageAdapter

    private lateinit var rvAll: RecyclerView
    private lateinit var allAdapter: LanguageAdapter

    private val listHistorySource = arrayListOf<Language>()
    private val listHistoryTarget = arrayListOf<Language>()

    private val innerLangChoice = object :LanguageAdapter.ILangChoice {
        override fun onChoice(language: Language) {

            iLangChoice?.onChoice(language)
        }
    }

    private fun initViews(ctx: Context) {
        tvSave = findViewById(R.id.tv_save)
        panel = findViewById(R.id.panel)
        cancelView = findViewById(R.id.cancel_top)
        tvRecent = findViewById(R.id.tv_recently)
        rvRecent = findViewById(R.id.rv_recent)

        recentAdapter = LanguageAdapter(true, arrayListOf(), innerLangChoice, isRecently = true)
        allAdapter = LanguageAdapter(true, languageList, innerLangChoice)
        rvAll = findViewById(R.id.rv_all)
        rvAll.layoutManager = LinearLayoutManager(ctx)

        App.ins.sourceOld.apply {
            if (this.contains(";")) {
                val sp = this.split(";")
                if (sp.isNotEmpty()) {
                    for (s in sp) {
                        listHistorySource.add(Language(code = s))
                    }
                }
            }
        }
        App.ins.targetOld.apply {
            if (this.contains(";")) {
                val sp = this.split(";")
                if (sp.isNotEmpty()) {
                    for (s in sp) {
                        listHistoryTarget.add(Language(code = s))
                    }
                }
            }
        }

        notifyRealtime()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyRealtime() {
        if (isCurrentSource) {
            if (listHistorySource.isNotEmpty()) {
                tvRecent.visibility = View.VISIBLE
                rvRecent.visibility = View.VISIBLE

                recentAdapter.isSource = true
                recentAdapter.list = listHistorySource
                recentAdapter.notifyDataSetChanged()
            } else {
                tvRecent.visibility = View.GONE
                rvRecent.visibility = View.GONE
            }
            return
        }

        if (listHistoryTarget.isNotEmpty()) {
            tvRecent.visibility = View.VISIBLE
            rvRecent.visibility = View.VISIBLE

            recentAdapter.isSource = false
            recentAdapter.list = listHistoryTarget
            recentAdapter.notifyDataSetChanged()
        } else {
            tvRecent.visibility = View.GONE
            rvRecent.visibility = View.GONE
        }
    }

    private fun addNewHistory(language: Language) {
        if (isCurrentSource) {
            if (!listHistorySource.contains(language)) {
                App.ins.sourceOld = App.ins.sourceOld + ";${language.code}"
            }
        } else {
            if (!listHistoryTarget.contains(language)) {
                App.ins.targetOld = App.ins.targetOld + ";${language.code}"
            }
        }
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

    fun changeSide(isSource: Boolean) {
        isCurrentSource = isSource
        notifyRealtime()
    }

    fun setChoiceListener(listener: LanguageAdapter.ILangChoice) {
        iLangChoice = listener
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