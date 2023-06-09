package id.co.adiva.attendance.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import id.co.adiva.attendance.databinding.FragmentHomeBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.helper.HPI
import id.co.adiva.attendance.home.kehadiran.ListKehadiranActivity
import id.co.adiva.attendance.home.pengajuan.PengajuanActivity
import id.co.adiva.attendance.home.slipgaji.ListSlipGajiActivity
import id.co.adiva.attendance.uitel.LoadingDialogFragment
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var data : DataManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        data = DataManager(root.context)

        binding.homeFragment.setVisibility(View.GONE)

        binding.kehadiran.setOnClickListener {
            startActivity(Intent(this.context, ListKehadiranActivity::class.java))
        }

        binding.slipgaji.setOnClickListener {
            startActivity(Intent(this.context, ListSlipGajiActivity::class.java))
        }

        binding.jadwal.setOnClickListener {
            Toast.makeText(this.context, "Masih dalam tahap pengembangan", Toast.LENGTH_LONG).show()
        }

        binding.pengajuan.setOnClickListener {
            startActivity(Intent(this.context, PengajuanActivity::class.java))
        }

        val loading = LoadingDialogFragment(this)
        loading.startLoading()

        val token = data.getString("token")
        val queue = Volley.newRequestQueue(this.context)
        val url = "${HPI.API_URL}get-dashboard"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                try {
                    val responseJson = JSONObject(response)
                    val user = responseJson.getJSONObject("result")
                    val statusJamkerja = user.getInt("attendance_action_code")
                    val jamMasuk = user.getString("shift_clock_start")
                    val jamPulang = user.getString("shift_clock_end")
                    val employee = user.getJSONObject("employee")
                    val fotoProfil = employee.getString("photo_link")
                    val employeeName = employee.getString("employee_name")
                    val departementName = employee.getString("department_name")

                    data.putProfile(fotoProfil)

                    binding.jamKerja.text = "${jamMasuk}-${jamPulang}"
                    binding.user.text = employeeName
                    binding.departement.text = departementName
                    Picasso.get().load(fotoProfil).into(binding.fotoProfile)

                    Log.i("DATA_API", user.toString())

                    if (statusJamkerja.equals(0) || statusJamkerja.equals(2)) {
                        binding.jammasukDisable.setVisibility(View.VISIBLE)
                        binding.jamkeluarDisable.setVisibility(View.VISIBLE)
                        binding.isiJammasuk.setVisibility(View.GONE)
                        binding.isiJamkeluar.setVisibility(View.GONE)
                    } else if (statusJamkerja.equals(1)) {
                        binding.jammasukDisable.setVisibility(View.GONE)
                        binding.jamkeluarDisable.setVisibility(View.VISIBLE)
                        binding.isiJamkeluar.setVisibility(View.GONE)
                        binding.isiJammasuk.setOnClickListener {
                            startActivity(Intent(this.context, CheckInActivity::class.java))
                        }
                    } else if (statusJamkerja.equals(3)){
                        binding.jammasukDisable.setVisibility(View.VISIBLE)
                        binding.jamkeluarDisable.setVisibility(View.GONE)
                        binding.isiJammasuk.setVisibility(View.GONE)
                        binding.isiJamkeluar.setOnClickListener {
                            startActivity(Intent(this.context, CheckOutActivity::class.java))
                        }
                    }else {
                        binding.jammasukDisable.setVisibility(View.GONE)
                        binding.jamkeluarDisable.setVisibility(View.GONE)
                        binding.isiJammasuk.setVisibility(View.GONE)
                        binding.isiJamkeluar.setVisibility(View.GONE)
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                loading.isDismiss()
                binding.homeFragment.setVisibility(View.VISIBLE)
            },
            Response.ErrorListener {
                Toast.makeText(this.context, "Gagal ditampilkan", Toast.LENGTH_LONG).show()
                loading.isDismiss()
                binding.homeFragment.setVisibility(View.VISIBLE)
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

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}