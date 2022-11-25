package com.tools.android.translator.ui.view

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tools.android.translator.App
import com.tools.android.translator.R
import com.tools.android.translator.base.AnimatorListener
import com.tools.android.translator.translate.Language
import com.tools.android.translator.translate.languageList
import com.tools.android.translator.ui.adapt.LanguageAdapter
import com.tools.android.translator.ui.adapt.LanguageAdapter.Companion.isCurrentSource
import java.lang.StringBuilder

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
        initViews(context)
        cancelView.setOnClickListener(this)
        findViewById<View>(R.id.bg_source).setOnClickListener(this)
        findViewById<View>(R.id.bg_target).setOnClickListener(this)
        panel.post {
            hasInflated = true
            visibility = View.GONE
        }
    }

    private var iLangChoice: LanguageAdapter.ILangChoice? = null

    private var animSet: AnimatorSet? = null
    private var hasInflated = false

    private lateinit var tvSource: TextView
    private lateinit var tvTarget: TextView

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
            Log.e("qwer","====${language.available}=")
            if (!language.isAvailable()) return
            addNewHistory(language)
        }

        override fun onStatus(language: Language) {
            iLangChoice?.onStatus(language)
        }
    }

    private fun initViews(ctx: Context) {
        tvSource = findViewById(R.id.tv_la_source)
        tvTarget = findViewById(R.id.tv_la_target)
        findViewById<ImageView>(R.id.img_exchange).setOnClickListener {
            val lang = LanguageAdapter.sourceLa
            LanguageAdapter.sourceLa = LanguageAdapter.targetLa
            LanguageAdapter.targetLa = lang
            freshLanguageUi()
        }
        freshLanguageUi()

        panel = findViewById(R.id.panel)
        cancelView = findViewById(R.id.cancel_top)
        tvRecent = findViewById(R.id.tv_recently)
        rvRecent = findViewById(R.id.rv_recent)
        rvRecent.layoutManager = LinearLayoutManager(ctx)

        recentAdapter = LanguageAdapter(true, arrayListOf(), innerLangChoice, isRecently = true)
        allAdapter = LanguageAdapter(true, languageList, innerLangChoice)
        rvAll = findViewById(R.id.rv_all)
        rvAll.layoutManager = LinearLayoutManager(ctx)

        rvRecent.adapter = recentAdapter
        rvAll.adapter = allAdapter

        App.ins.sourceHistory.apply {
            if (this.contains(";")) {
                val sp = this.split(";")
                if (sp.isNotEmpty()) {
                    for (s in sp) {
                        listHistorySource.add(Language(code = s, available = 1))
                    }
                }
            } else if (this.isNotEmpty()) {
                listHistorySource.add(Language(code = this, available = 1))
            }
        }
        App.ins.targetHistory.apply {
            if (this.contains(";")) {
                val sp = this.split(";")
                if (sp.isNotEmpty()) {
                    for (s in sp) {
                        listHistoryTarget.add(Language(code = s, available = 1))
                    }
                }
            } else if (this.isNotEmpty()) {
                listHistoryTarget.add(Language(code = this, available = 1))
            }
        }

        notifyListSide()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyListSide() {
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

    private fun freshLanguageUi() {
        LanguageAdapter.sourceLa.apply {
            tvSource.text = displayName
        }
        LanguageAdapter.targetLa.apply {
            tvTarget.text = displayName
        }
    }

    @Synchronized
    private fun addNewHistory(language: Language) {
        val isSource = isCurrentSource
        val listHistory = if (isSource) listHistorySource else listHistoryTarget
        if (listHistory.contains(language)) return

        listHistory.add(0, language)
        if (listHistory.size > 5) {
            listHistory.removeAt(5)
        }
        val builder = StringBuilder()
        for (la in listHistory) {
            builder.append(la.code).append(";")
        }
        if (isSource) {
            App.ins.sourceHistory = builder.substring(0, builder.length - 1)
        } else {
            App.ins.targetHistory = builder.substring(0, builder.length - 1)
        }

        notifyListSide()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancel_top -> collapse()

            R.id.bg_source -> changeSide(true)

            R.id.bg_target -> changeSide(false)
        }
    }

    fun expand() {
        freshLanguageUi()
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
        if (isSource == isCurrentSource) return
        isCurrentSource = isSource
        notifyListSide()
    }

    fun setChoiceListener(listener: LanguageAdapter.ILangChoice) {
        iLangChoice = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyAllAdapter() {
        allAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyRecentAdapter() {
        recentAdapter.notifyDataSetChanged()
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