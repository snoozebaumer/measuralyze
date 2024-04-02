package ch.hslu.measuralyze

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import ch.hslu.measuralyze.model.Configuration
import ch.hslu.measuralyze.model.Measurement

/**
 * SharedViewModel to share configuration and app state between different screens.
 */
class SharedViewModel: ViewModel() {
    private val _measurementData: MutableState<List<Measurement>> = mutableStateOf(emptyList())
    private val _stagesFormData: MutableState<List<String>> = mutableStateOf(emptyList())

    // Expose immutable State for observation
    val measurementData: State<List<Measurement>> = _measurementData
    val stagesFormData: State<List<String>> = _stagesFormData
    val config: Configuration = Configuration()

    fun addMeasurement(measurement: Measurement) {
        _measurementData.value += measurement
    }

    fun initConfigForm() {
        if(config.stages.isNotEmpty()) {
            _stagesFormData.value = config.stages
        } else {
            _stagesFormData.value = mutableListOf("Standard settings")
        }
    }

    fun saveConfig() {
        config.stages = _stagesFormData.value
    }

    fun addStage(stage: String) {
        _stagesFormData.value += stage
    }

    fun removeStage(index: Int) {
        _stagesFormData.value = _stagesFormData.value.toMutableList().apply { removeAt(index) }
    }

}