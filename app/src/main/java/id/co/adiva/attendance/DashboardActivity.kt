package id.co.adiva.attendance

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import id.co.adiva.attendance.databinding.ActivityDashboardBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.helper.HPI
import id.co.adiva.attendance.profile.SettingActivity
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class DashboardActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var data: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (isConnectedToNetwork()) {

            binding = ActivityDashboardBinding.inflate(layoutInflater)
            setContentView(binding.root)
            data = DataManager(baseContext)
            setSupportActionBar(binding.appBarMain.toolbar)

            val drawerLayout: DrawerLayout = binding.drawerLayout
            val navView: NavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    // R.id.nav_home, R.id.nav_gallery, R.id.logout
                    R.id.nav_home
                ), drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)

            val token = data.getString("token")
            val queue = Volley.newRequestQueue(this@DashboardActivity)
            val url = "${HPI.API_URL}setting/get-profile"
            val stringRequest: StringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener<String?> { response ->
                    try {
                        val responseJson = JSONObject(response)
                        val user = responseJson.getJSONObject("result")
                        val employee = user.getJSONObject("employee")
                        val fotoProfil = employee.getString("photo_link")
                        val employeeName = employee.getString("employee_name")
                        val departementName = employee.getString("department_name")
                        val userName = employee.getString("username")
                        val PhotoProfil = navView.findViewById<ImageView>(R.id.fotoProfile)
                        val User = navView.findViewById<TextView>(R.id.User)
                        val Departement = navView.findViewById<TextView>(R.id.departement_name)

                        Picasso.get().load(fotoProfil).into(PhotoProfil)
                        User.text = employeeName
                        Departement.text = departementName
                        data.putUsername(userName)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                },
                Response.ErrorListener {
                    Toast.makeText(this@DashboardActivity, "Gagal ditampilkan", Toast.LENGTH_LONG)
                        .show()
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

        else {
            startNoConnectedActivity()
        }
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

    private fun startNoConnectedActivity() {
        val splashTime: Long = 3000
        Handler().postDelayed({
            val intent = Intent(this, NoConnectedActivity::class.java)
            startActivity(intent)
            finish()
        }, splashTime)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val settingsItem = menu.findItem(R.id.action_settings)
        settingsItem.setOnMenuItemClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
            true
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}