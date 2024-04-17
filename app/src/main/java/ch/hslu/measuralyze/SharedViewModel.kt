package ch.hslu.measuralyze

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import ch.hslu.measuralyze.model.Configuration
import ch.hslu.measuralyze.model.MeasureLocation
import ch.hslu.measuralyze.model.Measurement
import ch.hslu.measuralyze.persistence.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SharedViewModel to share configuration and app state between different screens.
 */
class SharedViewModel(context: Context) : ViewModel() {
    private val standardLocationName: String = "Standard location"
    private val standardStageName: String = "Standard settings"

    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "app-database"
    ).build()

    private val measurementDao = database.measurementDao()
    private val configurationDao = database.configurationDao()

    private val _measurementData: MutableState<List<Measurement>> = mutableStateOf(emptyList())

    // Form data for configuration screen
    private val _stagesFormData: MutableState<List<String>> = mutableStateOf(emptyList())
    private val _measureLocationsFormData: MutableState<List<MeasureLocation>> =
        mutableStateOf(emptyList())
    val iterationsFormData: MutableState<Int> = mutableIntStateOf(20)
    val measurementIntervalInMsFormData: MutableState<Int> = mutableIntStateOf(1000)

    // Expose immutable State for observation
    val measurementData: State<List<Measurement>> = _measurementData
    val stagesFormData: State<List<String>> = _stagesFormData
    val measureLocationsFormData: State<List<MeasureLocation>> = _measureLocationsFormData
    val config: Configuration = Configuration()

    var configFormDirty: Boolean = false

    val currentMeasureStage = mutableIntStateOf(0)
    val currentMeasureLocation: MutableState<MeasureLocation> = mutableStateOf(
        MeasureLocation(standardLocationName, 0.0, 0.0)
    )
    val measuring: MutableState<Boolean> = mutableStateOf(false)

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                configurationDao.getConfiguration()?.let { configurationEntity ->
                    val stages = configurationEntity.stages
                    val measureLocations = configurationEntity.measureLocations
                    val iterations = configurationEntity.iterations
                    val measurementIntervalInMs = configurationEntity.measurementIntervalInMs
                    withContext(Dispatchers.Main) {
                        config.stages = stages
                        config.measureLocations = measureLocations
                        config.iterations = iterations
                        config.measurementIntervalInMs = measurementIntervalInMs
                        _stagesFormData.value = stages
                        _measureLocationsFormData.value = measureLocations
                        currentMeasureLocation.value = measureLocations.first()
                        iterationsFormData.value = iterations
                        measurementIntervalInMsFormData.value = measurementIntervalInMs
                    }
                }
            }
        }

        if (config.stages.isNotEmpty()) {
            _stagesFormData.value = config.stages
        } else {
            _stagesFormData.value = mutableListOf(standardStageName)
        }

        if (config.measureLocations.isNotEmpty()) {
            _measureLocationsFormData.value = config.measureLocations
        } else {
            _measureLocationsFormData.value =
                mutableListOf(MeasureLocation(standardLocationName, 0.0, 0.0))
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

        if (config.stages.isNotEmpty()) {
            _stagesFormData.value = config.stages
        } else {
            _stagesFormData.value = mutableListOf(standardStageName)
        }

        if (config.measureLocations.isNotEmpty()) {
            _measureLocationsFormData.value = config.measureLocations
        } else {
            _measureLocationsFormData.value =
                mutableListOf(MeasureLocation(standardLocationName, 0.0, 0.0))
        }

        iterationsFormData.value = config.iterations
        measurementIntervalInMsFormData.value = config.measurementIntervalInMs
    }

    fun saveConfig() {
        config.stages = _stagesFormData.value
        config.iterations = iterationsFormData.value
        config.measurementIntervalInMs = measurementIntervalInMsFormData.value
        config.measureLocations = _measureLocationsFormData.value

        // if a user deletes a stage while they are measuring, reset current stage to 0 to make sure it is still valid, else the application will crash
        if (currentMeasureStage.intValue >= config.stages.size) {
            currentMeasureStage.intValue = 0
        }

        if (currentMeasureLocation.value !in config.measureLocations) {
            currentMeasureLocation.value = config.measureLocations.first()
        }

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

    fun addLocation(location: MeasureLocation) {
        _measureLocationsFormData.value += location
    }

    fun removeLocation(index: Int) {
        _measureLocationsFormData.value =
            _measureLocationsFormData.value.toMutableList().apply { removeAt(index) }
    }

    fun hasUnsavedConfigChanges(): Boolean {
        return config.stages != _stagesFormData.value || config.iterations != iterationsFormData.value || config.measurementIntervalInMs != measurementIntervalInMsFormData.value || config.measureLocations != _measureLocationsFormData.value
    }

}