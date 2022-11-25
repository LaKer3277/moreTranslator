package com.tools.android.translator.support

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast
import com.tools.android.translator.R
import com.tools.android.translator.server.ServerBean
import java.lang.Exception

fun Context.toast(s: String){
    Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}

fun getServerLogo(serverBean: ServerBean):Int{
    return when(serverBean.guo){
        "HongKong"-> R.mipmap.hongkong
        "Australia"-> R.mipmap.australia
        "Belgium"-> R.mipmap.belgium
        "Brazil"-> R.mipmap.brazil
        "Canada"-> R.mipmap.canada
        "France"-> R.mipmap.france
        "Germany"-> R.mipmap.germany
        "India"-> R.mipmap.india
        "Ireland"-> R.mipmap.ireland
        "Italy"-> R.mipmap.italy
        "Japan"-> R.mipmap.japan
        "KoreaSouth"-> R.mipmap.koreasouth
        "Netherlands"-> R.mipmap.netherlands
        "New Zealand"-> R.mipmap.newzealand
        "Norway"-> R.mipmap.norway
        "Russian"-> R.mipmap.russianfederation
        "Singapore"-> R.mipmap.singapore
        "Sweden"-> R.mipmap.sweden
        "Switzerland"-> R.mipmap.switzerland
        "UnitedKingdom"-> R.mipmap.unitedkingdom
        "UnitedStates"-> R.mipmap.unitedstates
        else-> R.mipmap.fast
    }
}

fun Context.netStatus(): Int {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
        if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
            return 2
        } else if (activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE) {
            return 0
        }
    } else {
        return 1
    }
    return 1
}

fun transTime(t:Long):String{
    try {
        val shi=t/3600
        val fen= (t % 3600) / 60
        val miao= (t % 3600) % 60
        val s=if (shi<10) "0${shi}" else shi
        val f=if (fen<10) "0${fen}" else fen
        val m=if (miao<10) "0${miao}" else miao
        return "${s}:${f}:${m}"
    }catch (e: Exception){}
    return "00:00:00"
}
