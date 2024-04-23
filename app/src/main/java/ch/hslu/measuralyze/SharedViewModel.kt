package ch.hslu.measuralyze

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import ch.hslu.measuralyze.model.CellTowerInfo
import ch.hslu.measuralyze.model.Configuration
import ch.hslu.measuralyze.model.MeasureLocation
import ch.hslu.measuralyze.model.Measurement
import ch.hslu.measuralyze.model.WifiInfo
import ch.hslu.measuralyze.persistence.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    fun createCSVFile(context: Context, measurements: List<Measurement>): File {
        val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss") // 20240418_120102
        val file =
            File(context.cacheDir, "measurements_${LocalDateTime.now().format(dateFormat)}.csv")

        // check the max amount of cell towers in any measurement
        val maxCellTowers = measurements.maxOf { it.cellTowerInfo.size }

        // check the max amount of found wifi access points in any measurement
        val maxWifi = measurements.maxOf { it.wifiInfo.size }

        val writer = FileWriter(file)

        writer.append(getCSVHeader(maxCellTowers, maxWifi))

        writer.append(getCSVContent(measurements, maxCellTowers, maxWifi))

        writer.flush()
        writer.close()

        return file
    }

    fun shareCSV(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/csv"
        intent.putExtra(Intent.EXTRA_SUBJECT, "CSV File")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share CSV"))
    }

    fun deleteAllMeasurements() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                measurementDao.deleteAllMeasurements()
            }
            _measurementData.value = emptyList()
        }
    }

    /**
     * create the header for the csv file, containing all attributes of a measurement
     */
    private fun getCSVHeader(maxCellTowers: Int, maxWifi: Int): String {
        // create header for every single attribute, separated by a comma, mark the cell tower and wifi attributes with a number to indicate the amount of cell towers and wifi
        return "timestamp\tstage\tmeasureLocation\tactualLatitude\tactualLongitude\tgpsLatitude\tgpsLongitude\tgpsAccuracy\t${
            (1..maxCellTowers).joinToString(
                "\t"
            ) { CellTowerInfo.getCsvHeader(it) }
        }" + "\t" + "${(1..maxWifi).joinToString("\t") { WifiInfo.getCsvHeader(it) }}\twifiScanning\tbluetoothScanning\tairplaneMode\twifiEnabled\tbluetoothEnabled\tsimPresent\n"
    }

    /**
     * compile all measurements into a single string, separated by a tab, each measurement separated by a newline
     */
    private fun getCSVContent(measurements: List<Measurement>, maxCellTowers: Int, maxWifi: Int): String {
        val str: StringBuilder = StringBuilder()
        measurements.forEach { measurement ->
            str.append("${measurement.timeStamp}\t${measurement.stage}\t${measurement.measureLocation.description}\t${measurement.measureLocation.latitude}\t${measurement.measureLocation.longitude}\t${measurement.gpsPosition.latitude}\t${measurement.gpsPosition.longitude}\t${measurement.gpsPosition.accuracy}\t")

            str.append(getCellTowerInfoCSV(measurement, maxCellTowers))

            str.append(getWifiInfoCSV(measurement, maxWifi))

            str.append("${measurement.systemSettings.isWifiScanningEnabled}\t${measurement.systemSettings.isBluetoothScanningEnabled}\t${measurement.systemSettings.isAirplaneModeEnabled}\t${measurement.systemSettings.isWifiEnabled}\t${measurement.systemSettings.isBluetoothEnabled}\t${measurement.systemSettings.isSimPresent}\n")
        }
        return str.toString()
    }

    /**
     * create a string with all cell tower information for current measurement, seperated by tab. if there are less cell towers than the max amount, fill the remaining columns with empty strings
     */
    private fun getCellTowerInfoCSV(measurement: Measurement, maxCellTowers: Int): String {
        return (1..maxCellTowers).joinToString("\t") {
            if (it <= measurement.cellTowerInfo.size) {
                val cellTowerInfo = measurement.cellTowerInfo[it - 1]
                cellTowerInfo.toCsvString()
            } else {
                "\t\t\t\t\t\t"
            }
        } + "\t"
    }

    /**
     * create a string with all wifi information for current measurement, seperated by tab. if there are less wifi than the max amount, fill the remaining columns with empty strings
     */
    private fun getWifiInfoCSV(measurement: Measurement, maxWifi: Int): String {
        return (1..maxWifi).joinToString("\t") {
            if (it <= measurement.wifiInfo.size) {
                val wifiInfo = measurement.wifiInfo[it - 1]
                wifiInfo.toCsvString()
            } else {
                "\t\t\t"
            }
        } + "\t"
    }

}