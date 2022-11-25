package com.tools.android.translator.support

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object setPoint {
//    private val remoteConfig=Firebase.analytics

    fun point(name:String){
        Log.i("AdCenter","set point:$name")
//        remoteConfig.logEvent(name, Bundle())
    }
}