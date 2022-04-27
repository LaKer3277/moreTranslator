package com.tools.android.translator.support

import com.tools.android.translator.App

/**
 * Created on 2022/4/27
 * Describe:
 */
class RemoteConfig {

    companion object {
        val ins: RemoteConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { RemoteConfig() }
    }

    /*private val remoteConfig = Firebase.remoteConfig
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
    }*/

    fun getAdsConfig(): String {
        /*var config: String? = remoteConfig.getString("")
        if (config.isNullOrEmpty()) {
            return adLocal
        }*/
        return adLocal
    }

    private val adLocal = "{\n" +
            "    \"iTran_zks\":50,\n" +
            "    \"iTran_ydj\":15,\n" +
            "    \"iTran_sykp\":[\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-3940256099942544/3419835294\",\n" +
            "            \"odkg\":\"o\",\n" +
            "            \"nbm\":3\n" +
            "        },\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-3940256099942544/3419835294\",\n" +
            "            \"odkg\":\"o\",\n" +
            "            \"nbm\":2\n" +
            "        }\n" +
            "    ],\n" +
            "    \"iTran_syys\":[\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-3940256099942544/2247696110\",\n" +
            "            \"odkg\":\"n\",\n" +
            "            \"nbm\":3\n" +
            "        }\n" +
            "    ],\n" +
            "    \"iTran_tr\":[\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-3940256099942544/1033173712\",\n" +
            "            \"odkg\":\"i\",\n" +
            "            \"nbm\":3\n" +
            "        },\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-3940256099942544/1033173712\",\n" +
            "            \"odkg\":\"i\",\n" +
            "            \"nbm\":1\n" +
            "        }\n" +
            "    ]\n" +
            "}"
}