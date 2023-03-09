package com.tools.android.translator.support

import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.tencent.mmkv.MMKV
import com.tools.android.translator.App

object ReferrerManager {

     fun readReferrer(){
         if(readLocalReferrer().isEmpty()){
             val referrerClient = InstallReferrerClient.newBuilder(App.ins).build()
             referrerClient.startConnection(object : InstallReferrerStateListener {
                 override fun onInstallReferrerSetupFinished(responseCode: Int) {
                     try {
                         when (responseCode) {
                             InstallReferrerClient.InstallReferrerResponse.OK -> {
                                 val installReferrer = referrerClient.installReferrer.installReferrer
                                 writeReferrer(installReferrer)
                             }
                         }
                     } catch (e: Exception) {

                     }
                     try {
                         referrerClient.endConnection()
                     }catch (e:Exception){

                     }
                 }
                 override fun onInstallReferrerServiceDisconnected() {
                 }
             })
         }
     }

    fun getReferrerDetail(callback:(details: ReferrerDetails?)->Unit){
        val referrerClient = InstallReferrerClient.newBuilder(App.ins).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                try {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            callback.invoke(referrerClient.installReferrer)
                        }
                        else->{
                            callback.invoke(null)
                        }
                    }
                } catch (e: Exception) {

                }
                try {
                    referrerClient.endConnection()
                }catch (e:Exception){

                }
            }
            override fun onInstallReferrerServiceDisconnected() {
            }
        })
    }

    private fun readLocalReferrer()= MMKV.defaultMMKV().decodeString("referrer", "")?:""

    private fun writeReferrer(string: String){
        MMKV.defaultMMKV().encode("referrer",string)
    }

    fun isBuyUser():Boolean {
        val local = readLocalReferrer()
        return local.contains("fb4a")||local.contains("gclid")||
                local.contains("not%20set")||local.contains("youtubeads")||local.contains("%7B%22")
    }

    fun isFB():Boolean {
        val local = readLocalReferrer()
        return local.contains("facebook")||local.contains("fb4a")
    }

    fun canShowInterstitialAd()=when(RemoteConfig.ins.iTranslatorSet){
        "1"-> true
        "2"-> isBuyUser()
        "3"-> isFB()
        else-> false
    }
}