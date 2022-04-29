package com.tools.android.translator.ads

/**
 * Created on 2022/4/26
 * Describe:
 */
class ConfigPos(val pos: AdPos, val ids: ArrayList<ConfigId>) {

    fun isEmpty(): Boolean {
        return ids.isNullOrEmpty()
    }
}