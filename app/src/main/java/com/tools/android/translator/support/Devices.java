package com.tools.android.translator.support;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import com.tools.android.translator.App;

import java.security.MessageDigest;
import java.util.UUID;

/**
 * Created on 2022/4/28
 * Describe:
 */
public class Devices {

    public static String androidIdOrUUid(Context context) {
        String androidID = getAndroidID(context);
        if (androidID == null || androidID.isEmpty() || androidID.equals("9774d56d682e549c")) {
            return UUID.randomUUID().toString();
        }

        return androidID;
    }

    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String deviceId() {
        String iid = androidIdOrUUid(App.ins);
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(iid.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String tem = Integer.toHexString(b & 0xff);
                if (tem.length() == 1) {
                    tem = "0" + tem;
                }
                result.append(tem);
            }
            return result.toString();
        } catch (Exception e) {
        }

        return iid;
    }
}
