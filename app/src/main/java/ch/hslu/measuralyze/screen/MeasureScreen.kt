package ch.hslu.measuralyze.screen

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.hslu.measuralyze.component.measure.MeasureButton
import ch.hslu.measuralyze.model.Measurement
import ch.hslu.measuralyze.service.MeasureService
import java.time.format.DateTimeFormatter

@Composable
fun MeasureScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var buttonText by remember { mutableStateOf("Start measurement") }
    var buttonColor by remember { mutableStateOf(Color.LightGray) }
    val measurementList = remember { mutableStateListOf<Measurement>() }

    val measureService = MeasureService(context)

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Measure",
                modifier = Modifier.padding(bottom = 16.dp),
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
            MeasureButton(text = buttonText, color = buttonColor) {
                buttonText = "Measuring"
                buttonColor = Color(0xFFADD8E6)

                if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    measureService.fetchMeasurement { measurement ->
                        measurementList.add(measurement)
                        buttonText = "Start measurement"
                        buttonColor = Color.LightGray
                    }
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    buttonText = "Permission failure"
                    buttonColor = Color.LightGray
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(start = 16.dp)
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
                            text = "GPS Position: ${measurement.gpsPosition}\nDate/Time: ${
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

@Preview(showBackground = true)
@Composable
fun MeasureScreenPreview() {
    MeasureScreen()
}
