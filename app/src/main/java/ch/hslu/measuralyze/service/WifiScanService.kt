package ch.hslu.measuralyze.service

import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import ch.hslu.measuralyze.model.WifiInfo

class WifiScanService(private val wifiManager: WifiManager) {
    fun fetchCurrentValues(onWifInfoFetched: (List<WifiInfo>) -> Unit) {
        try {
            val wifiInfoList: List<WifiInfo> = wifiManager.scanResults.map { scanResult ->
                WifiInfo().apply {
                    ssid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        scanResult.wifiSsid.toString()
                    } else {
                        // supressing deprecation warning because this is the only way to get the SSID on older versions and we explicitly check version
                        @Suppress("DEPRECATION")
                        scanResult.SSID
                    }
                    bssid = scanResult.BSSID
                    rssi = scanResult.level
                    frequency = scanResult.frequency
                }
            }
            onWifInfoFetched(wifiInfoList)
        } catch (e: SecurityException) {
            Log.e("WifiScanService", "Permission to access wifi information denied")
            onWifInfoFetched(ArrayList())
        }
    }

}