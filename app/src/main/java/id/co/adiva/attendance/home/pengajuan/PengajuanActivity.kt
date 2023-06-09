package id.co.adiva.attendance.home.pengajuan

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import id.co.adiva.attendance.databinding.ActivityPengajuanBinding

class PengajuanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPengajuanBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityPengajuanBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.back.setOnClickListener(View.OnClickListener { onBackPressed() })

    }
}
