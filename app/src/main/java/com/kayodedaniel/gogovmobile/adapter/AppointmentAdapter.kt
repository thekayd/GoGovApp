package com.kayodedaniel.gogovmobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.model.Appointment

class AppointmentAdapter(private val appointments: List<Appointment>) :
    RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tvAppointmentName)
        val dateTextView: TextView = itemView.findViewById(R.id.tvAppointmentDate)
        val timeTextView: TextView = itemView.findViewById(R.id.tvAppointmentTime)
        val statusTextView: TextView = itemView.findViewById(R.id.tvAppointmentStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.appointment_item, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.nameTextView.text = "${appointment.name} ${appointment.surname}"
        holder.dateTextView.text = "Date: ${appointment.appointment_date}"
        holder.timeTextView.text = "Time: ${appointment.appointment_time}"
        holder.statusTextView.text = "Status: ${appointment.status}"
    }

    override fun getItemCount(): Int = appointments.size
}
