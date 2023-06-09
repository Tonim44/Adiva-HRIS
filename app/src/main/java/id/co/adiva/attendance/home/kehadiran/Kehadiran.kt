package id.co.adiva.attendance.home.kehadiran

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Kehadiran(val id: Int,
                     val employee_name: String,
                     val date: String,
                     val clock_in: String,
                     val clock_out: String) : Parcelable
