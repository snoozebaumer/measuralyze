package ch.hslu.measuralyze.persistence

import androidx.room.TypeConverter
import ch.hslu.measuralyze.model.CellTowerInfo
import ch.hslu.measuralyze.model.GpsPosition
import ch.hslu.measuralyze.model.MeasureLocation
import ch.hslu.measuralyze.model.SystemSettings
import ch.hslu.measuralyze.model.WifiInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Converters {
    @TypeConverter
    @JvmStatic
    fun fromGpsPosition(value: GpsPosition?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toGpsPosition(value: String): GpsPosition {
        return Gson().fromJson(value, GpsPosition::class.java)
    }

    @TypeConverter
    @JvmStatic
    fun fromWifiInfoList(value: List<WifiInfo>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toWifiInfoList(value: String): List<WifiInfo> {
        val listType = object : TypeToken<List<WifiInfo>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun fromLocalDateTime(value: java.time.LocalDateTime?): Long? {
        return value?.toEpochSecond(java.time.ZoneOffset.UTC)
    }

    @TypeConverter
    @JvmStatic
    fun toLocalDateTime(value: Long?): java.time.LocalDateTime? {
        return value?.let { java.time.LocalDateTime.ofEpochSecond(it, 0, java.time.ZoneOffset.UTC) }
    }

    @TypeConverter
    @JvmStatic
    fun fromSystemSettings(value: SystemSettings?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toSystemSettings(value: String): SystemSettings {
        return Gson().fromJson(value, SystemSettings::class.java)
    }

    @TypeConverter
    @JvmStatic
    fun fromCellTowerInfoList(value: List<CellTowerInfo>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toCellTowerInfoList(value: String): List<CellTowerInfo> {
        val listType = object : TypeToken<List<CellTowerInfo>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun fromMeasureLocationList(value: List<MeasureLocation>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toMeasureLocationList(value: String): List<MeasureLocation> {
        val listType = object : TypeToken<List<MeasureLocation>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun fromStringList(value: List<String>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
