package id.co.adiva.attendance.home.slipgaji

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SlipGaji(val id: Int,
                     val employee_name: String,
                     val nominal: String) : Parcelable
