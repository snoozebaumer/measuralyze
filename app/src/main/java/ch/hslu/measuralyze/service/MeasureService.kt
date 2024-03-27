package ch.hslu.measuralyze.service

import android.content.Context
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import ch.hslu.measuralyze.model.Measurement
import com.google.android.gms.location.LocationServices
import java.time.LocalDateTime

class MeasureService(context: Context) {
    private val cellTowerService: CellTowerService = CellTowerService(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
    private val systemSettingsService: SystemSettingsService = SystemSettingsService(context.contentResolver, context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
    private val wifiScanService: WifiScanService = WifiScanService(context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
    private val gpsLocationService: GpsLocationService = GpsLocationService(LocationServices.getFusedLocationProviderClient(context))

    fun fetchMeasurement(onMeasurementFetched: (Measurement) -> Unit) {
        systemSettingsService.fetchSystemSettings { systemSettings ->
            gpsLocationService.fetchCurrentLocation(
                systemSettings.isAirplaneModeEnabled
            ) { position ->
                val measurement = Measurement(
                    LocalDateTime.now(),
                    position.latitude,
                    position.longitude,
                    position.accuracy
                )
                measurement.systemSettings = systemSettings

                cellTowerService.fetchCurrentValues { cellTowerInfo ->
                    measurement.cellTowerInfo = cellTowerInfo
                }
                wifiScanService.fetchCurrentValues { wifiInfo ->
                    measurement.wifiInfo = wifiInfo
                    onMeasurementFetched(measurement)
                }
            }
        }
    }
}