package com.tools.android.translator.ui.server

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.tools.android.translator.ads.AdCenter
import com.tools.android.translator.ads.AdPos
import com.tools.android.translator.ads.AdsListener
import com.tools.android.translator.ads.body.Ad
import com.tools.android.translator.ads.body.InterstitialAds
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityChooseServerBinding
import com.tools.android.translator.server.ConnectServerManager
import com.tools.android.translator.server.ServerBean
import com.tools.android.translator.ui.adapt.ServerListAdapter

class ChooseServerActivity: BaseBindingActivity<ActivityChooseServerBinding>() {

    private val listAdapter by lazy { ServerListAdapter(this){ clickItem(it) } }

    override fun obtainBinding(): ActivityChooseServerBinding {
        return ActivityChooseServerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdCenter.preloadAd(AdPos.BACK)
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.rvList.apply {
            layoutManager=LinearLayoutManager(this@ChooseServerActivity)
            adapter=listAdapter
        }
    }

    private fun clickItem(serverBean: ServerBean){
        val current = ConnectServerManager.serverBean
        val isConnected = ConnectServerManager.isConnected()
        if (isConnected&&current.ip!=serverBean.ip){
            showDisconnectDialog {
                back("dis",serverBean)
            }
        }else{
            if (isConnected){
                back("",serverBean)
            }else{
                back("con",serverBean)
            }
        }
    }

    private fun back(back:String,serverBean: ServerBean){
        ConnectServerManager.serverBean=serverBean
        setResult(1123, Intent().apply {
            putExtra("back",back)
        })
        finish()
    }

    private fun showDisconnectDialog(sure:()->Unit){
        AlertDialog.Builder(this).apply {
            setMessage("You are currently connected and need to disconnect before manually connecting to the server.")
            setPositiveButton("sure") { _, _ ->
                sure.invoke()
            }
            setNegativeButton("cancel",null)
            show()
        }
    }

    override fun onBackPressed() {
        if(AdCenter.hasCached(AdPos.BACK)){
            AdCenter.loadAd(this, AdPos.BACK, object : AdsListener() {
                override fun onAdLoaded(ad: Ad) {
                    if (ad !is InterstitialAds) {
                        return
                    }
                    ad.show(this@ChooseServerActivity)
                }

                override fun onAdError(err: String?) {
                    finish()
                }

                override fun onAdDismiss() {
                    super.onAdDismiss()
                    finish()
                }
            })
            return
        }
        finish()
    }
}