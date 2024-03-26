package ch.hslu.measuralyze.service

import android.net.wifi.WifiManager
import android.os.Build
import android.os.RemoteException
import android.util.Log
import ch.hslu.measuralyze.model.WifiInfo
import java.security.InvalidParameterException

class WifiScanService private constructor(private val wifiManager: WifiManager) {
    companion object {
        private var wifiScanService: WifiScanService? = null

        @Throws(InvalidParameterException::class)
        fun getWifiScanService(wifiManager: WifiManager? = null): WifiScanService {
            if (wifiScanService === null) {
                if (wifiManager === null) {
                    throw InvalidParameterException("cellTowerService doesn't exist and telephonyManager was not provided")
                }
                wifiScanService = WifiScanService(wifiManager)
            }
            return wifiScanService as WifiScanService
        }
    }


    @Throws(RemoteException::class)
    fun fetchCurrentValues(onWifInfoFetched: (List<WifiInfo>) -> Unit) {
        try {
            val wifiInfoList: List<WifiInfo> = wifiManager.scanResults.map { scanResult ->
                WifiInfo().apply {
                    ssid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        scanResult.wifiSsid.toString()
                    } else {
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