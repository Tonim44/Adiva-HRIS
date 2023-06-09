package id.co.adiva.attendance

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.co.adiva.attendance.databinding.ActivityLoginBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.helper.HPI
import id.co.adiva.attendance.helper.PrefHelper
import id.co.adiva.attendance.uitel.LoadingDialog
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var data : DataManager
    lateinit var prefHelper: PrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityLoginBinding.inflate(layoutInflater)
        data = DataManager(baseContext)
        val view = binding.root
        prefHelper = PrefHelper(this)
        setContentView(view)

        val loading = LoadingDialog(this)
        loading.startLoading()

        val txtUsername = binding.insertUsername
        val txtPassword = binding.insertPassword

        binding.login.setOnClickListener{

            if (isConnectedToNetwork()) {

            val username : String = txtUsername.text.toString()
            val password : String = txtPassword.text.toString()

            loginRequest(username, password)

            } else {
                Toast.makeText(this@LoginActivity, "Koneksi tidak terhubung", Toast.LENGTH_LONG).show()

            }
        }

        loading.isDismiss()

    }

    private fun isConnectedToNetwork(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        var isConnected = false

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        } else {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            isConnected = networkInfo?.isConnected ?: false
        }

        return isConnected
    }

    private fun loginRequest(username: String, password: String) {

        var isValid = true

        if (username.isEmpty()) {
            isValid = false
            binding.insertUsername.error = "NIM wajib diisi"
        }

        if (password.isEmpty()) {
            isValid = false
            binding.insertPassword.error = "Password wajib diisi"
        }

        if (isValid) {

            val loading = LoadingDialog(this)
            loading.startLoading()

            val queue = Volley.newRequestQueue(this)
            val url = "${HPI.API_URL}login"
            binding.login.isEnabled = false
            val stringRequest: StringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener<String?> { response ->
                    try {
                        val responseJson = JSONObject(response)
                        val results = responseJson.getJSONObject("result")
                        val Token = results.getString("token")
                        Log.i("DATA_API", Token)

                        if (username.isNotEmpty() && password.toString().isNotEmpty()) {
                            saveSession(username.toString(), password.toString())
                        }

                        data.putUserData(results)
                        binding.login.isEnabled = true
                        moveIntent()
                        loading.isDismiss()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                },
                Response.ErrorListener {
                    binding.login.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Email atau Password salah", Toast.LENGTH_LONG).show()
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
                    params["username"] = username
                    Log.i("DATA_API", username)
                    params["password"] = password
                    Log.i("DATA_API", password)
                    params["type"] = "student"
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

    override fun onStart() {
        super.onStart()
        if (prefHelper.getBoolean( HPI.PREF_IS_LOGIN )) {
            moveIntent()
        }
    }

    private fun saveSession(username: String, password: String){
        prefHelper.put( HPI.PREF_USERNAME, username )
        prefHelper.put( HPI.PREF_PASSWORD, password )
        prefHelper.put( HPI.PREF_IS_LOGIN, true)
    }

    private fun moveIntent(){
        startActivity(Intent(this, DashboardActivity::class.java))
        val loading = LoadingDialog(this)
        loading.startLoading()
        (object :Runnable{
            override fun run() {
                finish()
                loading.isDismiss()
            }
        })
    }

    override fun onBackPressed() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Keluar")
        alertDialogBuilder
            .setMessage("Apakah anda yakin untuk menutup Aplikasi ?")
            .setCancelable(false)
            .setPositiveButton(
                "Iya"
            ) { dialog, id ->
                moveTaskToBack(true)
                Process.killProcess(Process.myPid())
                System.exit(1)
            }
            .setNegativeButton(
                "Tidak"
            ) { dialog, id -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}