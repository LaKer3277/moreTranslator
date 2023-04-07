package com.tools.android.translator.support

import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.webkit.WebView
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.tencent.mmkv.MMKV
import com.tools.android.translator.App
import com.tools.android.translator.server.ServerBean
import com.tools.android.translator.server.ServerManager
import org.json.JSONObject
import java.util.*

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
    var isLimitUser=false
    private var abItranslator="80"
    var planType=""

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
        checkIsLimitUser()
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

                    val ab_itranslator = remoteConfig.getString("ab_itranslator")
                    if (ab_itranslator.isNotEmpty()){
                        abItranslator=ab_itranslator
                    }

                    action()
                }
            }
    }

    fun randomPlanType(){
        if(planType.isEmpty()){
            val nextInt = Random().nextInt(100)
            planType = if (str2Int(abItranslator)>=nextInt) "B" else "A"
        }
    }

    private fun checkIsLimitUser(){

        OkGo.get<String>("https://ipapi.co/json")
            .headers("User-Agent", WebView(App.ins).settings.userAgentString)
            .execute(object : StringCallback(){
                override fun onSuccess(response: Response<String>?) {
                    try {
                        isLimitUser = JSONObject(response?.body()?.toString()).optString("country_code").limitArea()
                    }catch (e:Exception){

                    }
                }
            })
    }

    private fun String.limitArea()=contains("IR")||contains("MO")||contains("HK")||contains("CN")


    fun getAdsConfig(): String {
        var config: String? = remoteConfig.getString("iTranslator_configuration")
        if (config.isNullOrEmpty()) {
            return adLocal
        }
        return config
    }

    fun Context.copy(string: String){
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setText(string)
    }


    val localServerList= arrayListOf(
        ServerBean(
            ip = "38.86.135.92",
            mima = "5doHkcC2oYYPVCYy",
            guo = "United States",
            cheng = "Ashburn",
            kou = 3852,
            zhang = "chacha20-ietf-poly1305"
        )
    )

    private val adLocal = """{
  "iTran_zks": 30,
  "iTran_ydj": 5,
  "iTran_sykp": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/7216279844",
      "xmco": "o",
      "xmcn": 3
    }
  ],
  "iTran_syys": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/2883155306",
      "xmco": "n",
      "xmcn": 3
    }
  ],
  "iTran_tr": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/9548636001",
      "xmco": "i",
      "xmcn": 3
    }
  ],
  "iTran_home": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/5609390990",
      "xmco": "n",
      "xmcn": 3
    }
  ],
  "itr_hm": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/9357064313",
      "xmco": "n",
      "xmcn": 3
    }
  ],
  "itr_result": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/6730900971",
      "xmco": "n",
      "xmcn": 3
    }
  ],
  "itr_link": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/7943910290",
      "xmco": "i",
      "xmcn": 3
    }
  ],
  "itr_return": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/2791655961",
      "xmco": "i",
      "xmcn": 3
    }
  ],
  "iTran_2back": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/2465187059",
      "xmco": "i",
      "xmcn": 3
    }
  ],
  "iTran_4back": [
    {
      "xmca": "admob",
      "xmcd": "ca-app-pub-2201554157805547/1996576908",
      "xmco": "i",
      "xmcn": 3
    }
  ]
}"""
}