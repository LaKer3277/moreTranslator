package com.tools.android.translator.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.*
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityWebBinding

/**
 * Created on 2022/4/26
 * Describe:
 */
class WebActivity: BaseBindingActivity<ActivityWebBinding>() {

    companion object {
        fun openPrivacy(ctx: Activity) {
            val intent = Intent(ctx, WebActivity::class.java)
            intent.putExtra("url", "https://sites.google.com/view/itranslatorapp/home")
            ctx.startActivity(intent)
        }
    }

    override fun obtainBinding(): ActivityWebBinding {
        return ActivityWebBinding.inflate(layoutInflater)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loadUrl = intent.getStringExtra("url") ?: ""

        val webSetting = binding.web.settings
        webSetting.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webSetting.setSupportZoom(true)
        webSetting.builtInZoomControls = true
        webSetting.javaScriptEnabled = true
        webSetting.domStorageEnabled = true

        binding.web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                } else {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress = newProgress
                }
            }
        }

        binding.web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        binding.web.loadUrl(loadUrl)
    }
}