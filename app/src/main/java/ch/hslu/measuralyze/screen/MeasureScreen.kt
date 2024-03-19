package ch.hslu.measuralyze.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Looper
import android.telephony.TelephonyManager
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
import androidx.core.content.ContextCompat.getSystemService
import ch.hslu.measuralyze.component.measure.MeasureButton
import ch.hslu.measuralyze.model.CellTowerInfo
import ch.hslu.measuralyze.model.GpsPosition
import ch.hslu.measuralyze.model.Measurement
import ch.hslu.measuralyze.service.CellTowerService
import ch.hslu.measuralyze.service.WifiScanService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.time.format.DateTimeFormatter

@Composable
fun MeasureScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var buttonText by remember { mutableStateOf("Start measurement") }
    var buttonColor by remember { mutableStateOf(Color.LightGray) }
    var cellTowers by remember { mutableStateOf(ArrayList<CellTowerInfo>()) }
    val measurementList = remember { mutableStateListOf<Measurement>() }

    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    val cellTowerService: CellTowerService = CellTowerService.getCellTowerService(
        getSystemService(
            context,
            TelephonyManager::class.java
        ) as TelephonyManager
    )
    val wifiScanService: WifiScanService = WifiScanService.getWifiScanService(
        getSystemService(
            context,
            WifiManager::class.java
        ) as WifiManager
    )

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
                    fetchGPSPosition(fusedLocationClient) { position ->
                        val measurement = Measurement(java.time.LocalDateTime.now(), position.latitude, position.longitude, position.accuracy)
                        buttonText = "Start measurement"
                        buttonColor = Color.LightGray

                        cellTowerService.fetchCurrentValues() { cellTowerInfo ->
                            cellTowers = cellTowerInfo
                            measurement.cellTowerInfo = cellTowerInfo
                        }
                        wifiScanService.fetchCurrentValues { wifiInfo ->
                            measurement.wifiInfo = wifiInfo
                            measurementList.add(measurement)
                        }
                    }
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    buttonText = "Permission failure, press to try again"
                    buttonColor = Color.LightGray
                }
            }
        }

        Column(horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(start = 16.dp)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(
                    rememberScrollState())
                .weight(weight = 1f, fill = false)) {
            if (measurementList.isNotEmpty()) {
                //TODO: outsource to different view where measurementList is passed in, with Table: https://github.com/sunny-chung/composable-table
                if (measurementList[0].gpsPosition.longitude != 0.toDouble()) {
                    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    Text(
                        text = "GPS Position: ${measurementList[0].gpsPosition}\nDate/Time: ${
                            measurementList[0].timeStamp.format(
                                dateFormat
                            )
                        }",
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }

                for (cellTower in measurementList[0].cellTowerInfo) {
                Text(
                    text = "ECI: ${cellTower.cid}\nLAC: ${cellTower.lac}\nSignal: ${cellTower.rsrp}",
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

                for (wifiInfo in measurementList[0].wifiInfo) {
                    Text(
                        text = "SSID: ${wifiInfo.ssid}\nBSSID: ${wifiInfo.bssid}\nSignal strength: ${wifiInfo.rssi}",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

            }
        }
    }
}

@SuppressLint("MissingPermission")
fun fetchGPSPosition(
    fusedLocationClient: FusedLocationProviderClient,
    onPositionFetched: (GpsPosition) -> Unit
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100).apply {
        setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
        setWaitForAccurateLocation(true)
    }.build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                val position = GpsPosition(
                    location.latitude,
                    location.longitude,
                    location.accuracy
                )
                onPositionFetched(position)
                // Stop receiving updates after position is fetched
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
}

@Preview(showBackground = true)
@Composable
fun MeasureScreenPreview() {
    MeasureScreen()
}
