package com.tools.android.translator.translate

import com.tools.android.translator.R

/**
 * Created on 2022/4/22
 * Describe:
 */
val languageList = arrayListOf<Language>(
    Language("en", available = 1),
    Language("de"),
    Language("ar"),
    Language("fr"),
    Language("ja"),
    Language("ko"),
    Language("pl"),
    Language("es"),
    Language("pt"),
    Language("zh"),
)

fun findResourceByCode(code: String): Int {
    return when (code) {
        "de" -> R.mipmap.language_de
        "ar" -> R.mipmap.language_ar
        "fr" -> R.mipmap.language_fr
        "ja" -> R.mipmap.language_jp
        "ko" -> R.mipmap.language_kr
        /*"pl" -> R.mipmap.language_pl
        "es" -> R.mipmap.language_es
        "pt" -> R.mipmap.language_pt*/
        "zh" -> R.mipmap.language_zh
        else -> R.mipmap.language_en
    }
}