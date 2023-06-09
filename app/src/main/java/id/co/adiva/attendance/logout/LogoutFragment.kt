package id.co.adiva.attendance.logout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import id.co.adiva.attendance.LoginActivity
import id.co.adiva.attendance.databinding.FragmentLogoutBinding
import id.co.adiva.attendance.helper.PrefHelper
import id.co.adiva.attendance.uitel.LoadingDialogFragment

class LogoutFragment : Fragment() {

    private var _binding: FragmentLogoutBinding? = null
    private val binding get() = _binding!!
    lateinit var prefHelper: PrefHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLogoutBinding.inflate(inflater, container, false)
        val root: View = binding.root
        prefHelper = PrefHelper(root.context)

        val Yalogout = binding.yaLogout
        Yalogout.setOnClickListener{
            val loading = LoadingDialogFragment(this)
            loading.startLoading()
            (object :Runnable{
                override fun run() {
                    loading.isDismiss()
                }
            })

            prefHelper.clear()
            startActivity(Intent(this.context, LoginActivity::class.java))
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}