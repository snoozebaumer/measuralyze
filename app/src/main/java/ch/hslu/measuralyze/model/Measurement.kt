package ch.hslu.measuralyze.model

data class Measurement constructor(var timeStamp: java.time.LocalDateTime, var latitude: Double, var longitude: Double, var accuracy: Float) {

    var gpsPosition: GpsPosition = GpsPosition(latitude, longitude, accuracy)
    var cellTowerInfo: List<CellTowerInfo> = mutableListOf()
    var wifiInfo: List<WifiInfo> = mutableListOf()
    var systemSettings: SystemSettings = SystemSettings()

    override fun toString(): String {
        return "Measurement(timestamp=${timeStamp} gpsPosition=$gpsPosition, cellTowerInfo=$cellTowerInfo, wifiInfo=$wifiInfo, systemSettings=$systemSettings)"
    }
}