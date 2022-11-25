package com.tools.android.translator.ui.translate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.tencent.mmkv.MMKV
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
import com.tools.android.translator.dialog.LimitDialog
import com.tools.android.translator.dialog.ServerGuideDialog
import com.tools.android.translator.server.ConnectServerManager
import com.tools.android.translator.support.RemoteConfig
import com.tools.android.translator.support.setPoint
import com.tools.android.translator.translate.Language
import com.tools.android.translator.translate.languageList
import com.tools.android.translator.ui.CameraActivity
import com.tools.android.translator.ui.SettingsActivity
import com.tools.android.translator.ui.adapt.LanguageAdapter
import com.tools.android.translator.ui.adapt.LanguageAdapter.Companion.isCurrentSource
import com.tools.android.translator.ui.server.ConnectServerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

class MainActivity : BaseBindingActivity<ActivityMainBinding>(), View.OnClickListener, CoroutineScope by MainScope() {
    private var showingLimitDialog=false
    private var isFirstLoad=true

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
            serverLayout.setOnClickListener(this@MainActivity)
        }
        mTrModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(TranslateViewModel::class.java)
        initLanguageViews()
        binding.clear.setOnClickListener {
            clearEtText()
        }

        checkLimit()
        checkCanShowGuideDialog()
    }

    override fun onResume() {
        super.onResume()
        mTrModel.fetchDownloadedModels()
        mTrModel.sourceLang.value = LanguageAdapter.sourceLa
        mTrModel.targetLang.value = LanguageAdapter.targetLa
        freshLangUI()
        delayNativeShow()
        checkHotLoadShowDialog()
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
            R.id.server_layout->{
                setPoint.point("itr_vpn_click")
                startActivity(Intent(this, ConnectServerActivity::class.java))
            }
        }
    }

    private fun clearEtText() {
        binding.etSource.setText("")
    }

    private var isTranslating = false
    private var exchanging = false
    private fun initLanguageViews() {
        binding.tvResult.isVerticalScrollBarEnabled = true
        binding.tvResult.movementMethod = ScrollingMovementMethod.getInstance()
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

                launch {
                    delay(50L)
                    showLoading(false)
                    if (isTranslating && !App.ins.isAtomicStarting.get()) {
                        showInterstitial {
                            showTxt.invoke()
                        }
                    } else {
                        showTxt.invoke()
                    }
                    App.ins.isAtomicStarting.set(false)
                    isTranslating = false
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

    private fun checkLimit(){
        showingLimitDialog=false
        val country = Locale.getDefault().country
        if(country=="IR"){
            showingLimitDialog=true
            showLimitDialog()
        }else{
            OkGo.get<String>("https://api.infoip.io/ip").execute(object : StringCallback(){
                override fun onSuccess(response: Response<String>?) {
                    //{"ip":"104.206.40.160","country":"United States","cc":"US"}
                    try {
                        if(JSONObject(response?.body().toString()).optString("cc")=="IR"){
                            showingLimitDialog=true
                            showLimitDialog()
                        }
                    }catch (e:Exception){

                    }
                }

                override fun onError(response: Response<String>?) {
                    super.onError(response)

                }
            })
        }
    }

    private fun checkCanShowGuideDialog(){
        val editPopup=MMKV.defaultMMKV().decodeString("editPopup")?:""
        val editItrV=MMKV.defaultMMKV().decodeString("editItrV")?:""
        if(editPopup.isNotEmpty()){
            RemoteConfig.ins.itrPopShow=editPopup
        }
        if(editItrV.isNotEmpty()){
            RemoteConfig.ins.itrV=editItrV
        }



        if(showingLimitDialog){
            return
        }
        if(RemoteConfig.ins.itrPopShow=="cold"&&isFirstLoad){
            isFirstLoad=false
            checkReferrer()
            return
        }
    }

    private fun checkHotLoadShowDialog() {
        if(RemoteConfig.ins.itrPopShow=="both"&&!ConnectServerManager.isConnected()){
            if(isFirstLoad||App.ins.isHotLoad){
                isFirstLoad=false
                App.ins.isHotLoad=false
                checkReferrer()
            }
        }
    }

    private fun checkReferrer(){
        when(RemoteConfig.ins.itrV){
            "0"->showServerGuideDialog()
            "1"->readReferrer {
                //    1. referrer字段包含【fb4a】【gclid】【not%20set】【youtubeads】【%7B%22】识别为买量
                if(it.contains("fb4a")||it.contains("gclid")||
                    it.contains("not%20set")||it.contains("youtubeads")||it.contains("%7B%22")){
                    showServerGuideDialog()
                }
            }
            "2"->readReferrer {
                //    1. FB用户为referrer字段包含facebook.或者fb4a
                if (it.contains("facebook")||it.contains("fb4a")){
                    showServerGuideDialog()
                }
            }
        }
    }

    private fun readReferrer(callback:(referrer:String)->Unit){
//        callback.invoke("ffffffb4a")

        val decodeString = MMKV.defaultMMKV().decodeString("referrer", "")?:""
        if(decodeString.isEmpty()){
            val referrerClient = InstallReferrerClient.newBuilder(App.ins).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    try {
                        referrerClient.endConnection()
                        when (responseCode) {
                            InstallReferrerClient.InstallReferrerResponse.OK -> {
                                val installReferrer = referrerClient.installReferrer.installReferrer
                                MMKV.defaultMMKV().encode("referrer",installReferrer)
                                callback.invoke(installReferrer)
                            }
                            else->{

                            }
                        }
                    } catch (e: Exception) {

                    }
                }
                override fun onInstallReferrerServiceDisconnected() {
                }
            })
        }else{
            callback.invoke(decodeString)
        }
    }

    private fun showServerGuideDialog(){
        setPoint.point("itr_noti_show")
        if(RemoteConfig.ins.isShowingGuideDialog){
            return
        }
        ServerGuideDialog().show(supportFragmentManager,"ServerGuideDialog")
    }

    private fun showLimitDialog(){
        LimitDialog().show(supportFragmentManager,"LimitDialog")
    }

    companion object {
        var needFreshNav = true
    }
}