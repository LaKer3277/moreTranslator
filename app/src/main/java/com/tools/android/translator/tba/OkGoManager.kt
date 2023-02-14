package com.tools.android.translator.tba

import android.util.Log
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import org.json.JSONObject

object OkGoManager {
    private const val URL="https://test-crises.itranslator.org/groin/trivial"

    fun requestGet(url:String,result:(json:String)-> Unit){
        OkGo.get<String>(url).execute(object : StringCallback(){
            override fun onSuccess(response: Response<String>?) {
                result(response?.body().toString())
            }
        })
    }

    fun uploadEvent(jsonObject:JSONObject,install:Boolean){
        val path="$URL?waste=${CommonJson.getLogId()}&thee=${CommonJson.getOsCountry()}&kelly=${CommonJson.getManufacturer()}"
        Log.e("qwer","==uploadEvent===${jsonObject.toString()}")
        OkGo.post<String>(path)
            .retryCount(3)
            .headers("content-type","application/json")
            .headers("xerxes", CommonJson.getSystemLanguage())
            .upJson(jsonObject)
            .execute(object :StringCallback(){
                override fun onSuccess(response: Response<String>?) {
                    if (install){
                        if (jsonObject.optString("acerbic").isEmpty()){
                            TbaJson.saveNoReferrerTag()
                        }else{
                            TbaJson.saveHasReferrerTag()
                        }
                    }
                    Log.e("qwer","=onSuccess==${response?.body()?.toString()}==")

                }

                override fun onError(response: Response<String>?) {
                    super.onError(response)
                    Log.e("qwer","=onError==${response?.body()?.toString()}==")
                }
            })
    }
}