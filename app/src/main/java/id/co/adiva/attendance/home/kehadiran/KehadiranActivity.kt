package id.co.adiva.attendance.home.kehadiran

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import id.co.adiva.attendance.R
import id.co.adiva.attendance.databinding.ActivityKehadiranBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.helper.PrefHelper
import id.co.adiva.attendance.home.kehadiran.fragment.CheckInFragment
import id.co.adiva.attendance.home.kehadiran.fragment.CheckOutFragment
import id.co.adiva.attendance.uitel.LoadingDialog

class KehadiranActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKehadiranBinding
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var data: DataManager
    lateinit var prefHelper: PrefHelper
    private lateinit var kehadiran: Kehadiran

    companion object {
        const val EXTRA_KEHADIRAN = "extra_kehadiran"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = DataBindingUtil.setContentView(this, R.layout.activity_kehadiran)
        data = DataManager(baseContext)
        prefHelper = PrefHelper(this)

        binding.activityKehadiran.setVisibility(View.GONE)
        binding.nowork.setVisibility(View.GONE)

        val loading = LoadingDialog(this)
        loading.startLoading()

        kehadiran = intent.getParcelableExtra<Kehadiran>(EXTRA_KEHADIRAN) as Kehadiran
        binding.back.setOnClickListener(View.OnClickListener { onBackPressed() })

        val id : String = kehadiran.id.toString()
        data.putId(id)

        val CheckIn : String = kehadiran.clock_in
        val CheckOut : String = kehadiran.clock_out
        binding.user.text = kehadiran.employee_name
        binding.date.text = kehadiran.date

        val PhotoProfil = binding.fotoProfile
        val FotoProfile = data.getString("photo_profile")
        Picasso.get().load(FotoProfile).into(PhotoProfil)

        if (CheckIn.equals("-") && CheckOut.equals("-")) {
            binding.tabLayout.setVisibility(View.GONE)
            binding.viewPager.setVisibility(View.GONE)
            binding.nowork.setVisibility(View.VISIBLE)
        }else {
            initView()
        }

        loading.isDismiss()
        binding.activityKehadiran.setVisibility(View.VISIBLE)
    }

    private fun initView() {
        setupViewPager(binding.viewPager,)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.mFragmentTitleList[position]
        }.attach()

        for (i in 0 until binding.tabLayout.tabCount) {
            val tv = LayoutInflater.from(this)
                .inflate(R.layout.custom_tab, null) as TextView

            binding.tabLayout.getTabAt(i)?.customView = tv
        }
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        adapter.addFragment(CheckInFragment(), "Check In")
        adapter.addFragment(CheckOutFragment(), "Check Out")

        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
    }

    inner class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        private val mFragmentList = ArrayList<Fragment>()
        val mFragmentTitleList = ArrayList<String>()

        override fun createFragment(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getItemCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }
    }
}
