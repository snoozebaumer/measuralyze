package ch.hslu.measuralyze.service

import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import android.util.Log
import ch.hslu.measuralyze.model.CellTowerInfo
import java.security.InvalidParameterException

class CellTowerService private constructor(private val telephonyManager: TelephonyManager) {
    companion object {
        private var cellTowerService: CellTowerService? = null

        @Throws(InvalidParameterException::class)
        fun getCellTowerService(telephonyManager: TelephonyManager? = null): CellTowerService {
            if (cellTowerService === null) {
                if (telephonyManager === null) {
                    throw InvalidParameterException("cellTowerService doesn't exist and telephonyManager was not provided")
                }
                cellTowerService = CellTowerService(telephonyManager)
            }
            return cellTowerService as CellTowerService
        }
    }


    fun fetchCurrentValues(onCellTowerValuesFetched: (ArrayList<CellTowerInfo>) -> Unit) {
        val cellInfoList: List<CellInfo>
        try {
            cellInfoList = telephonyManager.allCellInfo
        } catch (e: SecurityException) {
            Log.e("CellTowerService", "Permission to access cell tower information denied")
            onCellTowerValuesFetched(ArrayList())
            return
        }
        val cellTowerInfoList: ArrayList<CellTowerInfo> = ArrayList()

        for (cellInfo: CellInfo in cellInfoList) {
            val cellIdentity = cellInfo.cellIdentity
            val cellTowerInfo = CellTowerInfo()
            cellTowerInfo.operator = cellIdentity.operatorAlphaLong.toString()


            when (cellInfo) {
                is CellInfoWcdma -> {
                    val cellIdentityWcdma = cellInfo.cellIdentity
                    cellTowerInfo.mcc = cellIdentityWcdma.mccString ?: ""
                    cellTowerInfo.mnc = cellIdentityWcdma.mncString ?: ""
                    cellTowerInfo.lac =
                        if (cellIdentityWcdma.lac == Int.MAX_VALUE) 0 else cellIdentityWcdma.lac
                    cellTowerInfo.cid =
                        if (cellIdentityWcdma.cid == Int.MAX_VALUE) 0 else cellIdentityWcdma.cid
                    val cellSignalStrengthWcdma = cellInfo.cellSignalStrength
                    cellTowerInfo.rsrp = cellSignalStrengthWcdma.dbm
                    cellTowerInfo.rsrq = cellSignalStrengthWcdma.asuLevel
                }

                is CellInfoLte -> {
                    val cellIdentityLte = cellInfo.cellIdentity
                    cellTowerInfo.mcc = cellIdentityLte.mccString ?: ""
                    cellTowerInfo.mnc = cellIdentityLte.mncString ?: ""
                    cellTowerInfo.lac =
                        if (cellIdentityLte.tac == Int.MAX_VALUE) 0 else cellIdentityLte.tac
                    cellTowerInfo.cid =
                        if (cellIdentityLte.ci == Int.MAX_VALUE) 0 else cellIdentityLte.ci
                    val cellSignalStrengthLte = cellInfo.cellSignalStrength
                    cellTowerInfo.rsrp = cellSignalStrengthLte.rsrp
                    cellTowerInfo.rsrq = cellSignalStrengthLte.rsrq
                }

                is CellInfoGsm -> {
                    val cellIdentityGsm = cellInfo.cellIdentity
                    cellTowerInfo.mcc = cellIdentityGsm.mccString ?: ""
                    cellTowerInfo.mnc = cellIdentityGsm.mncString ?: ""
                    cellTowerInfo.lac =
                        if (cellIdentityGsm.lac == Int.MAX_VALUE) 0 else cellIdentityGsm.lac
                    cellTowerInfo.cid =
                        if (cellIdentityGsm.cid == Int.MAX_VALUE) 0 else cellIdentityGsm.cid
                    val cellSignalStrengthGsm = cellInfo.cellSignalStrength
                    cellTowerInfo.rsrp = cellSignalStrengthGsm.dbm
                    cellTowerInfo.rsrq = cellSignalStrengthGsm.asuLevel
                }

                else -> {
                    Log.e("CellTowerService", "Unsupported CellInfo type")
                    continue
                }
            }
            cellTowerInfoList.add(cellTowerInfo)
        }
        onCellTowerValuesFetched(cellTowerInfoList)
    }
}
