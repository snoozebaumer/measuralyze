package ch.hslu.measuralyze

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import ch.hslu.measuralyze.ui.theme.MeasuralyzeTheme

class MainActivity : ComponentActivity() {
    private var bluetoothScanningSettingEnabled = false
    private lateinit var contentObserver: ContentObserver
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, fetch and display information
                displayDeviceInfo()
            } else {
                // Permission denied, handle accordingly
                // You may want to inform the user or take appropriate action
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLocationPermission()
        setContent {
            MeasuralyzeTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Samuel")
                }
            }
        }
    }
    private fun requestLocationPermission() {
        when {
            // Check if the app has location permissions
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permissions already granted, fetch and display information
                displayDeviceInfo()
            }
            else -> {
                // Request location permission
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun displayDeviceInfo() {
        // Accessing TelephonyManager for cell tower information
        val telephonyManager = getSystemService(this, TelephonyManager::class.java) as TelephonyManager

        val tac = telephonyManager.networkOperator
        val eci = telephonyManager.allCellInfo.toString()
        val power = telephonyManager.signalStrength?.level
        // val band = telephonyManager.networkType

        // Accessing WifiManager for Wi-Fi information
        val wifiManager = getSystemService(this, WifiManager::class.java) as WifiManager
        val wifiScanResults: List<ScanResult> = wifiManager.scanResults

        val isWifiScanningEnabled = wifiManager.isScanAlwaysAvailable
        bluetoothScanningSettingEnabled = isBluetoothScanningSettingEnabled()

        // Now you have the information, you can display it or use it as needed
        // For simplicity, I'll just log the information here
        Log.d("DeviceInfo", "ECI: $eci, TAC: $tac, Power: $power")
        Log.d("DeviceInfo", "Wifi Scan Results: $wifiScanResults")
        Log.d("DeviceInfo", "Is Wifi Scanning Enabled: $isWifiScanningEnabled")
        Log.d("DeviceInfo", "Is Bluetooth Scanning Enabled: $bluetoothScanningSettingEnabled")
    }

    /*
    Settings are always cached for some reason, so if i change the setting and immediately open the app, it's gonna display the old value.
    that's why code (observeBluetoothScanningSetting) below exists
     */
    private fun isBluetoothScanningSettingEnabled(): Boolean {
        return try {
            val bluetoothScanningSetting = Settings.Global.getInt(
                contentResolver,
                "ble_scan_always_enabled"
            )
            Log.d("DeviceInfo", "BLE_SCAN value: $bluetoothScanningSetting")
            bluetoothScanningSetting == 1
        } catch (e: Settings.SettingNotFoundException) {
            Log.d("DeviceInfo", "BLE not found :(((")
            false
        }
    }

    /*
    subscribe to any and all changes to setting ble_scan_always_enabled
     */
    private fun observeBluetoothScanningSetting() {
        val contentResolver = contentResolver
        val uri = Settings.Global.getUriFor("ble_scan_always_enabled")

        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                // The setting has changed, update the UI or take appropriate action
                displayDeviceInfo()
            }
        }

        contentResolver.registerContentObserver(uri, true, contentObserver)
    }

    override fun onResume() {
        super.onResume()
        // Register the ContentObserver when user returns to app
        observeBluetoothScanningSetting()
    }

    override fun onPause() {
        super.onPause()
        // Unregister the ContentObserver when user leaves app
        contentResolver.unregisterContentObserver(contentObserver)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Surface(color = Color.Cyan) {
        Text(
            text = "Hi, my name is $name",
            modifier = modifier.padding(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MeasuralyzeTheme {
        Greeting("Samuel")
    }
}