package com.tools.android.translator.upload.http

/**
 * Created on 2022/4/27
 * Describe:
 */
interface IHttpCallback {

    fun onSuccess(code: Int, msg: String?)
    fun onFailed(code: Int, msg: String?)

}