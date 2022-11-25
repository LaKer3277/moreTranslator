package com.tools.android.translator.server

import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager

class ServerBean(
    val zhang:String="",
    val mima:String="",
    val ip:String="",
    val guo:String="Faster server",
    val kou:Int=0,
    val cheng:String=""
) {
    fun isFast()=guo=="Faster server"&&ip.isEmpty()

    fun getServerId():Long{
        ProfileManager.getActiveProfiles()?.forEach {
            if (it.host==ip&&it.remotePort==kou){
                return it.id
            }
        }
        return 0L
    }

    fun createProfile(){
        val profile = Profile(
            id = 0L,
            name = "$guo - $cheng",
            host = ip,
            remotePort = kou,
            password = mima,
            method = zhang
        )

        var id: Long? = null
        ProfileManager.getActiveProfiles()?.forEach {
            if (it.remotePort == profile.remotePort && it.host == profile.host) {
                id = it.id
                return@forEach
            }
        }
        if (null == id) {
            ProfileManager.createProfile(profile)
        } else {
            profile.id = id!!
            ProfileManager.updateProfile(profile)
        }
    }
}