package com.tools.android.translator.ads

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateUtils
import com.tools.android.translator.App

/**
 * Created on 2022/4/29
 * Describe:
 */
class AdConfig {

    companion object {
        val ins: AdConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AdConfig() }
    }

    private val sp: SharedPreferences = App.ins.getSharedPreferences("ad_config_n", Context.MODE_PRIVATE)

    private val shown_prefix = "shown_total"
    private val click_prefix = "click_total"

    fun addShownCount() {
        val key = shown_prefix
        sp.edit().putInt(key, sp.getInt(key, 0) + 1).apply()
    }

    fun addClickCount() {
        val key = click_prefix
        sp.edit().putInt(key, sp.getInt(key, 0) + 1).apply()
    }

    fun getShownCount(): Int {
        return sp.getInt(shown_prefix, 0)
    }
    fun getClickCount(): Int {
        return sp.getInt(click_prefix, 0)
    }

    fun checkFirst() {
        /*val key = "last_clear_time"
        val lastDay = sp.getLong(key, 0L) / DateUtils.DAY_IN_MILLIS
        val now = System.currentTimeMillis() / DateUtils.DAY_IN_MILLIS
        val isSameDay = lastDay == now
        if (!isSameDay) {
            if (lastDay != 0L) {
                putLong(sp, timesShown, 0)
                putLong(sp, timesClick, 0)
            }
            putLong(sp, ifIsToday, System.currentTimeMillis())
        }
        return isSameDay*/

        val lastClearTms = lastClearTime
        if (lastClearTms > 0) {
            val lastDay = lastClearTms / DateUtils.DAY_IN_MILLIS
            val now = System.currentTimeMillis() / DateUtils.DAY_IN_MILLIS
            val isSameDay = lastDay == now
            if (!isSameDay) {
                sp.edit().putInt(shown_prefix, 0).apply()
                sp.edit().putInt(click_prefix, 0).apply()
                lastClearTime = System.currentTimeMillis()
            }
        } else {
            lastClearTime = System.currentTimeMillis()
        }
    }

    var lastClearTime: Long
        get() {
            return sp.getLong("last_clear_time", 0L) ?: 0L
        }
        set(value) {
            sp.edit().putLong("last_clear_time", value).apply()
        }
}