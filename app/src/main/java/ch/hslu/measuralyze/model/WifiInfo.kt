package ch.hslu.measuralyze.model

/**
 * more concise version of android.wifi.net.ScanResult
 * */
class WifiInfo {
    /**
     * the readable name of the network
     */
    var ssid: String = ""

    /**
     * the unique identifier of the network
     */
    var bssid: String = ""

    /**
     * the signal strength of the network
     */
    var rssi: Int = 0

    /**
     * the frequency at which the network operates
     */
    var frequency: Int = 0

    override fun toString(): String {
        return "WifiInfo(ssid='$ssid', bssid='$bssid', rssi=$rssi, frequency=$frequency)"
    }

    companion object {
        fun getCsvHeader(numberInWifiList: Int): String {
            return "wifiSsid$numberInWifiList\twifiBssid$numberInWifiList\twifiRssi$numberInWifiList\twifiFrequency$numberInWifiList"
        }
    }

    fun toCsvString(): String {
        return "$ssid\t$bssid\t$rssi\t$frequency"
    }
}