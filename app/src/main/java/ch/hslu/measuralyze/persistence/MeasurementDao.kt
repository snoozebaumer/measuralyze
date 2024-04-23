package ch.hslu.measuralyze.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ch.hslu.measuralyze.model.Measurement
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements")
    fun getAllMeasurements(): Flow<List<Measurement>>

    @Insert
    suspend fun insertMeasurement(measurement: Measurement)

    @Query("DELETE FROM measurements")
    fun deleteAllMeasurements()
}
