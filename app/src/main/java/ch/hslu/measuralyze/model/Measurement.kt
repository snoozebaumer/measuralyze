package ch.hslu.measuralyze.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var timeStamp: java.time.LocalDateTime,
    var stage: String = "",
    var gpsPosition: GpsPosition = GpsPosition(0.0,0.0,0f),
    var cellTowerInfo: List<CellTowerInfo> = mutableListOf(),
    var wifiInfo: List<WifiInfo> = mutableListOf(),
    var systemSettings: SystemSettings = SystemSettings()
) {
    override fun toString(): String {
        return "Measurement(timestamp=${timeStamp} stage=$stage gpsPosition=$gpsPosition, cellTowerInfo=$cellTowerInfo, wifiInfo=$wifiInfo, systemSettings=$systemSettings)"
    }
}