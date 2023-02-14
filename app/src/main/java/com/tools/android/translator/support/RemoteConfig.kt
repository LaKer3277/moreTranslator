package com.tools.android.translator.support

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.tencent.mmkv.MMKV
import com.tools.android.translator.App
import com.tools.android.translator.server.ServerBean
import com.tools.android.translator.server.ServerManager

/**
 * Created on 2022/4/27
 * Describe:
 */
class RemoteConfig {
    //  - both-如果用户没有在连接状态，冷热启动都出现（热启动：退出到后台5s后）
    //  - cold-仅在冷启动出现
    var itrPopShow="cold"
    var itrV="2"
    var isShowingGuideDialog=false
    var iTranslatorSet="2"

    companion object {
        val ins: RemoteConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { RemoteConfig() }
    }

//    private val remoteConfig = Firebase.remoteConfig
    init {
//        if (!App.isRelease) {
//            //如果是调试，启用开发者模式，以便可以频繁刷新缓存
//            val configSettings = FirebaseRemoteConfigSettings.Builder()
//                .setMinimumFetchIntervalInSeconds(60).build()
//            remoteConfig.setConfigSettingsAsync(configSettings)
//        }
    }

    fun init() {
        ServerManager.createOrUpdateProfile(localServerList)

        fetchAndActivate {
            //更新完毕
        }
    }
    private fun fetchAndActivate(action: () -> Unit) {
//        remoteConfig.fetchAndActivate()
//            .addOnCompleteListener {
//                if (it.isSuccessful) {
//                    ServerManager.readServerConfig(remoteConfig.getString("itr_ser"))
//                    ServerManager.readCityConfig(remoteConfig.getString("itr_fat"))
//                    val itr_popshow=remoteConfig.getString("itr_popshow")
//                    if (itr_popshow.isNotEmpty()){
//                        itrPopShow=itr_popshow
//                    }
//                    val itr_v = remoteConfig.getString("itr_v")
//                    if (itr_v.isNotEmpty()){
//                        itrV=itr_v
//                    }
//
//                    val itranslator_set = remoteConfig.getString("itranslator_set")
//                    if (itranslator_set.isNotEmpty()){
//                        iTranslatorSet=itranslator_set
//                    }
//
//                    action()
//                }
//            }
    }



    fun getAdsConfig(): String {
        /*var config: String? = remoteConfig.getString("")
        if (config.isNullOrEmpty()) {
            return adLocal
        }*/
        return adLocal
    }


    val localServerList= arrayListOf(
        ServerBean(
            ip = "100.223.52.0",
            mima = "123456",
            guo = "Japan",
            cheng = "Tokyo",
            kou = 100,
            zhang = "chacha20-ietf-poly1305"
        ),
        ServerBean(
            ip = "100.223.52.78",
            mima = "123456",
            guo = "UnitedStates",
            cheng = "newyork",
            kou = 100,
            zhang = "chacha20-ietf-poly1305"
        )
    )


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
            "    \"iTran_home\":[\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-3940256099942544/2247696110\",\n" +
            "            \"odkg\":\"n\",\n" +
            "            \"nbm\":3\n" +
            "        }\n" +
            "    ],\n" +
            "    \"itr_hm\":[\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-3940256099942544/2247696110\",\n" +
            "            \"odkg\":\"n\",\n" +
            "            \"nbm\":3\n" +
            "        }\n" +
            "    ],\n" +
            "    \"itr_result\":[\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-3940256099942544/2247696110\",\n" +
            "            \"odkg\":\"n\",\n" +
            "            \"nbm\":3\n" +
            "        }\n" +
            "    ],\n" +
            "    \"itr_link\":[\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-3940256099942544/1033173712\",\n" +
            "            \"odkg\":\"i\",\n" +
            "            \"nbm\":3\n" +
            "        }\n" +
            "    ],\n" +
            "    \"itr_return\":[\n" +
            "        {\n" +
            "            \"dls\":\"admob\",\n" +
            "            \"ltof\":\"ca-app-pub-3940256099942544/1033173712\",\n" +
            "            \"odkg\":\"i\",\n" +
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