package id.co.adiva.attendance.uitel

import android.app.Activity
import android.app.AlertDialog
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import id.co.adiva.attendance.R

class LoadingDialog(val mActivity: Activity) {

    private lateinit var isdialog: AlertDialog

    fun startLoading() {
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_item, null)

        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isdialog = builder.create()
        isdialog.window?.setBackgroundDrawableResource(android.R.color.background_light)

        isdialog.show()

        // Mengubah ukuran dialog dan posisinya di tengah
        val window = isdialog.window
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    fun isDismiss() {
        isdialog.dismiss()
    }
}