package com.twomax.live.data.device

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("device_id", Context.MODE_PRIVATE)

    fun getMacAddress(): String {
        val cached = prefs.getString("mac_address", null)
        if (cached != null) return cached

        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(androidId.toByteArray())

        // Take first 6 bytes and format as MAC address
        val mac = hash.take(6).joinToString(":") { byte ->
            String.format("%02X", byte)
        }

        prefs.edit().putString("mac_address", mac).apply()
        return mac
    }
}
