package com.tools.android.translator.ui.server

import android.os.Bundle
import android.view.View
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
}