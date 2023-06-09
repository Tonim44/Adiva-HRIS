package id.co.adiva.attendance.profile

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import id.co.adiva.attendance.databinding.ActivitySettingprofileBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.helper.PrefHelper
import id.co.adiva.attendance.uitel.LoadingDialog

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingprofileBinding
    private lateinit var data: DataManager
    lateinit var prefHelper: PrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivitySettingprofileBinding.inflate(layoutInflater)
        data = DataManager(baseContext)
        val view = binding.root
        prefHelper = PrefHelper(this)
        setContentView(view)

        val loading = LoadingDialog(this)
        loading.startLoading()

        binding.back.setOnClickListener(View.OnClickListener { onBackPressed() })

        binding.settingUsername.setOnClickListener{
            startActivity(Intent(this, SettingUsernameActivity::class.java))
        }

        binding.settingSandi.setOnClickListener{
            startActivity(Intent(this, SettingSandiActivity::class.java))
        }

        loading.isDismiss()
    }
}
