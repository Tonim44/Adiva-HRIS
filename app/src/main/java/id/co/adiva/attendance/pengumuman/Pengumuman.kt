package id.co.adiva.attendance.pengumuman

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pengumuman(val id: Int,
                val tittle: String,
                val linkfile : String,
                val date_formatted : String) : Parcelable
