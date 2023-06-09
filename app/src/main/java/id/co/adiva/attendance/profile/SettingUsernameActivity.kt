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
import id.co.adiva.attendance.databinding.ActivitySetusernameBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.helper.HPI
import id.co.adiva.attendance.helper.PrefHelper
import id.co.adiva.attendance.uitel.LoadingDialog
import org.json.JSONException

class SettingUsernameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetusernameBinding
    private lateinit var data: DataManager
    lateinit var prefHelper: PrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivitySetusernameBinding.inflate(layoutInflater)
        data = DataManager(baseContext)
        val view = binding.root
        prefHelper = PrefHelper(this)
        setContentView(view)

        val loading = LoadingDialog(this)
        loading.startLoading()

        val user = data.getString("user")
        binding.insertUsername.setText(user)

        binding.back.setOnClickListener(View.OnClickListener { onBackPressed() })

        val txtUsername = binding.insertUsername

        binding.save.setOnClickListener{

            val username : String = txtUsername.text.toString()
            changeUsername(username)
        }

        loading.isDismiss()

    }

    private fun changeUsername(username: String) {

        var isValid = true

        if (username.isEmpty()) {
            isValid = false
            binding.insertUsername.error = "NIM wajib diisi"
        }

        if (isValid) {
            val loading = LoadingDialog(this)
            loading.startLoading()
            val token = data.getString("token")
            val queue = Volley.newRequestQueue(this)
            val url = "${HPI.API_URL}setting/set-profile"
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
                    Toast.makeText(this@SettingUsernameActivity, "Gagal diubah", Toast.LENGTH_LONG).show()
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
                    params["username"] = username
                    Log.i("DATA_API", username)
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