package id.co.adiva.attendance.home.kehadiran.fragment

import android.widget.ImageView
import id.co.adiva.attendance.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomInfoWindow(mapView: MapView?) : InfoWindow(R.layout.layout_tooltip, mapView) {

    override fun onClose() {
    }

    override fun onOpen(item: Any) {
        val marker = item as Marker
        val imageClose = mView.findViewById<ImageView>(R.id.imageClose)
        imageClose.setOnClickListener {
            marker.closeInfoWindow()
        }
    }

}