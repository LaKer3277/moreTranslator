package com.tools.android.translator.ads

object RefreshAd {
    private val map= hashMapOf<String,Boolean>()

    fun canRefresh(pos:AdPos)=map[pos.toString()]?:true

    fun setValue(pos:AdPos,bool:Boolean){
        map[pos.toString()]=bool
    }

    fun resetAll(){
        map.keys.forEach {
            map[it]=true
        }
    }
}