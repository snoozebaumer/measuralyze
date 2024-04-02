package ch.hslu.measuralyze.service

import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import android.util.Log
import ch.hslu.measuralyze.model.CellTowerInfo

class CellTowerService(private val telephonyManager: TelephonyManager) {
    fun fetchCurrentValues(onCellTowerValuesFetched: (ArrayList<CellTowerInfo>) -> Unit) {
        val cellInfoList = getCellInfoList()
        val cellTowerInfoList = extractCellTowerInfo(cellInfoList)
        onCellTowerValuesFetched(cellTowerInfoList)
    }

    private fun getCellInfoList(): List<CellInfo> {
        return try {
            telephonyManager.allCellInfo ?: emptyList()
        } catch (e: SecurityException) {
            Log.e("CellTowerService", "Permission to access cell tower information denied")
            emptyList()
        }
    }

    private fun extractCellTowerInfo(cellInfoList: List<CellInfo>): ArrayList<CellTowerInfo> {
        val cellTowerInfoList: ArrayList<CellTowerInfo> = ArrayList()

        for (cellInfo: CellInfo in cellInfoList) {
            val cellTowerInfo = when (cellInfo) {
                is CellInfoWcdma -> extractCellTowerInfoFromWcdma(cellInfo)
                is CellInfoLte -> extractCellTowerInfoFromLte(cellInfo)
                is CellInfoGsm -> extractCellTowerInfoFromGsm(cellInfo)
                else -> {
                    Log.e("CellTowerService", "Unsupported CellInfo type")
                    continue
                }
            }
            cellTowerInfoList.add(cellTowerInfo)
        }
        return cellTowerInfoList
    }

    private fun extractCellTowerInfoFromWcdma(cellInfo: CellInfoWcdma): CellTowerInfo {
        val cellIdentityWcdma = cellInfo.cellIdentity
        val cellSignalStrengthWcdma = cellInfo.cellSignalStrength

        return CellTowerInfo().apply {
            operator = cellIdentityWcdma.operatorAlphaLong.toString()
            mcc = cellIdentityWcdma.mccString ?: ""
            mnc = cellIdentityWcdma.mncString ?: ""
            lac = if (cellIdentityWcdma.lac == Int.MAX_VALUE) 0 else cellIdentityWcdma.lac
            cid = if (cellIdentityWcdma.cid == Int.MAX_VALUE) 0 else cellIdentityWcdma.cid
            rsrp = cellSignalStrengthWcdma.dbm
            rsrq = cellSignalStrengthWcdma.asuLevel
        }
    }

    private fun extractCellTowerInfoFromLte(cellInfo: CellInfoLte): CellTowerInfo {
        val cellIdentityLte = cellInfo.cellIdentity
        val cellSignalStrengthLte = cellInfo.cellSignalStrength

        return CellTowerInfo().apply {
            operator = cellIdentityLte.operatorAlphaLong.toString()
            mcc = cellIdentityLte.mccString ?: ""
            mnc = cellIdentityLte.mncString ?: ""
            lac = if (cellIdentityLte.tac == Int.MAX_VALUE) 0 else cellIdentityLte.tac
            cid = if (cellIdentityLte.ci == Int.MAX_VALUE) 0 else cellIdentityLte.ci
            rsrp = cellSignalStrengthLte.rsrp
            rsrq = cellSignalStrengthLte.rsrq
        }
    }

    private fun extractCellTowerInfoFromGsm(cellInfo: CellInfoGsm): CellTowerInfo {
        val cellIdentityGsm = cellInfo.cellIdentity
        val cellSignalStrengthGsm = cellInfo.cellSignalStrength

        return CellTowerInfo().apply {
            operator = cellIdentityGsm.operatorAlphaLong.toString()
            mcc = cellIdentityGsm.mccString ?: ""
            mnc = cellIdentityGsm.mncString ?: ""
            lac = if (cellIdentityGsm.lac == Int.MAX_VALUE) 0 else cellIdentityGsm.lac
            cid = if (cellIdentityGsm.cid == Int.MAX_VALUE) 0 else cellIdentityGsm.cid
            rsrp = cellSignalStrengthGsm.dbm
            rsrq = cellSignalStrengthGsm.asuLevel
        }
    }
}
