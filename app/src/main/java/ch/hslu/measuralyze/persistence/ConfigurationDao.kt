package ch.hslu.measuralyze.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.hslu.measuralyze.model.Configuration

@Dao
interface ConfigurationDao {
    @Query("SELECT * FROM configurations WHERE id = 1")
    fun getConfiguration(): Configuration?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfiguration(configuration: Configuration)
}