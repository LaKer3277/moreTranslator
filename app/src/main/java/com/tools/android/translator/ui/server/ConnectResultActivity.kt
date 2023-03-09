package com.tools.android.translator.ui.server

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.tools.android.translator.R
import com.tools.android.translator.ads.AdCenter
import com.tools.android.translator.ads.AdPos
import com.tools.android.translator.ads.AdsListener
import com.tools.android.translator.ads.body.Ad
import com.tools.android.translator.ads.body.NativeAds
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityConnectResultBinding
import com.tools.android.translator.server.ConnectServerManager
import com.tools.android.translator.support.getServerLogo
import com.tools.android.translator.ui.CameraActivity
import com.tools.android.translator.ui.translate.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConnectResultActivity : BaseBindingActivity<ActivityConnectResultBinding>() {
    override fun obtainBinding(): ActivityConnectResultBinding {
        return ActivityConnectResultBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.ivBack.setOnClickListener { finish() }
        val connect = intent.getBooleanExtra("connect",false)
        if(connect){
            binding.tvResult.text="Connected succeeded"
            binding.ivResult.setImageResource(R.mipmap.resut_connect)
        }else{
            binding.tvResult.text="Disconnected succeeded"
            binding.ivResult.setImageResource(R.mipmap.result_disconnect)
        }

        val lastServer = ConnectServerManager.lastServer
        binding.tvServerName.text=lastServer.guo
        binding.ivServerLogo.setImageResource(getServerLogo(lastServer))
        binding.llcCameraTranslate.setOnClickListener {
            if (checkCameraPermission()) {
                startActivity(Intent(this, CameraActivity::class.java))
            }
        }
        binding.llcTextTranslate.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        delayNativeShow()
    }

    private fun delayNativeShow() {
        AdCenter.preloadAd(AdPos.RESULT)
        lifecycleScope.launch {
            delay(100L)
            if (isPaused()) return@launch

            requestAndLoad()
        }
    }

    private var lastNative: NativeAds? = null
    private fun requestAndLoad() {
        AdCenter.loadAd(this, AdPos.RESULT, object : AdsListener() {
            override fun onAdLoaded(ad: Ad) {
                if (isPaused()) {
                    AdCenter.add2cache(AdPos.RESULT, ad)
                    return
                }
                if (ad !is NativeAds) return

                lastNative?.onDestroy()
                lastNative = ad

                binding.ivCover.visibility = View.GONE
                binding.resultAd.apply {
                    root.visibility = View.VISIBLE
                    ad.showTitle(root, this.adTitle)
                    ad.showBody(root, this.adDesc)
                    ad.showIcon(root, this.adIcon)
                    ad.showCta(root, this.adAction)
                    ad.showImage(root, this.adImage)
                    ad.register(root)
                    AdCenter.preloadAd(AdPos.RESULT)
                }
            }
        })
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

}