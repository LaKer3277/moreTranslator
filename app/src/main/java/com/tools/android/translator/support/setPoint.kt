package com.tools.android.translator.support

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object setPoint {
    private val remoteConfig=Firebase.analytics

    fun point(name:String,key:String="",value:String=""){
//        Log.i("AdCenter","set point:$name=====$key====$value")
        val bundle = Bundle()
        if(key.isNotEmpty()){
            bundle.putString(key,value)
        }
        remoteConfig.logEvent(name, bundle)
    }

    fun setUserType(type:String=RemoteConfig.ins.planType.toLowerCase()){
//        Log.i("AdCenter","set point user :$type")
        remoteConfig.setUserProperty("itranslator_t",type)
    }
}