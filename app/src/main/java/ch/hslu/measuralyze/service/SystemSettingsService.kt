package ch.hslu.measuralyze.service

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import ch.hslu.measuralyze.model.SystemSettings
import java.security.InvalidParameterException

class SystemSettingsService private constructor(private val contentResolver: ContentResolver, private val telephonyManager: TelephonyManager) {
    private var systemSettings: SystemSettings? = null
    private val observers: MutableList<ContentObserver> = mutableListOf()

    companion object {
        private var systemSettingsService: SystemSettingsService? = null

        @Throws(InvalidParameterException::class)
        fun getSystemSettingsService(contentResolver: ContentResolver? = null, telephonyManager: TelephonyManager? = null): SystemSettingsService {
            if (systemSettingsService == null) {
                if (contentResolver == null || telephonyManager == null) {
                    throw InvalidParameterException("systemSettingsService doesn't exist and contentResolver or telephonyManager was not provided")
                }
                systemSettingsService = SystemSettingsService(contentResolver, telephonyManager)
            }
            return systemSettingsService as SystemSettingsService
        }
    }

    init {
        startListeningForChangesForCachedSettings()
    }

    fun fetchSystemSettings(onSystemSettingsFetched: (SystemSettings) -> Unit) {
        if (observers.isEmpty()) {
            startListeningForChangesForCachedSettings()
        }
        updateSystemSettings()
        systemSettings?.let { onSystemSettingsFetched(it) }
    }

    private fun updateSystemSettings() {
        systemSettings = SystemSettings()
        systemSettings!!.isBluetoothScanningEnabled = isBluetoothScanningEnabled()
        systemSettings!!.isWifiScanningEnabled = isWifiScanningEnabled()
        systemSettings!!.isAirplaneModeEnabled = isAirplaneModeEnabled()
        systemSettings!!.isWifiEnabled = isWifiEnabled()
        systemSettings!!.isBluetoothEnabled = isBluetoothEnabled()
        systemSettings!!.isSimPresent = isSimPresent()
    }

    private fun isWifiScanningEnabled(): Boolean {
        return try {
            Settings.Global.getInt(
                contentResolver,
                "wifi_scan_always_enabled"
            ) != 0
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

    private fun isBluetoothScanningEnabled(): Boolean {
        return try {
            Settings.Global.getInt(
                contentResolver,
                "ble_scan_always_enabled"
            ) != 0
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

    private fun isAirplaneModeEnabled(): Boolean {
        return try {
            Settings.System.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

    private fun isWifiEnabled(): Boolean {
        return try {
            Settings.Global.getInt(contentResolver, Settings.Global.WIFI_ON, 0) != 0
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        return try {
            Settings.Global.getInt(contentResolver, Settings.Global.BLUETOOTH_ON, 0) != 0
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

    private fun isSimPresent(): Boolean {
        return telephonyManager.simState == TelephonyManager.SIM_STATE_READY
    }

    fun startListeningForChangesForCachedSettings() {
        if (observers.isNotEmpty()) {
            stopListeningForChangesForCachedSettings()
        }
        registerObserver(Settings.Global.getUriFor("ble_scan_always_enabled"))
        registerObserver(Settings.Global.getUriFor("wifi_scan_always_enabled"))
    }

    private fun registerObserver(uri: Uri?) {
        uri?.let {
            val observer = object :  ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    updateSystemSettings()
                }
            }
            contentResolver.registerContentObserver(uri, true, observer)
            observers.add(observer)
        }
    }

    fun stopListeningForChangesForCachedSettings() {
        for (observer in observers) {
            contentResolver.unregisterContentObserver(observer)
        }
        observers.clear()
    }
}
