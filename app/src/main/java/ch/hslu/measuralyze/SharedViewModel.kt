package ch.hslu.measuralyze

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import ch.hslu.measuralyze.model.Configuration
import ch.hslu.measuralyze.model.Measurement
import ch.hslu.measuralyze.persistence.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SharedViewModel to share configuration and app state between different screens.
 */
class SharedViewModel(context: Context) : ViewModel() {
    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "app-database"
    ).build()

    private val measurementDao = database.measurementDao()
    private val configurationDao = database.configurationDao()
    private val _measurementData: MutableState<List<Measurement>> = mutableStateOf(emptyList())
    private val _stagesFormData: MutableState<List<String>> = mutableStateOf(emptyList())

    var configFormDirty: Boolean = false

    // Expose immutable State for observation
    val measurementData: State<List<Measurement>> = _measurementData
    val stagesFormData: State<List<String>> = _stagesFormData
    val config: Configuration = Configuration()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                configurationDao.getConfiguration()?.let { configurationEntity ->
                    val stages = configurationEntity.stages
                    withContext(Dispatchers.Main) {
                        config.stages = stages
                        _stagesFormData.value = stages
                    }
                }
            }
        }
        viewModelScope.launch {
            measurementDao.getAllMeasurements().collect { measurements ->
                _measurementData.value = measurements
            }
        }
    }

    fun addMeasurement(measurement: Measurement) {
        _measurementData.value += measurement
        viewModelScope.launch {
            measurementDao.insertMeasurement(measurement)
        }
    }

    fun initConfigForm() {
        if (configFormDirty) {
            return
        }

        if(config.stages.isNotEmpty()) {
            _stagesFormData.value = config.stages
        } else {
            _stagesFormData.value = mutableListOf("Standard settings")
        }
    }

    fun saveConfig() {
        config.stages = _stagesFormData.value
        viewModelScope.launch {
            configurationDao.insertOrUpdateConfiguration(config)
        }
        configFormDirty = false
    }

    fun addStage(stage: String) {
        _stagesFormData.value += stage
    }

    fun removeStage(index: Int) {
        _stagesFormData.value = _stagesFormData.value.toMutableList().apply { removeAt(index) }
    }

    fun hasUnsavedConfigChanges(): Boolean {
        return config.stages != _stagesFormData.value
    }

}