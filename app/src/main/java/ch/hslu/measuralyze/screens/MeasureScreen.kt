package ch.hslu.measuralyze.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.hslu.measuralyze.components.measure.MeasureButton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.time.format.DateTimeFormatter

@Composable
fun MeasureScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var buttonText by remember { mutableStateOf("Start measurement") }
    var buttonColor by remember { mutableStateOf(Color.LightGray) }
    var gpsPosition by remember { mutableStateOf("") }
    var currentDateTime by remember { mutableStateOf("") }

    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted, fetch the GPS position
                fetchGPSPosition(fusedLocationClient) { position ->
                    gpsPosition = position
                    currentDateTime = java.time.LocalDateTime.now().toString()
                }
            } else {
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
                        gpsPosition = position
                        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        currentDateTime = java.time.LocalDateTime.now().format(dateFormat)
                        buttonText = "Start measurement"
                        buttonColor = Color.LightGray
                    }
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    buttonText = "Permission failure, press to try again"
                    buttonColor = Color.LightGray
                }
            }
        }

        Text(
            text = "GPS Position: $gpsPosition\nDate/Time: $currentDateTime",
            modifier = Modifier.padding(top = 16.dp),
            style = TextStyle(fontSize = 16.sp)
        )
    }
}

@SuppressLint("MissingPermission")
fun fetchGPSPosition(fusedLocationClient: FusedLocationProviderClient, onPositionFetched: (String) -> Unit) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100).apply {
        setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
        setWaitForAccurateLocation(true)
    }.build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                val position = "Lat: ${location.latitude}, Long: ${location.longitude}"
                onPositionFetched(position)
                // Stop receiving updates after position is fetched
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
}

@Preview(showBackground = true)
@Composable
fun MeasureScreenPreview() {
    MeasureScreen()
}
