package com.tools.android.translator

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.github.shadowsocks.Core
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.tencent.mmkv.MMKV
import com.tools.android.translator.ads.AdConfig
import com.tools.android.translator.ads.RefreshAd
import com.tools.android.translator.support.Devices
import com.tools.android.translator.support.RemoteConfig
import com.tools.android.translator.tba.TbaJson
import com.tools.android.translator.ui.HomeActivity
import com.tools.android.translator.ui.LoadingActivity
import com.tools.android.translator.ui.translate.MainActivity
import com.tools.android.translator.ui.translate.MainActivity.Companion.needFreshNav
import com.tools.android.translator.upload.Uploader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created on 2022/4/20
 * Describe:
 */
class App: Application() {

    companion object {
        lateinit var ins: App
        const val isRelease = false
    }

    val isAtomicStarting = AtomicBoolean(false)
    private lateinit var sp: SharedPreferences
    override fun onCreate() {
        super.onCreate()
        ins = this
        Core.init(this,HomeActivity::class)
        if (!packageName.equals(processName(this))){
            return
        }
        MMKV.initialize(this)
        sp = getSharedPreferences("iThan_config", Context.MODE_PRIVATE)

        MobileAds.initialize(this)
        RemoteConfig.ins.init()
        registerActivityLifecycleCallbacks(ActivityLifecycle())
        Uploader.ins.doStart()
        AdConfig.ins.checkFirst()
        TbaJson.uploadTba(this)
    }

    private fun processName(applicationContext: Application): String {
        val pid = android.os.Process.myPid()
        var processName = ""
        val manager = applicationContext.getSystemService(Application.ACTIVITY_SERVICE) as ActivityManager
        for (process in manager.runningAppProcesses) {
            if (process.pid === pid) {
                processName = process.processName
            }
        }
        return processName
    }


    //存在的前台Activity 数量
    private var nForeActivity = 0
    private var delayJob: Job? = null
    private var bHotLoading = false
    //屏蔽本次热启动
    private var blockHot = false

    var isHotLoad=false

    fun blockOnceHot() {
        blockHot = true
    }

    fun isAppForeground(): Boolean {
        return nForeActivity > 0
    }

    @SuppressLint("LogNotTimber")
    private inner class ActivityLifecycle: ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {
            Log.e("ProcessLifecycle", "onActivityStarted: $activity")
            nForeActivity++
            delayJob?.cancel()
            if ( nForeActivity== 1) {
                if (activity !is AdActivity
                    && activity !is LoadingActivity) {
                    if (!blockHot && bHotLoading) {
                        isAtomicStarting.set(true)
                        LoadingActivity.restart(activity)
                        needFreshNav = true
                        isHotLoad=true
                    }
                    blockHot = false
                    bHotLoading = false
                }
            }
        }

        override fun onActivityResumed(activity: Activity) { }

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
            Log.i("ProcessLifecycle", "onActivityStopped: $activity")
            --nForeActivity
            if (nForeActivity<=0){
                RefreshAd.resetAll()
                delayJob = GlobalScope.launch {
                    delay(2990L)
                    bHotLoading = true
                    if (activity is AdActivity || (activity is LoadingActivity && nForeActivity <= 0)) {
                        if (activity.isFinishing || activity.isDestroyed) return@launch
                        if (isAppForeground()) return@launch
                        //if (!atomicBackHome.get()) return@launch
                        Log.e("ProcessLifecycle", "finish: $activity")
                        activity.finish()
                    }
                }
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            Log.i("ProcessLifecycle", "onActivityDestroyed: $activity")
        }
    }

    //翻译的源语言码
    var sourceLa: String
        get() {
            return sp.getString("source_lan", "en") ?: "en"
        }
        set(value) {
            sp.edit().putString("source_lan", value).apply()
        }
    //翻译的目标语言码
    var targetLa: String
        get() {
            return sp.getString("target_lan", "en") ?: "en"
        }
        set(value) {
            sp.edit().putString("target_lan", value).apply()
        }

    //翻译的源历史语言码
    var sourceHistory: String
        get() {
            return sp.getString("source_history", "") ?: ""
        }
        set(value) {
            sp.edit().putString("source_history", value).apply()
        }
    //翻译的目标历史语言码
    var targetHistory: String
        get() {
            return sp.getString("source_history", "") ?: ""
        }
        set(value) {
            sp.edit().putString("source_history", value).apply()
        }

    val deviceId: String
        get() {
            val deviceIdKey = "device_id_n"
            var device = sp.getString(deviceIdKey, "")
            if (device.isNullOrEmpty()) {
                device = Devices.deviceId()
                sp.edit().putString(deviceIdKey, device).apply()
            }
            return device ?: ""
        }

    var localUserIp: String
        get() {
            return sp.getString("user_local_ip", "") ?: ""
        }
        set(value) {
            sp.edit().putString("user_local_ip", value).apply()
        }

    var firstCountry: String
        get() {
            return sp.getString("first_country", "") ?: ""
        }
        set(value) {
            sp.edit().putString("first_country", value).apply()
        }
}