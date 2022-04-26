package com.tools.android.translator.support

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.tools.android.translator.App

/**
 * Created on 2022/4/26
 * Describe:
 */
object GpConsole {

    fun skip2Market(pkgName: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse(
                "https://play.google.com/store/apps/details?id=$pkgName")
            if (hasGooglePlay()) {
                setPackage("com.android.vending")
            }
        }
        try {
            App.ins.startActivity(intent)
        } catch (e: Exception) {
            openUrlByBrowser("https://play.google.com/store/apps/details?id=$pkgName")
        }
    }

    fun hasGooglePlayEnv(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return (resultCode == ConnectionResult.SUCCESS) && hasGooglePlay()
    }

    private fun hasGooglePlay(): Boolean {
        return try {
            App.ins.packageManager.getApplicationInfo("com.android.vending", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openUrlByBrowser(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            App.ins.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}