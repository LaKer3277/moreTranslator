package com.tools.android.translator.tba

import android.content.Context
import android.os.Build
import android.util.Log
import android.webkit.WebSettings
import com.tencent.mmkv.MMKV
import com.tools.android.translator.support.ReferrerManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

object TbaJson {
    var ip=""

    fun uploadTba(context:Context){
        getIp {
            GlobalScope.launch {
                uploadInstallJson(context,ip)
                uploadSessionJson(context,ip)
            }
        }
    }

    fun getIp(callback:()->Unit){
        if (ip.isEmpty()){
            OkGoManager.requestGet("https://api.myip.com/"){
                ip = parseIpJson(it)
                callback.invoke()
            }
        }else{
            callback.invoke()
        }
    }

    private fun uploadSessionJson(context: Context,ip:String){
        val commonJson = CommonJson.getCommonJson(context, ip)
        commonJson.put("bradley","virgin")
        OkGoManager.uploadEvent(commonJson,false)
    }

    private fun uploadInstallJson(context: Context,ip:String){
        if (!uploadHasReferrerTag() || !uploadNoReferrerTag()){
            ReferrerManager.getReferrerDetail {

                if (null==it&& uploadNoReferrerTag()){
                    return@getReferrerDetail
                }
                if (null!=it&& uploadHasReferrerTag()){
                    return@getReferrerDetail
                }
                val commonJson = CommonJson.getCommonJson(context, ip)
                commonJson.put("dunkirk", getBuild())
                commonJson.put("acerbic",it?.installReferrer?:"")
                commonJson.put("ovum",it?.installVersion?:"")
                commonJson.put("parr",it?.referrerClickTimestampSeconds?:0)
                commonJson.put("trauma",it?.installBeginTimestampSeconds?:0)
                commonJson.put("cabot",it?.referrerClickTimestampServerSeconds?:0)
                commonJson.put("prodigy",it?.installBeginTimestampServerSeconds?:0)
                commonJson.put("reb", getFirstInstallTime(context))
                commonJson.put("clay", getLastUpdateTime(context))
                commonJson.put("marlowe","spinal")
                commonJson.put("abel", getUserAgent(context))
                commonJson.put("bradley", "would")
                OkGoManager.uploadEvent(commonJson,true)
            }
        }
    }


    private fun parseIpJson(string:String):String{
        //    {"ip":"23.27.206.181","country":"United States","cc":"US"}
        try {
            ip=JSONObject(string).optString("ip")
            return ip
        }catch (e:Exception){

        }
        return ""
    }

    private fun getBuild():String = "build/${Build.VERSION.RELEASE}"

    private fun getUserAgent(context: Context) = WebSettings.getDefaultUserAgent(context)

    private fun getFirstInstallTime(context: Context):Long{
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.firstInstallTime
        }catch (e:java.lang.Exception){

        }
        return System.currentTimeMillis()
    }

    private fun getLastUpdateTime(context: Context):Long{
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.lastUpdateTime
        }catch (e:java.lang.Exception){

        }
        return System.currentTimeMillis()
    }

    fun saveNoReferrerTag(){
        MMKV.defaultMMKV().encode("moreNo",1)
    }

    private fun uploadNoReferrerTag()= MMKV.defaultMMKV().decodeInt("moreNo")==1

    fun saveHasReferrerTag(){
        MMKV.defaultMMKV().encode("moreHas",1)
    }

    private fun uploadHasReferrerTag()= MMKV.defaultMMKV().decodeInt("moreHas")==1
}