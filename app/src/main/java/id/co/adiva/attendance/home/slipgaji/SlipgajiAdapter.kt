package id.co.adiva.attendance.home.slipgaji

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.co.adiva.attendance.R

class SlipgajiAdapter(val slipgajiList: List<SlipGaji>) : RecyclerView.Adapter<SlipgajiAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_listslipgaji, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val payroll = slipgajiList[position]
        holder.textViewName.text = payroll.employee_name
        holder.textViewNominal.text = payroll.nominal

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, SlipGajiActivity::class.java)
            intent.putExtra(SlipGajiActivity.EXTRA_SLIPGAJI, payroll)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = slipgajiList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName = itemView.findViewById<TextView>(R.id.nama)
        val textViewNominal = itemView.findViewById<TextView>(R.id.nominal)

    }

}