package id.co.adiva.attendance.home.kehadiran.fragment

import android.annotation.SuppressLint
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
import id.co.adiva.attendance.databinding.FragmentCekinBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.helper.HPI
import id.co.adiva.attendance.uitel.LoadingDialogFragment
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class CheckInFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    private var _binding: FragmentCekinBinding? = null
    private val binding get() = _binding!!
    private lateinit var data : DataManager

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCekinBinding.inflate(inflater, container, false)
        val root: View = binding.root
        data = DataManager(root.context)

        val id = data.getString("id")
        Log.i("DATA_API", id.toString())

        binding.fragmentCekin.setVisibility(View.GONE)
        val loading = LoadingDialogFragment(this)
        loading.startLoading()

        val token = data.getString("token")
        val queue = Volley.newRequestQueue(this.context)
        val url = "${HPI.API_URL}attendance/detail"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                try {
                    val responseJson = JSONObject(response)
                    val user = responseJson.getJSONObject("result")
                    val Attendances = user.getJSONObject("attendance")
                    val checkIn = Attendances.getString("clock_in")
                    val photoIn = Attendances.getString("clock_in_photo_link")
                    val locationIn = Attendances.getString("clock_in_photo_location")
                    Log.i("DATA_API", user.toString())

                    binding.jamMasuk.text = checkIn
                    Picasso.get().load(photoIn).into(binding.fotoAbsenIn)

                    if (locationIn.equals("false")){
                        binding.sharelokIn.setVisibility(View.GONE)
                    }else{
                        val locationInObject = Attendances.getJSONObject("clock_in_photo_location")
                        val latitude = locationInObject.getString("latitude")
                        val longitude = locationInObject.getString("longitude")
                        Log.i("DATA_API", locationInObject.toString())

                        data.putLatitude(latitude)
                        data.putLongitude(longitude)

                        binding.sharelokIn.setOnClickListener {
                            startActivity(Intent(this.context, MapsHadirActivity::class.java))
                        }
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                loading.isDismiss()
                binding.fragmentCekin.setVisibility(View.VISIBLE)
            },
            Response.ErrorListener {
                Toast.makeText(this.context, "Gagal ditampilkan", Toast.LENGTH_LONG)
                    .show()
                loading.isDismiss()
                binding.fragmentCekin.setVisibility(View.VISIBLE)
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
                params["id_attendance"] = id.toString()
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

        return root
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        fun newInstance(param1: String?, param2: String?): CheckInFragment {
            val fragment = CheckInFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
