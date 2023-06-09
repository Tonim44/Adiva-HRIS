package id.co.adiva.attendance.profile

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
import id.co.adiva.attendance.databinding.FragmentProfileBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.helper.HPI
import id.co.adiva.attendance.uitel.LoadingDialogFragment
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var data: DataManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        data = DataManager(root.context)

        binding.profileFragment.setVisibility(View.GONE)
        val loading = LoadingDialogFragment(this)
        loading.startLoading()

        val token = data.getString("token")
        val queue = Volley.newRequestQueue(this.context)
        val url = "${HPI.API_URL}setting/get-profile"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                try {
                    val responseJson = JSONObject(response)
                    val user = responseJson.getJSONObject("result")
                    val employee = user.getJSONObject("employee")
                    val id = employee.getInt("id")
                    val fotoProfil = employee.getString("photo_link")
                    val userName = employee.getString("username")
                    val departementName = employee.getString("department_name")
                    val employeeName = employee.getString("employee_name")
                    val employeeNumber = employee.getString("employee_number")
                    val phoneNumber = employee.getString("phone_number")
                    val email = employee.getString("email")
                    val gender = employee.getString("gender")
                    val positionName = employee.getString("position_name")
                    val address = employee.getString("address")

                    Picasso.get().load(fotoProfil).into(binding.fotoProfile)
                    binding.namaLengkap.text = employeeName
                    binding.departement.text = departementName
                    binding.username.text = userName
                    binding.employeeNumber.text = employeeNumber
                    binding.phoneNumber.text = phoneNumber
                    binding.email.text = email
                    binding.positionName.text = positionName
                    binding.address.text = address

                    if (gender.equals("L")) {
                        binding.gender.text = "Laki-laki"
                    }else{
                        binding.gender.text = "Perempuan"
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                loading.isDismiss()
                binding.profileFragment.setVisibility(View.VISIBLE)
            },
            Response.ErrorListener {
                Toast.makeText(this.context, "Gagal ditampilkan", Toast.LENGTH_LONG).show()
                loading.isDismiss()
                binding.profileFragment.setVisibility(View.VISIBLE)
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