package id.co.adiva.attendance

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import id.co.adiva.attendance.uitel.LoadingDialog

class NoConnectedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noconnect)

        val refreshButton: CardView = findViewById(R.id.muat_ulang)
        refreshButton.setOnClickListener {
            val loading = LoadingDialog(this)
            loading.startLoading()
            startActivity(Intent(this, DashboardActivity::class.java))
            loading.isDismiss()
        }
    }
}

