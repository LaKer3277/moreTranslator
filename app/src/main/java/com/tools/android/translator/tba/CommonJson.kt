package com.tools.android.translator.tba

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object CommonJson {

    fun getCommonJson(context: Context,ip: String):JSONObject{
        val json=JSONObject()
        json.put("handout",getHandoutJson(context))
        json.put("halcyon",getHalcyon(context,ip))
        return json
    }

    private fun getHandoutJson(context: Context):JSONObject{
        val json=JSONObject()
        json.put("alluvium", getCpuName())
        json.put("thee", getOsCountry())
        json.put("kelly", getManufacturer())
        json.put("especial", getCpuName())
        json.put("bisect", getScreenRes(context))
        json.put("elliott", "gig")
        json.put("xerxes", getSystemLanguage())
        json.put("city", getAndroidId(context))
        return json
    }

    private fun getHalcyon(context: Context,ip:String):JSONObject{
        val json=JSONObject()
        json.put("approval", getDistinctId(context))
        json.put("begging", getOperator(context))
        json.put("swirly", getNetworkType(context))
        json.put("carriage", getBrand())
        json.put("waste", getLogId())
        json.put("aphasic", getOsVersion())
        json.put("feisty", getGaid(context))
        json.put("bayesian", getAppVersion(context))
        json.put("gasket", ip)
        json.put("anarchy", getDeviceModel())
        json.put("myron", getBundleId(context))
        json.put("podium", getZoneOffset())
        json.put("giraffe", getClientTs())
        json.put("cutlass", getKey())
        return json
    }

    private fun getCpuName()=android.os.Build.CPU_ABI

    fun getOsCountry()= Locale.getDefault().country

    fun getManufacturer()= Build.MANUFACTURER

    private fun getScreenRes(context: Context):String{
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.density.toString()
    }

    fun getSystemLanguage():String{
        val default = Locale.getDefault()
        return "${default.language}_${default.country}"
    }

    private fun getAndroidId(context: Context): String {
        try {
            val id: String = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
            )
            return if ("9774d56d682e549c" == id) "" else id ?: ""
        }catch (e:Exception){

        }
        return ""
    }

    private fun getDistinctId(context: Context)= encrypt(getAndroidId(context))

    private fun getOperator(context: Context):String{
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.networkOperator
        }catch (e:Exception){

        }
        return ""
    }

    private fun getNetworkType(context: Context):String{
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                    return "wifi"
                } else if (activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                    return "mobile"
                }
            } else {
                return "no"
            }
            return "no"
        }catch (ex:Exception){

        }
        return "no"
    }

    private fun getBrand()= android.os.Build.BRAND

    fun getLogId()= UUID.randomUUID().toString()

    private fun getOsVersion()=Build.VERSION.RELEASE

    private fun getGaid(context: Context)=try {
        AdvertisingIdClient.getAdvertisingIdInfo(context).id
    }catch (e:Exception){
        Log.e("qwer","===${e.message}")
        ""
    }

    private fun getAppVersion(context: Context)=context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_META_DATA).versionName

    private fun getDeviceModel()=Build.MODEL

    private fun getBundleId(context: Context)=context.packageName

    private fun getZoneOffset()=TimeZone.getDefault().rawOffset/3600/1000

    private fun getClientTs()=System.currentTimeMillis()

    private fun getKey()= UUID.randomUUID().toString()

    private fun encrypt(raw: String): String {
        var md5Str = raw
        try {
            val md = MessageDigest.getInstance("MD5")
            md.update(raw.toByteArray())
            val encryContext = md.digest()
            var i: Int
            val buf = StringBuffer("")
            for (offset in encryContext.indices) {
                i = encryContext[offset].toInt()
                if (i < 0) {
                    i += 256
                }
                if (i < 16) {
                    buf.append("0")
                }
                buf.append(Integer.toHexString(i))
            }
            md5Str = buf.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return md5Str
    }
}