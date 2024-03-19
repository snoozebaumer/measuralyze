package ch.hslu.measuralyze.model

data class Measurement constructor(var timeStamp: java.time.LocalDateTime, var latitude: Double, var longitude: Double, var accuracy: Float) {

    var gpsPosition: GpsPosition = GpsPosition(latitude, longitude, accuracy)
    var cellTowerInfo: List<CellTowerInfo> = mutableListOf()
    var wifiInfo: List<WifiInfo> = mutableListOf()

    override fun toString(): String {
        return "Measurement(cellTowerInfo=$cellTowerInfo, wifiInfo=$wifiInfo)"
    }
}