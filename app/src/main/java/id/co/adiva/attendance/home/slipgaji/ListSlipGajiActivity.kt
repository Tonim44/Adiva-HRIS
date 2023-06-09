package id.co.adiva.attendance.home.slipgaji

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.co.adiva.attendance.databinding.ActivityListslipgajiBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.helper.HPI
import id.co.adiva.attendance.helper.PrefHelper
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class ListSlipGajiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListslipgajiBinding
    private lateinit var data: DataManager
    lateinit var prefHelper: PrefHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: SlipgajiAdapter
    private lateinit var slipgajiList: MutableList<SlipGaji>
    private lateinit var requestQueue: RequestQueue
    private var page = 1
    private var limit = 5
    var isLoading = false

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityListslipgajiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        data = DataManager(baseContext)
        prefHelper = PrefHelper(this)

        binding.back.setOnClickListener(View.OnClickListener { onBackPressed() })

        slipgajiList = mutableListOf()
        adapter = SlipgajiAdapter(slipgajiList)
        recyclerView = binding.slipgaji
        recyclerView.layoutManager = LinearLayoutManager(this@ListSlipGajiActivity)
        recyclerView.adapter = adapter

        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            page = 1
            slipgajiList.clear()
            loadDataFromApi()
        }

        requestQueue = Volley.newRequestQueue(this@ListSlipGajiActivity)


        var isLastPage = false

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItemPosition == adapter.itemCount - 1 && !isLoading && !isLastPage) {
                    isLoading = true
                    page++
                    loadDataFromApi()
                }
            }
        })

        loadDataFromApi()
        isLoading = false

    }

    private fun loadDataFromApi() {

        val token = data.getString("token")
        swipeRefreshLayout.isRefreshing = true
        val url = "${HPI.API_URL}payroll/list"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                try {
                    val responseJson = JSONObject(response)
                    val Result = responseJson.getJSONObject("result")
                    val Payrolls = Result.getJSONArray("payrolls")
                    for (i in 0 until Payrolls.length()) {
                        val payrolls = Payrolls.getJSONObject(i)
                        val listSlipGaji = SlipGaji(
                            payrolls.getInt("id"),
                            payrolls.getString("employee_name"),
                            payrolls.getString("total")
                        )
                       slipgajiList.add(listSlipGaji)
                    }

                    if (page == 1) {
                        adapter = SlipgajiAdapter(slipgajiList)
                        recyclerView.adapter = adapter
                    } else {
                        adapter.notifyDataSetChanged()
                    }

                    swipeRefreshLayout.isRefreshing = false
                    isLoading = false

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this@ListSlipGajiActivity, "Gagal ditampilkan", Toast.LENGTH_LONG).show()
                swipeRefreshLayout.isRefreshing = false
                isLoading = false
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
                params["page"] = page.toString()
                Log.i("DATA_API", page.toString())
                params["limit"] = limit.toString()
                Log.i("DATA_API", limit.toString())
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
            override fun retry(error: VolleyError) {}
        }

        requestQueue.add(stringRequest)
    }
}
