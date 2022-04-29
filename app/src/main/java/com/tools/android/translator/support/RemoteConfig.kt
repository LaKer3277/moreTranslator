package com.tools.android.translator.support

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
        return adLocal
    }

    private val adLocal = "{\n" +
            "    \"iTran_zks\":50,\n" +
            "    \"iTran_ydj\":15,\n" +
            "    \"iTran_sykp\":[\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-2201554157805547/2767361425\",\n" +
            "            \"odkg\":\"o\",\n" +
            "            \"nbm\":3\n" +
            "        },\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-2201554157805547/9001597284\",\n" +
            "            \"odkg\":\"o\",\n" +
            "            \"nbm\":2\n" +
            "        },\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-2201554157805547/7496943929\",\n" +
            "            \"odkg\":\"o\",\n" +
            "            \"nbm\":1\n" +
            "        }\n" +
            "    ],\n" +
            "    \"iTran_syys\":[\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-2201554157805547/1344816156\",\n" +
            "            \"odkg\":\"n\",\n" +
            "            \"nbm\":3\n" +
            "        },\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-2201554157805547/1669902174\",\n" +
            "            \"odkg\":\"n\",\n" +
            "            \"nbm\":2\n" +
            "        },\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-2201554157805547/4104493828\",\n" +
            "            \"odkg\":\"n\",\n" +
            "            \"nbm\":1\n" +
            "        }\n" +
            "    ]\n" +
            "}"
}