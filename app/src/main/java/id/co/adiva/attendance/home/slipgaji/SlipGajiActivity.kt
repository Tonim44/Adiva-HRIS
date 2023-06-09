package id.co.adiva.attendance.home.slipgaji

import android.app.DownloadManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.co.adiva.attendance.databinding.ActivitySlipgajiBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.helper.HPI
import id.co.adiva.attendance.uitel.LoadingDialog
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class SlipGajiActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySlipgajiBinding
    private lateinit var data: DataManager
    var id : String? = null
    private lateinit var payroll: SlipGaji

    companion object {
        const val EXTRA_SLIPGAJI = "extra_slipgaji"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivitySlipgajiBinding.inflate(layoutInflater)
        data = DataManager(baseContext)
        val view = binding.root
        setContentView(view)

        payroll = intent.getParcelableExtra<SlipGaji>(SlipGajiActivity.EXTRA_SLIPGAJI) as SlipGaji
        id = payroll.id.toString()

        binding.back.setOnClickListener(View.OnClickListener { onBackPressed() })

        data = DataManager(baseContext)

        binding.activitySlipgaji.setVisibility(View.GONE)
        val loading = LoadingDialog(this)
        loading.startLoading()

        val token = data.getString("token")
        val queue = Volley.newRequestQueue(this@SlipGajiActivity)
        val url = "${HPI.API_URL}payroll/detail"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                try {
                    val responseJson = JSONObject(response)
                    val Result = responseJson.getJSONObject("result")
                    val payRoll = Result.getJSONObject("payroll")

                    binding.nama.text = payRoll.getString("employee_name")
                    binding.basicSalary.text = payRoll.getString("basic_salary")
                    binding.totalAllowance.text = payRoll.getString( "total_allowance")
                    binding.bonus.text = payRoll.getString("bonus")

                    val pemotongan = payRoll.getJSONArray("cut_list")
                    for (i in 0 until pemotongan.length()) {
                        val payrolls = pemotongan.getJSONObject(i)
                        val cutNominal = payrolls.getString("cut_nominal")
                        val ketPemotongan = payrolls.getString("cut_name")
                        binding.cutNominal.text = cutNominal
                        binding.cutName.text = ketPemotongan
                    }

                    binding.totalText.text = payRoll.getString("total")
                    val notes = payRoll.getString("notes")
                    val slipgaji = payRoll.getString("slip_link")

                    if (notes.equals("null")) {
                        binding.notes.setVisibility(View.GONE)
                    }else{
                        binding.notes.text = notes
                    }

                    binding.downloadSlipgaji.setOnClickListener {
                        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val uri = Uri.parse(slipgaji)
                        val request = DownloadManager.Request(uri)

                        // Konfigurasi permintaan unduhan
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        request.setTitle("Unduh Slip Gaji")
                        request.setDescription("Mengunduh slip gaji...")

                        // Memulai unduhan
                        downloadManager.enqueue(request)
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                loading.isDismiss()
                binding.activitySlipgaji.setVisibility(View.VISIBLE)
            },
            Response.ErrorListener {
                Toast.makeText(this@SlipGajiActivity, "Gagal ditampilkan", Toast.LENGTH_LONG).show()
                loading.isDismiss()
                binding.activitySlipgaji.setVisibility(View.VISIBLE)
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
                params["id_payroll"] = id.toString()
                Log.i("DATA_API", id.toString())
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
