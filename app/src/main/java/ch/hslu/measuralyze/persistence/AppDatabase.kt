package ch.hslu.measuralyze.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ch.hslu.measuralyze.model.Configuration
import ch.hslu.measuralyze.model.Measurement

@Database(entities = [Measurement::class, Configuration::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao
    abstract fun configurationDao(): ConfigurationDao
}
