package ch.hslu.measuralyze.service

import android.os.Looper
import ch.hslu.measuralyze.model.GpsPosition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority

class GpsLocationService(private val fusedLocationProviderClient: FusedLocationProviderClient) {
    fun fetchCurrentLocation(isAirplaneModeEnabled: Boolean, onGpsPositionFetched: (GpsPosition) -> Unit) {
        val priority: Int =
            if (isAirplaneModeEnabled) Priority.PRIORITY_BALANCED_POWER_ACCURACY else Priority.PRIORITY_HIGH_ACCURACY

        val locationRequest =
            LocationRequest.Builder(priority, 100).apply {
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
                    onGpsPositionFetched(position)
                    // Stop receiving updates after position is fetched
                    fusedLocationProviderClient.removeLocationUpdates(this)
                }
            }
        }

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}