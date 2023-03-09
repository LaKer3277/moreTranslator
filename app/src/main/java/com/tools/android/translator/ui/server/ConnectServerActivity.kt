package com.tools.android.translator.ui.server

import android.Manifest
import android.animation.ValueAnimator
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.github.shadowsocks.utils.StartService
import com.tools.android.translator.App
import com.tools.android.translator.R
import com.tools.android.translator.ads.AdCenter
import com.tools.android.translator.ads.AdPos
import com.tools.android.translator.ads.AdsListener
import com.tools.android.translator.ads.body.Ad
import com.tools.android.translator.ads.body.InterstitialAds
import com.tools.android.translator.ads.body.NativeAds
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityConnectServerBinding
import com.tools.android.translator.interfaces.IServerConnectCallback
import com.tools.android.translator.interfaces.ITimerCallback
import com.tools.android.translator.server.ConnectServerManager
import com.tools.android.translator.server.TimeManager
import com.tools.android.translator.support.*
import com.tools.android.translator.ui.CameraActivity
import com.tools.android.translator.ui.SettingsActivity
import com.tools.android.translator.ui.translate.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConnectServerActivity: BaseBindingActivity<ActivityConnectServerBinding>(),
    IServerConnectCallback, ITimerCallback {
    private var connect=false
    private var connecting=false
    private val instance=dp2px(104F)
    private var connectAnimator:ValueAnimator?=null

    private var permission=false
    private val registerResult=registerForActivityResult(StartService()) {
        if (!it &&permission) {
            setPoint.point("itr_vpn_getpermisson")
            permission = false
            connectServer()
        } else {
            connecting=false
            toast("Connected fail")
        }
    }

    override fun obtainBinding(): ActivityConnectServerBinding {
        return ActivityConnectServerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateServerInfo()
        ConnectServerManager.init(this,this)
        TimeManager.setTimerCallback(this)
        setListener()

        if(intent.getBooleanExtra("auto",false)){
            binding.ivConnectBtn.performClick()
        }
    }

    private fun setListener(){
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivConnectBtn.setOnClickListener {
            if(!connecting){
                connecting=true
                setPoint.point("itr_vpn")
                doLogic()
            }
        }
        binding.ivChooseServer.setOnClickListener {
            if(!connecting){
                startActivityForResult(Intent(this,ChooseServerActivity::class.java),1123)
            }
        }
    }

    private fun doLogic(){
        if(RemoteConfig.ins.isLimitUser){
            AlertDialog.Builder(this).apply {
                setMessage("Due to the policy reason , this service is not available in y country")
                setCancelable(false)
                setPositiveButton("confirm", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        finish()
                    }
                })
                show()
            }
            return
        }

        AdCenter.preloadAd(AdPos.CONNECT)
        AdCenter.preloadAd(AdPos.RESULT)
        if(ConnectServerManager.isConnected()){
            startConnectAnimator(false)
            ConnectServerManager.disconnect()
        }else{
            updateServerInfo()
            if (netStatus()==1){
                AlertDialog.Builder(this).apply {
                    setMessage("You are not currently connected to the network")
                    setPositiveButton("sure", null)
                    show()
                }
                connecting=false
                return
            }
            if (VpnService.prepare(this) != null) {
                permission = true
                registerResult.launch(null)
                return
            }
            connectServer()
        }
    }

    private fun connectServer(){
        ConnectServerManager.connect()
        startConnectAnimator(true)
    }

    private fun startConnectAnimator(connect: Boolean){
        this.connect=connect
        connectAnimator=ValueAnimator.ofInt(0, 100).apply {
            duration=10000L
            interpolator = LinearInterpolator()
            addUpdateListener {
                val pro = it.animatedValue as Int
                binding.ivConnectBtn.translationY=getTranslationY(connect, pro)
                val duration = (10 * (pro / 100.0F)).toInt()
                if (duration in 2..9){
                    val bool=if(connect) ConnectServerManager.isConnected() else ConnectServerManager.isStopped()
                    if (bool&&AdCenter.hasCached(AdPos.CONNECT)){
                        stopConnectAnimator()
                        binding.ivConnectBtn.translationY=getTranslationY(connect, 100)
                        if(!App.ins.isAtomicStarting.get()){
                            AdCenter.loadAd(this@ConnectServerActivity, AdPos.CONNECT, object :AdsListener() {
                                override fun onAdLoaded(ad: Ad) {
                                    if (ad !is InterstitialAds) {
                                        return
                                    }
                                    ad.show(this@ConnectServerActivity)
                                }

                                override fun onAdError(err: String?) {
                                    checkResult()
                                }

                                override fun onAdDismiss() {
                                    super.onAdDismiss()
                                    checkResult()
                                }
                            })
                            App.ins.isAtomicStarting.set(false)
                        }else{
                            checkResult()
                        }
                    }

                }else if (duration>=10){
                    checkResult()
                }
            }
            start()
        }
    }

    private fun checkResult(){
        val bool=if(connect) ConnectServerManager.isConnected() else ConnectServerManager.isStopped()
        if (bool){
            if (connect){
                setPoint.point("itr_vpn_succ")
                updateConnectedUI()
            }else{
                setPoint.point("itr_vpn_dis")
                updateStoppedUI()
                updateServerInfo()
            }
            if(App.ins.isAppForeground()){
                startActivity(Intent(this,ConnectResultActivity::class.java).apply {
                    putExtra("connect",connect)
                })
            }

        }else{
            if(connect){
                setPoint.point("itr_vpn_fail")
            }
            updateStoppedUI()
            toast(if (connect) "Connect Fail" else "Disconnect Fail")
        }
        connecting=false
    }

    private fun updateServerInfo(){
        val serverBean = ConnectServerManager.serverBean
        binding.tvServerName.text=serverBean.guo
        binding.ivServerLogo.setImageResource(getServerLogo(serverBean))
    }

    private fun updateConnectedUI(){
        if(binding.ivConnectBtn.translationY != (this.instance.toFloat())){
            binding.ivConnectBtn.translationY=(this.instance.toFloat())
        }
        binding.ivServerStatus.setImageResource(R.mipmap.server_connected)
    }

    private fun updateStoppedUI(){
        binding.ivServerStatus.setImageResource(R.mipmap.server_idle)
    }

    private fun stopConnectAnimator(){
        connectAnimator?.removeAllUpdateListeners()
        connectAnimator?.cancel()
        connectAnimator=null
    }

    private fun getTranslationY(connect: Boolean,pro:Int):Float{
        val fl = instance/ 100F * pro
        return if (connect) fl else instance-fl
    }

    private fun checkCameraPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) return true
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        return false
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
                finish()
            }
        }
    }

    override fun connectSuccess() {
        updateConnectedUI()
    }

    override fun disconnectSuccess() {
        if(!connecting){
            updateStoppedUI()
        }
    }

    override fun connectTime(time: String) {
        binding.tvConnectTime.text=time
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==1123){
            when(data?.getStringExtra("back")){
                "dis"->{
                    binding.ivConnectBtn.performClick()
                }
                "con"->{
                    updateServerInfo()
                    binding.ivConnectBtn.performClick()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        delayNativeShow()
    }

    private fun delayNativeShow() {
        AdCenter.preloadAd(AdPos.TRANS)
        lifecycleScope.launch {
            delay(100L)
            if (isPaused()) return@launch

            requestAndLoad()
        }
    }

    private var lastNative: NativeAds? = null
    private fun requestAndLoad() {
        AdCenter.loadAd(this, AdPos.SERVER_HOME, object :AdsListener() {
            override fun onAdLoaded(ad: Ad) {
                if (isPaused()) {
                    AdCenter.add2cache(AdPos.SERVER_HOME, ad)
                    return
                }
                if (ad !is NativeAds) return

                lastNative?.onDestroy()
                lastNative = ad

                binding.ivCover.visibility = View.GONE
                binding.serverHomeAd.apply {
                    root.visibility = View.VISIBLE
                    ad.showTitle(root, this.adTitle)
                    ad.showBody(root, this.adDesc)
                    ad.showIcon(root, this.adIcon)
                    ad.showCta(root, this.adAction)
                    ad.showImage(root, this.adImage)
                    ad.register(root)
                    AdCenter.preloadAd(AdPos.SERVER_HOME)
                }
            }
        })
    }

    override fun onBackPressed() {
        if (!connecting){
            finish()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        stopConnectAnimator()
        ConnectServerManager.onDestroy()
        TimeManager.setTimerCallback(null)
    }
}