package com.tools.android.translator.ui.translate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.tools.android.translator.App
import com.tools.android.translator.R
import com.tools.android.translator.ads.AdCenter
import com.tools.android.translator.ads.AdPos
import com.tools.android.translator.ads.AdsListener
import com.tools.android.translator.ads.body.Ad
import com.tools.android.translator.ads.body.InterstitialAds
import com.tools.android.translator.ads.body.NativeAds
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityMainBinding
import com.tools.android.translator.translate.Language
import com.tools.android.translator.translate.languageList
import com.tools.android.translator.ui.CameraActivity
import com.tools.android.translator.ui.SettingsActivity
import com.tools.android.translator.ui.adapt.LanguageAdapter
import com.tools.android.translator.ui.adapt.LanguageAdapter.Companion.isCurrentSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        initLanguageViews()
        binding.clear.setOnClickListener {
            clearEtText()
        }
    }

    override fun onResume() {
        super.onResume()
        mTrModel.sourceLang.value = LanguageAdapter.sourceLa
        mTrModel.targetLang.value = LanguageAdapter.targetLa
        freshLangUI()
        delayNativeShow()
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

            R.id.iv_camera, R.id.tv_camera -> {
                if (checkCameraPermission()) {
                    startActivity(Intent(this, CameraActivity::class.java))
                }
            }

            R.id.iv_setting, R.id.tv_setting -> startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun clearEtText() {
        binding.etSource.setText("")
    }

    private var isTranslating = false
    private var exchanging = false
    private fun initLanguageViews() {
        binding.imgExchange.setOnClickListener {
            if (exchanging) return@setOnClickListener
            exchanging = true
            //清除文本
            clearEtText()
            exchangeLanguage()
            freshLangUI()
            exchanging = false
            //textChangedForTranslatePrepared()
        }

        // Translate input text as it is typed
        binding.etSource.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                textChangedForTranslatePrepared()
            }
        })

        binding.tvTranslate.setOnClickListener {
            if (isTranslating) return@setOnClickListener
            isTranslating = true
            showLoading(true)
            lifecycleScope.launch {
                //展示loading 过程
                delay(500L)
                mTrModel.sourceText.postValue(binding.etSource.text.toString())
            }
        }

        binding.languagePanel.root.setChoiceListener(object :LanguageAdapter.ILangChoice{
            //选择语言
            override fun onChoice(language: Language) {
                if (!language.isAvailable()) return
                if (isCurrentSource) {
                    mTrModel.sourceLang.value = language
                    LanguageAdapter.sourceLa = language
                    App.ins.sourceLa = language.code
                    //清除翻译内容并弹出软键盘
                    clearEtText()
                    showKeyboard(binding.etSource)
                } else {
                    mTrModel.targetLang.value = language
                    LanguageAdapter.targetLa = language
                    App.ins.targetLa = language.code
                }
                binding.languagePanel.root.collapse()
                freshLangUI()
            }

            //下载 / 删除
            override fun onStatus(language: Language) {
                //下载
                if (language.isUnavailable()) {
                    language.available = 0
                    mTrModel.downloadLanguage(language)
                } else {
                    if ((isCurrentSource && language == LanguageAdapter.sourceLa)
                        || (!isCurrentSource && language == LanguageAdapter.targetLa)) {
                        return
                    } else {
                        mTrModel.deleteLanguage(language)
                    }
                }
                binding.languagePanel.root.notifyAllAdapter()
            }
        })

        mTrModel.translatedText.observe(
            this,
            { resultOrError ->
                showLoading(false)

                val showTxt = {
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

                if (isTranslating) {
                    isTranslating = false
                    showInterstitial {
                        showTxt.invoke()
                    }
                } else {
                    showTxt.invoke()
                }
            }
        )

        // Update sync toggle button states based on downloaded models list.
        mTrModel.availableModels.observe(this) {
            /*languageList.forEach { lang ->
                if (lang.available != 1)
                    lang.available = -1
            }*/
            for (la in languageList) {
                if (it.contains(la.code)) {
                    la.available = 1
                } else {
                    la.available = -1
                }
            }
            binding.languagePanel.root.notifyAllAdapter()
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

    private fun textChangedForTranslatePrepared() {
        binding.groupResult.visibility = View.GONE
        //setProgressText(targetTextView)
        if (binding.etSource.text.isNotEmpty()) {
            binding.groupTranslate.visibility = View.VISIBLE
            binding.clear.visibility = View.VISIBLE
        } else {
            binding.groupTranslate.visibility = View.GONE
            binding.clear.visibility = View.GONE
            mTrModel.sourceText.value = ""
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this, CameraActivity::class.java))
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) return true
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        return false
    }

    private fun delayNativeShow() {
        AdCenter.preloadAd(AdPos.TRANS)
        lifecycleScope.launch {
            delay(100L)
            if (isPaused()) return@launch

            if (needFreshNav) {
                needFreshNav = false

                requestAndLoad()
            }
        }
    }

    private var lastNative: NativeAds? = null
    private fun requestAndLoad() {
        AdCenter.loadAd(this, AdPos.MAIN, object :AdsListener() {
            override fun onAdLoaded(ad: Ad) {
                if (isPaused()) {
                    AdCenter.add2cache(AdPos.MAIN, ad)
                    return
                }
                if (ad !is NativeAds) return

                lastNative?.onDestroy()
                lastNative = ad

                binding.imgHolder.visibility = View.GONE
                binding.nativeAd.apply {
                    root.visibility = View.VISIBLE
                    ad.showTitle(root, this.adTitle)
                    ad.showBody(root, this.adDesc)
                    ad.showIcon(root, this.adIcon)
                    ad.showCta(root, this.adAction)
                    ad.register(root)
                }
            }
        })
    }

    private fun showInterstitial(action: () -> Unit) {
        AdCenter.loadAd(this, AdPos.TRANS, object :AdsListener() {
            override fun onAdLoaded(ad: Ad) {
                action.invoke()
                if (ad !is InterstitialAds) {
                    return
                }
                ad.show(this@MainActivity)
            }

            override fun onAdError(err: String?) {
                action.invoke()
            }
        }, justCache = true)
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.layoutLoading.visibility = View.VISIBLE
            binding.progressBar.show()
        } else {
            binding.layoutLoading.visibility = View.GONE
        }
    }

    companion object {
        var needFreshNav = true
    }
}