package com.tools.android.translator.ui.translate

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.tools.android.translator.App
import com.tools.android.translator.R
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityMainBinding
import com.tools.android.translator.translate.Language
import com.tools.android.translator.translate.languageList
import com.tools.android.translator.ui.CameraActivity
import com.tools.android.translator.ui.SettingsActivity
import com.tools.android.translator.ui.adapt.LanguageAdapter
import com.tools.android.translator.ui.adapt.LanguageAdapter.Companion.isCurrentSource

class MainActivity : BaseBindingActivity<ActivityMainBinding>(), View.OnClickListener {

    override fun obtainBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var mTrModel: TranslateViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.includeNav.apply {
            ivText.isSelected = true
            tvText.setTextColor(Color.parseColor("#FBB79F"))

            ivCamera.setOnClickListener(this@MainActivity)
            tvCamera.setOnClickListener(this@MainActivity)
            ivSetting.setOnClickListener(this@MainActivity)
            tvSetting.setOnClickListener(this@MainActivity)
        }
        mTrModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(TranslateViewModel::class.java)
        initViews()

        mTrModel.translatedText.observe(
            this,
            { resultOrError ->
                if (resultOrError.error != null) {
                    //srcTextView.setError(resultOrError.error!!.localizedMessage)
                } else {
                    if (resultOrError.result.isNullOrEmpty()) {
                        binding.groupResult.visibility = View.GONE
                    } else {
                        binding.groupResult.visibility = View.VISIBLE
                    }
                    binding.groupTranslate.visibility = View.GONE
                    binding.tvResult.text = resultOrError.result
                }
            }
        )

        // Update sync toggle button states based on downloaded models list.
        mTrModel.availableModels.observe(this) {
            languageList.forEach { lang ->
                if (lang.available != 1)
                    lang.available = -1
            }
            for (la in languageList) {
                if (it.contains(la.code)) {
                    la.available = 1
                }
            }
            binding.languagePanel.root.notifyAllAdapter()
        }
    }

    override fun onBackPressed() {
        if (hideKeyboard(binding.etSource)) return
        if (binding.languagePanel.root.collapse()) return
        super.onBackPressed()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bg_source -> {
                binding.languagePanel.root.apply {
                    expand()
                    if (!isCurrentSource)
                        changeSide(true)
                }
            }

            R.id.bg_target -> {
                binding.languagePanel.root.apply {
                    expand()
                    if (isCurrentSource)
                        changeSide(false)
                }
            }

            R.id.iv_camera, R.id.tv_camera -> startActivity(Intent(this, CameraActivity::class.java))

            R.id.iv_setting, R.id.tv_setting -> startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private var exchanging = false
    private fun initViews() {
        mTrModel.sourceLang.value = LanguageAdapter.sourceLa
        mTrModel.targetLang.value = LanguageAdapter.targetLa
        binding.imgExchange.setOnClickListener {
            if (exchanging) return@setOnClickListener
            exchanging = true
            exchangeLanguage()
            freshLangUI()
            exchanging = false
        }
        freshLangUI()

        // Translate input text as it is typed
        binding.etSource.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                binding.groupResult.visibility = View.GONE
                //setProgressText(targetTextView)
                if (s.isNotEmpty()) {
                    binding.groupTranslate.visibility = View.VISIBLE
                    binding.clear.visibility = View.VISIBLE
                } else {
                    binding.groupTranslate.visibility = View.GONE
                    binding.clear.visibility = View.GONE
                }
            }
        })

        binding.tvTranslate.setOnClickListener {
            mTrModel.sourceText.postValue(binding.etSource.text.toString())
        }

        binding.languagePanel.root.setChoiceListener(object :LanguageAdapter.ILangChoice{
            override fun onChoice(language: Language) {
                if (!language.isAvailable()) return
                if (isCurrentSource) {
                    mTrModel.sourceLang.value = language
                    LanguageAdapter.sourceLa = language
                    App.ins.sourceLa = language.code
                } else {
                    mTrModel.targetLang.value = language
                    LanguageAdapter.targetLa = language
                    App.ins.targetLa = language.code
                }
                binding.languagePanel.root.collapse()
                freshLangUI()
            }

            override fun onStatus(language: Language) {
                if (isCurrentSource && language == LanguageAdapter.sourceLa) {
                    mTrModel.deleteLanguage(language)
                } else if (!isCurrentSource && language == LanguageAdapter.targetLa) {
                    mTrModel.deleteLanguage(language)
                } else {
                    if (language.isUnavailable()) {
                        language.available = 0
                        mTrModel.downloadLanguage(language)
                    } else return
                }
                binding.languagePanel.root.notifyAllAdapter()
            }
        })

        binding.clear.setOnClickListener {
            binding.etSource.setText("")
            mTrModel.sourceText.value = ""
        }
    }

    private fun exchangeLanguage() {
        val lang = LanguageAdapter.sourceLa
        LanguageAdapter.sourceLa = LanguageAdapter.targetLa
        LanguageAdapter.targetLa = lang

        mTrModel.sourceLang.value = LanguageAdapter.sourceLa
        mTrModel.targetLang.value = lang
        App.ins.sourceLa = LanguageAdapter.sourceLa.code
        App.ins.targetLa = LanguageAdapter.targetLa.code
    }

    private fun freshLangUI() {
        mTrModel.sourceLang.value?.apply {
            binding.tvLaSource.text = displayName
        }

        mTrModel.targetLang.value?.apply {
            binding.tvLaTarget.text = displayName
        }
    }
}