package ch.hslu.measuralyze.model

/**
 * saves the GPS position of the device in WGS84 projection
 */
data class GpsPosition(var latitude: Double = 0.0, var longitude: Double = 0.0, var accuracy: Float = 0.0f) {
    override fun toString(): String {
        return "GpsPosition(latitude=$latitude, longitude=$longitude, accuracy=$accuracy)"
    }
}