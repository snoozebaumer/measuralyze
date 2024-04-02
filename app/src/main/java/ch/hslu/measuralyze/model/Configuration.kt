package ch.hslu.measuralyze.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configurations")
data class Configuration(
    @PrimaryKey val id: Int = 1,
    var stages: List<String> = listOf("Standard settings")
)