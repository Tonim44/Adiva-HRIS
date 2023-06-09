package id.co.adiva.attendance.home.kehadiran

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.co.adiva.attendance.R

class KehadiranAdapter(val kehadiranList: List<Kehadiran>) : RecyclerView.Adapter<KehadiranAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_listkehadiran, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val kehadiran = kehadiranList[position]
        holder.textViewName.text = kehadiran.employee_name
        holder.textViewDate.text = kehadiran.date

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, KehadiranActivity::class.java)
            intent.putExtra(KehadiranActivity.EXTRA_KEHADIRAN, kehadiran)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = kehadiranList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName = itemView.findViewById<TextView>(R.id.nama)
        val textViewDate = itemView.findViewById<TextView>(R.id.tanggal)

    }

}
