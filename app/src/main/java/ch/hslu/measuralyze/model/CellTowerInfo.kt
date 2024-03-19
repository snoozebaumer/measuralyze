package ch.hslu.measuralyze.model

/**
 * standardized CellTowerInfo instead of having 3 different types for Gsm, LTE and Wcdma
 * some properties might keep the standard value on some network types, though.
 */
class CellTowerInfo {
    /**
     * The Mobile Country Code of the phone network
     */
    var mcc: String = ""

    /**
     * Mobile Network Code for the phone operator
     */
    var mnc: String = ""

    /**
     * Location Area Code of the cell tower
     */
    var lac: Int = 0

    /**
     * Cell tower id
     */
    var cid: Int = 0

    /**
     * The phone network operator
     */
    var operator: String = ""

    /**
     * Signal strength RSRP (Referenc Signal Received Power) in dBm
     */
    var rsrp: Int = 0

    /**
     * Signal quality RSRQ (Reference Signal Received Quality) in dB
     */
    var rsrq: Int = 0

    override fun toString(): String {
        return "CellTowerInfo(mcc='$mcc', mnc='$mnc', lac=$lac, cid=$cid, operator='$operator', rsrp=$rsrp, rsrq=$rsrq)"
    }
}