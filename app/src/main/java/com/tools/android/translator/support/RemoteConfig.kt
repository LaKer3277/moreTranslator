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

    private val remoteConfig = Firebase.remoteConfig
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
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ServerManager.readServerConfig(remoteConfig.getString("itr_ser"))
                    ServerManager.readCityConfig(remoteConfig.getString("itr_fat"))
                    val itr_popshow=remoteConfig.getString("itr_popshow")
                    if (itr_popshow.isNotEmpty()){
                        itrPopShow=itr_popshow
                    }
                    val itr_v = remoteConfig.getString("itr_v")
                    if (itr_v.isNotEmpty()){
                        itrV=itr_v
                    }

                    val itranslator_set = remoteConfig.getString("itranslator_set")
                    if (itranslator_set.isNotEmpty()){
                        iTranslatorSet=itranslator_set
                    }

                    action()
                }
            }
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
            ip = "192.54.56.156",
            mima = "qLLEjorvbDD8sta2YTcl",
            guo = "Switzerland",
            cheng = "Zurich",
            kou = 4740,
            zhang = "chacha20-ietf-poly1305"
        ),
        ServerBean(
            ip = "51.161.153.248",
            mima = "qLLEjorvbDD8sta2YTcl",
            guo = "Australia",
            cheng = "Sydney",
            kou = 4740,
            zhang = "chacha20-ietf-poly1305"
        )
    )


    private val adLocal = """{
  "iTran_zks": 50,
  "iTran_ydj": 15,
  "iTran_sykp": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/6154675426",
      "xmco": "o",
      "xmcn": 3
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/2897956699",
      "xmco": "o",
      "xmcn": 2
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/9463365042",
      "xmco": "o",
      "xmcn": 1
    }
  ],
  "iTran_syys": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/1832287031",
      "xmco": "n",
      "xmcn": 3
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/4566261580",
      "xmco": "n",
      "xmcn": 2
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/8686836977",
      "xmco": "n",
      "xmcn": 1
    }
  ],
  "iTran_tr": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/1009665119",
      "xmco": "i",
      "xmcn": 3
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/2678464841",
      "xmco": "i",
      "xmcn": 2
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/5140481813",
      "xmco": "i",
      "xmcn": 1
    }
  ],
  "itr_hm": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/8602134545",
      "xmco": "n",
      "xmcn": 3
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/4262847551",
      "xmco": "n",
      "xmcn": 2
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/5763136640",
      "xmco": "n",
      "xmcn": 1
    }
  ],
  "itr_result": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/7435805808",
      "xmco": "n",
      "xmcn": 3
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/4618070772",
      "xmco": "n",
      "xmcn": 2
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/6593497424",
      "xmco": "n",
      "xmcn": 1
    }
  ],
  "itr_link": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/8006156609",
      "xmco": "i",
      "xmcn": 3
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/3852370743",
      "xmco": "i",
      "xmcn": 2
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/2347717384",
      "xmco": "n",
      "xmcn": 1
    }
  ],
  "itr_return": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/4782309033",
      "xmco": "i",
      "xmcn": 3
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/5827210669",
      "xmco": "i",
      "xmcn": 2
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/8261802312",
      "xmco": "i",
      "xmcn": 1
    }
  ],
  "iTran_home": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/1469274624",
      "xmco": "n",
      "xmcn": 3
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/1469274624",
      "xmco": "n",
      "xmcn": 2
    },
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/9020461589",
      "xmco": "n",
      "xmcn": 1
    }
  ]
}"""
}