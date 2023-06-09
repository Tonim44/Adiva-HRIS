package id.co.adiva.attendance.profile

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.co.adiva.attendance.DashboardActivity
import id.co.adiva.attendance.databinding.ActivitySetsandiBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.helper.HPI
import id.co.adiva.attendance.helper.PrefHelper
import id.co.adiva.attendance.uitel.LoadingDialog
import org.json.JSONException

class SettingSandiActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetsandiBinding
    private lateinit var data: DataManager
    lateinit var prefHelper: PrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivitySetsandiBinding.inflate(layoutInflater)
        data = DataManager(baseContext)
        val view = binding.root
        prefHelper = PrefHelper(this)
        setContentView(view)

        val loading = LoadingDialog(this)
        loading.startLoading()

        binding.back.setOnClickListener(View.OnClickListener { onBackPressed() })

        val txtSandilama = binding.insertSandilama
        val txtSandiBaru = binding.insertSandibaru
        val txtKonfirmasi = binding.insertKonfirmasi

        binding.save.setOnClickListener{

            val sandiLama : String = txtSandilama.text.toString()
            val sandiBaru : String = txtSandiBaru.text.toString()
            val Konfirmasi : String = txtKonfirmasi.text.toString()
            changePassword(sandiLama, sandiBaru, Konfirmasi)
        }

        loading.isDismiss()

    }

    private fun changePassword(sandiLama: String, sandiBaru : String, Konfirmasi : String ) {

        var isValid = true

        if (sandiLama.isEmpty()) {
            isValid = false
            binding.insertSandilama.error = "Sandi lama wajib diisi"
        }

        if (sandiBaru.isEmpty()) {
            isValid = false
            binding.insertSandibaru.error = "Sandi baru wajib diisi"
        }

        if (Konfirmasi.isEmpty()) {
            isValid = false
            binding.insertKonfirmasi.error = "Wajib masukan konfirmasi sandi baru"
        }

        if (isValid) {

            val loading = LoadingDialog(this)
            loading.startLoading()

            val token = data.getString("token")
            val queue = Volley.newRequestQueue(this)
            val url = "${HPI.API_URL}setting/change-password"
            binding.save.isEnabled = false
            val stringRequest: StringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener<String?> { response ->
                    try {

                        binding.save.isEnabled = true
                        moveIntent()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                loading.isDismiss()
                },
                Response.ErrorListener {
                    binding.save.isEnabled = true
                    Toast.makeText(this@SettingSandiActivity, "Pengubahan sandi tidak sesuai", Toast.LENGTH_LONG).show()
                loading.isDismiss()
                }) {
                override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                    val mStatusCode = response.statusCode
                    Log.i("DATA_API", Integer.toString(mStatusCode))
                    return super.parseNetworkResponse(response)
                }

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Accept"] = "application/json"
                    return params
                }

                override fun getParams(): Map<String, String>? {
                    val params: MutableMap<String, String> = HashMap()
                    params["token"] = token.toString()
                    Log.i("DATA_API", token.toString())
                    params["old_password"] = sandiLama
                    Log.i("DATA_API", sandiLama)
                    params["new_password"] = sandiBaru
                    Log.i("DATA_API", sandiBaru)
                    params["confirm_password"] = Konfirmasi
                    Log.i("DATA_API", Konfirmasi)
                    return params
                }
            }

            stringRequest.retryPolicy = object : RetryPolicy {
                override fun getCurrentTimeout(): Int {
                    return 50000
                }

                override fun getCurrentRetryCount(): Int {
                    return 50000
                }

                @Throws(VolleyError::class)
                override fun retry(error: VolleyError) {
                }
            }

            queue.add(stringRequest)
        }
    }

    private fun moveIntent(){
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

}
