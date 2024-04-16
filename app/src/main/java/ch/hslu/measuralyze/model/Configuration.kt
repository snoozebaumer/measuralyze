package ch.hslu.measuralyze.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configurations")
data class Configuration(
    @PrimaryKey val id: Int = 1,
    var stages: List<String> = listOf("Standard settings"),
    var iterations: Int = 20,
    var measurementIntervalInMs: Int = 1000,
    var measureLocations: List<MeasureLocation> = listOf(
        MeasureLocation("Standard location", 0.0, 0.0),
    )
)