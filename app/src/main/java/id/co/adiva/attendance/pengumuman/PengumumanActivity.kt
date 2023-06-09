package id.co.adiva.attendance.pengumuman

import android.app.DownloadManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import id.co.adiva.attendance.databinding.ActivityPengumumanBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.uitel.LoadingDialog

class PengumumanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPengumumanBinding
    private lateinit var pengumuman: Pengumuman
    private lateinit var data : DataManager
    private lateinit var dokumen: CardView

    companion object {
        const val EXTRA_PENGUMUMAN = "extra_pengumuman"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityPengumumanBinding.inflate(layoutInflater)
        data = DataManager(baseContext)
        val view = binding.root
        setContentView(view)

        val loading = LoadingDialog(this)
        loading.startLoading()

        dokumen = binding.bukaFile

        pengumuman = intent.getParcelableExtra<Pengumuman>(PengumumanActivity.EXTRA_PENGUMUMAN) as Pengumuman

        binding.tittle.text = pengumuman.tittle
        binding.tanggal.text = pengumuman.date_formatted
        val id = pengumuman.id
        val link_file = pengumuman.linkfile

        if (link_file.equals("null")) {
            dokumen.visibility = View.GONE
        }

       dokumen.setOnClickListener{
           val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
           val uri = Uri.parse(pengumuman.linkfile)
           val request = DownloadManager.Request(uri)

           // Konfigurasi permintaan unduhan
           request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
           request.setTitle("Unduh Dokumen")
           request.setDescription("Mengunduh dokumen...")

           // Memulai unduhan
           downloadManager.enqueue(request)
       }
        binding.back.setOnClickListener(View.OnClickListener { onBackPressed() })

        loading.isDismiss()
    }
}