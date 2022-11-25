package com.tools.android.translator.server

import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.preference.DataStore
import com.tools.android.translator.base.BaseActivity
import com.tools.android.translator.interfaces.IServerConnectCallback
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object ConnectServerManager : ShadowsocksConnection.Callback {
    private var base:BaseActivity?=null
    private var state = BaseService.State.Stopped
    var serverBean= ServerBean()
    var lastServer= ServerBean()
    private val sc= ShadowsocksConnection(true)
    private var iServerConnectCallback:IServerConnectCallback?=null

    fun init(baseActivity: BaseActivity,iServerConnectCallback:IServerConnectCallback){
        this.base=baseActivity
        this.iServerConnectCallback=iServerConnectCallback
        sc.connect(baseActivity,this)
    }

    fun connect(){
        state= BaseService.State.Connecting
        GlobalScope.launch {
            if (serverBean.isFast()){
                DataStore.profileId = ServerManager.getFastServer().getServerId()
            }else{
                DataStore.profileId = serverBean.getServerId()
            }
            Core.startService()
        }
        TimeManager.resetTime()
    }

    fun disconnect(){
        state= BaseService.State.Stopping
        GlobalScope.launch {
            Core.stopService()
        }
    }

    fun isConnected()=state==BaseService.State.Connected

    fun isStopped()=state==BaseService.State.Stopped

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        this.state=state
        if (isConnected()){
            lastServer= serverBean
            TimeManager.start()
        }
        if (isStopped()){
            TimeManager.stop()
            iServerConnectCallback?.disconnectSuccess()
        }
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        val state = BaseService.State.values()[service.state]
        this.state=state
        if (isConnected()){
            lastServer= serverBean
            TimeManager.start()
            iServerConnectCallback?.connectSuccess()
        }
    }

    override fun onBinderDied() {
        base?.let {
            sc.disconnect(it)
        }
    }

    fun onDestroy(){
        onBinderDied()
        base=null
        iServerConnectCallback=null
    }

}