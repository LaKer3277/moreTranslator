package com.tools.android.translator.translate

import java.util.*

/**
 * Created on 2022/4/20
 * Describe:
 * Holds the language code (i.e. "en") and the corresponding localized full language name (i.e.
 * "English")
 */
data class Language(
    val code: String = "en",
    val displayName: String = Locale(code).displayName,
    var available: Int = -1  //-1: 不可用; 0:下载中; 1:可用
) : Comparable<Language> {

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }

        if (other !is Language) {
            return false
        }

        val otherLang = other as Language?
        return otherLang?.code == code
    }

    override fun toString(): String {
        return "$code - $displayName"
    }

    override fun compareTo(other: Language): Int {
        return this.displayName.compareTo(other.displayName)
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }

    fun isAvailable(): Boolean {
        return available == 1
    }

    fun isUnavailable(): Boolean {
        return available == -1
    }

    fun isDownloading(): Boolean {
        return available == 0
    }
}