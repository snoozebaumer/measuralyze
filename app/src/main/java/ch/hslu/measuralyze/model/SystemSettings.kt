package ch.hslu.measuralyze.model

data class SystemSettings(
    var isWifiScanningEnabled: Boolean = false,
    var isBluetoothScanningEnabled: Boolean = false,
    var isAirplaneModeEnabled: Boolean = false,
    var isWifiEnabled: Boolean = false,
    var isBluetoothEnabled: Boolean = false,
    var isSimPresent: Boolean = false
) {

    override fun toString(): String {
        return "SystemSettings(isWifiScanningEnabled=$isWifiScanningEnabled, isBluetoothScanningEnabled=$isBluetoothScanningEnabled, isAirplaneModeEnabled=$isAirplaneModeEnabled, isWifiEnabled=$isWifiEnabled, isBluetoothEnabled=$isBluetoothEnabled, isSimPresent=$isSimPresent)"
    }
}