package com.tools.android.translator.support

import android.widget.Toast
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.tools.android.translator.App

/**
 * Created on 2022/4/27
 * Describe:
 */
class RemoteConfig {

    companion object {
        val ins: RemoteConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { RemoteConfig() }
    }

    private val remoteConfig = FirebaseRemoteConfig.getInstance()
    init {
        if (!App.isRelease) {
            //如果是调试，启用开发者模式，以便可以频繁刷新缓存
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60).build()
            remoteConfig.setConfigSettingsAsync(configSettings)
        }
    }

    fun init() {
        fetchAndActivate {
            //更新完毕
        }
    }
    private fun fetchAndActivate(action: () -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                if (it.isSuccessful) action()
            }
    }

    fun getAdsConfig(): String {
        val config: String = remoteConfig.getString("iTranslator_configuration") ?: ""
        if (config.isNullOrEmpty()) {
            return adLocal
        }
        //Toast.makeText(App.ins, config, Toast.LENGTH_LONG).show()
        return adLocal
    }

    private val adLocal = "{\n" +
            "    \"iTran_zks\":50,\n" +
            "    \"iTran_ydj\":15,\n" +
            "    \"iTran_sykp\":[\n" +
            "        {\n" +
            "            \"xmca\":\"admob\",\n" +
            "            \"xmcd\":\"ca-app-pub-2201554157805547/6154675426\",\n" +
            "            \"xmco\":\"o\",\n" +
            "            \"xmcn\":3\n" +
            "        },\n" +
            "        {\n" +
            "            \"xmca\":\"admob\",\n" +
            "            \"xmcd\":\"ca-app-pub-2201554157805547/2897956699\",\n" +
            "            \"xmco\":\"o\",\n" +
            "            \"xmcn\":2\n" +
            "        },\n" +
            "        {\n" +
            "            \"xmca\":\"admob\",\n" +
            "            \"xmcd\":\"ca-app-pub-2201554157805547/9463365042\",\n" +
            "            \"xmco\":\"o\",\n" +
            "            \"xmcn\":1\n" +
            "        }\n" +
            "    ],\n" +
            "    \"iTran_syys\":[\n" +
            "        {\n" +
            "            \"xmca\":\"admob\",\n" +
            "            \"xmcd\":\"ca-app-pub-2201554157805547/1832287031\",\n" +
            "            \"xmco\":\"n\",\n" +
            "            \"xmcn\":3\n" +
            "        },\n" +
            "        {\n" +
            "            \"xmca\":\"admob\",\n" +
            "            \"xmcd\":\"ca-app-pub-2201554157805547/4566261580\",\n" +
            "            \"xmco\":\"n\",\n" +
            "            \"xmcn\":2\n" +
            "        },\n" +
            "        {\n" +
            "            \"xmca\":\"admob\",\n" +
            "            \"xmcd\":\"ca-app-pub-2201554157805547/8686836977\",\n" +
            "            \"xmco\":\"n\",\n" +
            "            \"xmcn\":1\n" +
            "        }\n" +
            "    ],\n" +
            "    \"iTran_tr\":[\n" +
            "        {\n" +
            "            \"xmca\":\"admob\",\n" +
            "            \"xmcd\":\"ca-app-pub-2201554157805547/1009665119\",\n" +
            "            \"xmco\":\"i\",\n" +
            "            \"xmcn\":3\n" +
            "        },\n" +
            "        {\n" +
            "            \"xmca\":\"admob\",\n" +
            "            \"xmcd\":\"ca-app-pub-2201554157805547/2678464841\",\n" +
            "            \"xmco\":\"i\",\n" +
            "            \"xmcn\":2\n" +
            "        },\n" +
            "        {\n" +
            "            \"xmca\":\"admob\",\n" +
            "            \"xmcd\":\"ca-app-pub-2201554157805547/5140481813\",\n" +
            "            \"xmco\":\"i\",\n" +
            "            \"xmcn\":1\n" +
            "        }\n" +
            "    ]\n" +
            "}"
}