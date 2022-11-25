package com.tools.android.translator.server

import com.tools.android.translator.interfaces.ITimerCallback
import com.tools.android.translator.support.transTime
import kotlinx.coroutines.*

object TimeManager {
    private var time=0L
    private var job:Job?=null
    private var iTimerCallback:ITimerCallback?=null

    fun resetTime(){
        time=0L
    }

    fun start(){
        if (null!= job) return
        job = GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                iTimerCallback?.connectTime(transTime(time))
                time++
                delay(1000L)
            }
        }
    }

    fun stop(){
        job?.cancel()
        job=null
    }

    fun setTimerCallback(iTimerCallback:ITimerCallback?){
        this.iTimerCallback=iTimerCallback
    }
}