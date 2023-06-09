package id.co.adiva.attendance.home

import android.content.pm.ActivityInfo
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import id.co.adiva.attendance.R
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.home.kehadiran.fragment.CustomInfoWindow
import id.co.adiva.attendance.uitel.LoadingDialog
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException
import java.util.*

class MapsIsiActivity : AppCompatActivity() {

    lateinit var mapController: MapController
    private lateinit var data: DataManager
    lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_maps)

        val loading = LoadingDialog(this)
        loading.startLoading()

        var addresses: List<Address>
        val geocoder = Geocoder(this@MapsIsiActivity, Locale.getDefault())

        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener(View.OnClickListener { onBackPressed() })

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        try {
            data = DataManager(baseContext)
            val latitude: String = data.getString("latitude_now").toString()
            val longitude: String = data.getString("longitude_now").toString()

            Log.i("DATA_API", latitude.toString())
            Log.i("DATA_API", longitude.toString())

            addresses = geocoder.getFromLocation(
                latitude.toDouble(),
                longitude.toDouble(),
                1
            ) as List<Address>

            var address: String = ""
            if (addresses.isNotEmpty()) {
                address = addresses[0].getAddressLine(0)
                Log.i("DATA_API", address)
            }

            val geoPoint = org.osmdroid.util.GeoPoint(
                latitude.toDouble(), longitude.toDouble()
            )

            mapView = findViewById<MapView>(R.id.mapView)
            mapView.setMultiTouchControls(true)
            mapView.controller.setZoom(20.0)
            mapView.controller.setCenter(geoPoint)
            mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

            mapController = mapView.controller as MapController

            val strVicinity: String = address
            val latLoc = latitude.toDouble()
            val longLoc = longitude.toDouble()
            Log.i("DATA_API", strVicinity)
            Log.i("DATA_API", "latloc ${latLoc.toString()}")
            Log.i("DATA_API", "longloc ${longLoc.toString()}")

            val tvAlamat = findViewById<TextView>(R.id.AlamatLokasi)
            tvAlamat.text = strVicinity

            val marker = Marker(mapView)
            marker.icon = resources.getDrawable(R.drawable.map_icon)
            marker.position = org.osmdroid.util.GeoPoint(latLoc, longLoc)
            marker.infoWindow = CustomInfoWindow(mapView)
            marker.setOnMarkerClickListener { item, arg1 ->
                item.showInfoWindow()
                true
            }
            mapView.overlays.add(marker)
            mapView.invalidate()

            loading.isDismiss()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i("DATA_API", "IOException: ${e.message}")
            // Tindakan yang ingin Anda lakukan jika terjadi IOException saat menggunakan geocoder
        }

        loading.isDismiss()
    }

    public override fun onResume() {
        super.onResume()
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        if (this::mapView.isInitialized) {
            mapView.onResume()
        }
    }

    public override fun onPause() {
        super.onPause()
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        if (this::mapView.isInitialized) {
            mapView.onPause()
        }
    }
}
