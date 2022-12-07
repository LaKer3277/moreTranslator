package com.tools.android.translator.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.tencent.mmkv.MMKV
import com.tools.android.translator.App
import com.tools.android.translator.ads.AdCenter
import com.tools.android.translator.ads.AdPos
import com.tools.android.translator.ads.AdsListener
import com.tools.android.translator.ads.RefreshAd
import com.tools.android.translator.ads.body.Ad
import com.tools.android.translator.ads.body.NativeAds
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityHomeBinding
import com.tools.android.translator.dialog.LimitDialog
import com.tools.android.translator.dialog.ServerGuideDialog
import com.tools.android.translator.server.ConnectServerManager
import com.tools.android.translator.support.RemoteConfig
import com.tools.android.translator.support.setPoint
import com.tools.android.translator.ui.server.ConnectServerActivity
import com.tools.android.translator.ui.translate.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

class HomeActivity:BaseBindingActivity<ActivityHomeBinding>() {
    private var showingLimitDialog=false
    private var isFirstLoad=true

    override fun obtainBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setClick()
        checkLimit()
        checkCanShowGuideDialog()
    }

    private fun setClick(){
        binding.ivTextTranslate.setOnClickListener { startActivity(Intent(this,MainActivity::class.java)) }
        binding.ivCameraTranslate.setOnClickListener {
            if (checkCameraPermission()) {
                startActivity(Intent(this, CameraActivity::class.java))
            }
        }
        binding.ivVpn.setOnClickListener {
            setPoint.point("itr_vpn_click")
            startActivity(Intent(this,ConnectServerActivity::class.java))
        }
        binding.ivSet.setOnClickListener { startActivity(Intent(this,SettingsActivity::class.java)) }
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


    override fun onResume() {
        super.onResume()
        delayNativeShow()
        checkHotLoadShowDialog()
    }

    private fun delayNativeShow() {
        AdCenter.preloadAd(AdPos.HOME)
        if(RefreshAd.canRefresh(AdPos.HOME)){
            lifecycleScope.launch {
                delay(100L)
                if (isPaused()) return@launch
                requestAndLoad()
            }
        }
    }

    private var lastNative: NativeAds? = null
    private fun requestAndLoad() {
        AdCenter.loadAd(this, AdPos.HOME, object : AdsListener() {
            override fun onAdLoaded(ad: Ad) {
                if (isPaused()) {
                    AdCenter.add2cache(AdPos.HOME, ad)
                    return
                }
                if (ad !is NativeAds) return

                lastNative?.onDestroy()
                lastNative = ad

                binding.ivCover.visibility = View.GONE
                binding.serverHomeAd.apply {
                    Log.i("AdCenter", "start show : ${AdPos.HOME}")
                    root.visibility = View.VISIBLE
                    ad.showTitle(root, this.adTitle)
                    ad.showBody(root, this.adDesc)
                    ad.showIcon(root, this.adIcon)
                    ad.showCta(root, this.adAction)
                    ad.showImage(root, this.adImage)
                    ad.register(root)
                    AdCenter.preloadAd(AdPos.HOME)
                    RefreshAd.setValue(AdPos.HOME,false)
                }
            }
        })
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
            if(isFirstLoad|| App.ins.isHotLoad){
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
}