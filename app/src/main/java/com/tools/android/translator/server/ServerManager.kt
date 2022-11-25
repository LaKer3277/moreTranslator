package com.tools.android.translator.server

import com.tools.android.translator.support.RemoteConfig
import org.json.JSONObject

object ServerManager {
    private val cityList= arrayListOf<String>()
    private val serverList= arrayListOf<ServerBean>()

    fun getAllServerList()=serverList.ifEmpty { RemoteConfig.ins.localServerList }

    fun getFastServer():ServerBean{
        val serverList = getAllServerList()
        if (cityList.isNullOrEmpty()){
            return serverList.random()
        }else{
            val filter = serverList.filter { cityList.contains(it.cheng) }
            if (!filter.isNullOrEmpty()){
                return filter.random()
            }
        }
        return serverList.random()
    }


    fun createOrUpdateProfile(list:ArrayList<ServerBean>){
        for (serverInfoBean in list) {
            serverInfoBean.createProfile()
        }
    }

    fun readCityConfig(string: String){
        try {
            val jsonArray = JSONObject(string).getJSONArray("itr_fat")
            for (index in 0 until jsonArray.length()){
                cityList.add(jsonArray.optString(index))
            }
        }catch (e:Exception){

        }
    }

    fun readServerConfig(string: String){
        try {
            val jsonArray = JSONObject(string).getJSONArray("itr_ser")
            for (index in 0 until jsonArray.length()){
                val json = jsonArray.getJSONObject(index)
                val serverInfoBean = ServerBean(
                    json.optString("zhang"),
                    json.optString("mima"),
                    json.optString("ip"),
                    json.optString("guo"),
                    json.optInt("kou"),
                    json.optString("cheng"),
                )
                serverList.add(serverInfoBean)
            }
            createOrUpdateProfile(serverList)
        }catch (e:Exception){

        }
    }
}