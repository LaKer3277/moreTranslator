package com.tools.android.translator

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.tools.android.translator.support.RemoteConfig

/**
 * Created on 2022/4/20
 * Describe:
 */
class App: Application() {

    companion object {
        lateinit var ins: App
        const val isRelease = false
    }

    private lateinit var sp: SharedPreferences
    override fun onCreate() {
        super.onCreate()
        ins = this
        sp = getSharedPreferences("iThan_config", Context.MODE_PRIVATE)

        //RemoteConfig.ins.init()
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
}