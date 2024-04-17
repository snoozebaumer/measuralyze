package ch.hslu.measuralyze.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.hslu.measuralyze.SharedViewModel
import ch.hslu.measuralyze.component.measure.MeasureButton
import ch.hslu.measuralyze.model.MeasureLocation
import ch.hslu.measuralyze.service.MeasureService
import java.time.format.DateTimeFormatter

@Composable
fun MeasureScreen(modifier: Modifier = Modifier, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    var buttonText by remember { mutableStateOf("Start measurement") }
    var buttonColor by remember { mutableStateOf(Color.LightGray) }
    val measurementList = sharedViewModel.measurementData.value

    val measureService = MeasureService(context)

    fun makeMeasurementsForCurrentStage() {
        var measurementCount = 0
        val totalIterations = sharedViewModel.config.iterations
        val measurementInterval = sharedViewModel.config.measurementIntervalInMs.toLong()

        val stages = sharedViewModel.config.stages
        val currentStageIndex = sharedViewModel.currentMeasureStage.intValue

        fun fetchNextMeasurement() {
            if (measurementCount < totalIterations) {
                measureService.fetchMeasurement { measurement ->
                    measurement.stage = stages[currentStageIndex]
                    measurement.measureLocation = sharedViewModel.currentMeasureLocation.value
                    sharedViewModel.addMeasurement(measurement)
                    measurementCount++
                    buttonText = "Measuring ${measurementCount}/${totalIterations}"
                    if (measurementCount == totalIterations) {
                        buttonText = "Start measurement"
                        buttonColor = Color.LightGray
                        sharedViewModel.measuring.value = false
                        val nextStageIndex = (currentStageIndex + 1) % stages.size
                        sharedViewModel.currentMeasureStage.intValue = nextStageIndex
                    } else {
                        // Schedule the next measurement
                        Handler(Looper.getMainLooper()).postDelayed({
                            fetchNextMeasurement()
                        }, measurementInterval)
                    }
                }
            }
        }

        // Start fetching measurements
        buttonText = "Measuring 0/${totalIterations}"
        buttonColor = Color(0xFFADD8E6)
        sharedViewModel.measuring.value = true
        fetchNextMeasurement()
    }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
                buttonText = "Permission failure"
                buttonColor = Color.LightGray
            } else {
                makeMeasurementsForCurrentStage()
            }
        }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.Absolute.Right,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.End) {
                LocationDropDown(
                    sharedViewModel.measureLocationsFormData,
                    sharedViewModel.currentMeasureLocation
                ) {
                    sharedViewModel.currentMeasureLocation.value = it
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Stage: " + if (sharedViewModel.stagesFormData.value.isNotEmpty()) sharedViewModel.stagesFormData.value[sharedViewModel.currentMeasureStage.intValue] else "Measure",
                    modifier = Modifier.padding(bottom = 16.dp),
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                )

                MeasureButton(text = buttonText, color = buttonColor) {
                    buttonText = "Measuring"
                    buttonColor = Color(0xFFADD8E6)

                    if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        makeMeasurementsForCurrentStage()
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(
                        rememberScrollState()
                    )
                    .weight(weight = 1f, fill = false)
            ) {
                if (measurementList.isNotEmpty()) {
                    //TODO: outsource to different view where measurementList is passed in, with Table: https://github.com/sunny-chung/composable-table
                    for (measurement in measurementList) {
                        if (measurement.gpsPosition.longitude != 0.toDouble()) {
                            val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            Text(
                                text = "${measurement.id}: GPS Position: ${measurement.gpsPosition}\nDate/Time: ${
                                    measurement.timeStamp.format(
                                        dateFormat
                                    )
                                }",
                                modifier = Modifier.padding(top = 16.dp),
                            )
                        }

                        for (cellTower in measurement.cellTowerInfo) {
                            Text(
                                text = "ECI: ${cellTower.cid}\nLAC: ${cellTower.lac}\nSignal: ${cellTower.rsrp}",
                                modifier = Modifier.padding(top = 16.dp),
                            )
                        }

                        for (wifiInfo in measurement.wifiInfo) {
                            Text(
                                text = "SSID: ${wifiInfo.ssid}\nBSSID: ${wifiInfo.bssid}\nSignal strength: ${wifiInfo.rssi}",
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                        Text(
                            text = "System settings: ${measurement.systemSettings}",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropDown(
    locations: State<List<MeasureLocation>>,
    selectedLocation: State<MeasureLocation>,
    onLocationSelected: (MeasureLocation) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded }) {
            TextField(
                modifier = Modifier.menuAnchor(),
                value = selectedLocation.value.description,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = isExpanded
                    )
                })

            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                locations.value.forEach { measureLocation ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(text = measureLocation.description)
                                Text(text = "(${measureLocation.latitude}, ${measureLocation.longitude})", style = TextStyle(fontSize = 12.sp))
                            }
                        },
                        onClick = {
                            onLocationSelected(measureLocation)
                            isExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}
