package com.tools.android.translator.ui.translate

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.tools.android.translator.R
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityMainBinding
import com.tools.android.translator.translate.Language
import com.tools.android.translator.ui.CameraActivity
import com.tools.android.translator.ui.SettingsActivity
import com.tools.android.translator.ui.adapt.LanguageAdapter
import com.tools.android.translator.ui.view.LanguagePanel

class MainActivity : BaseBindingActivity<ActivityMainBinding>(), View.OnClickListener {

    override fun obtainBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var mTranslateModel: TranslateViewModel
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
        mTranslateModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(TranslateViewModel::class.java)
        initViews()

        mTranslateModel.translatedText.observe(
            this,
            { resultOrError ->
                if (resultOrError.error != null) {
                    //srcTextView.setError(resultOrError.error!!.localizedMessage)
                } else {
                    binding.groupTranslate.visibility = View.GONE
                    binding.groupResult.visibility = View.VISIBLE
                    binding.tvResult.text = resultOrError.result
                }
            }
        )

        // Update sync toggle button states based on downloaded models list.
        mTranslateModel.availableModels.observe(
            this,
            { translateRemoteModels ->
                val output = getString(
                    R.string.downloaded_models_label,
                    translateRemoteModels
                )
                /*downloadedModelsTextView.text = output
                sourceSyncButton.isChecked =
                    translateRemoteModels!!.contains(
                        adapter.getItem(sourceLangSelector.selectedItemPosition)!!.code
                    )
                targetSyncButton.isChecked = translateRemoteModels.contains(
                    adapter.getItem(targetLangSelector.selectedItemPosition)!!.code
                )*/
            }
        )
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
                    if (!LanguagePanel.isCurrentSource)
                        changeSide(true)
                }
            }

            R.id.bg_target -> {
                binding.languagePanel.root.apply {
                    expand()
                    if (LanguagePanel.isCurrentSource)
                        changeSide(false)
                }
            }

            R.id.iv_camera, R.id.tv_camera -> startActivity(Intent(this, CameraActivity::class.java))

            R.id.iv_setting, R.id.tv_setting -> startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun initViews() {
        mTranslateModel.sourceLang.value = LanguageAdapter.sourceLa
        mTranslateModel.targetLang.value = LanguageAdapter.targetLa

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
            mTranslateModel.sourceText.postValue(binding.etSource.text.toString())
        }

        binding.languagePanel.root.apply {
            setChoiceListener(object :LanguageAdapter.ILangChoice{
                override fun onChoice(language: Language) {
                    changeSide(LanguagePanel.isCurrentSource)
                }
            })
        }
    }
}