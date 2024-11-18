package com.kayodedaniel.gogovmobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.model.Appointment

class AppointmentAdapter(
    private var appointments: List<Appointment>,
    private val listener: OnAppointmentClickListener
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    interface OnAppointmentClickListener {
        fun onUpdateClick(appointment: Appointment)
        fun onCancelClick(appointment: Appointment)
    }

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tvAppointmentName)
        val dateTextView: TextView = itemView.findViewById(R.id.tvAppointmentDate)
        val timeTextView: TextView = itemView.findViewById(R.id.tvAppointmentTime)
        val statusTextView: TextView = itemView.findViewById(R.id.tvAppointmentStatus)
        val updateButton: TextView = itemView.findViewById(R.id.btnUpdateAppointment)
        val cancelButton: TextView = itemView.findViewById(R.id.btnCancelAppointment)

        init {
            updateButton.setOnClickListener {
                listener.onUpdateClick(appointments[adapterPosition])
            }
            cancelButton.setOnClickListener {
                listener.onCancelClick(appointments[adapterPosition])
            }
        }
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

        // Disable update and cancel buttons if the appointment is already canceled
        val isCanceled = appointment.status.equals("Canceled", ignoreCase = true)
        holder.updateButton.isEnabled = !isCanceled
        holder.cancelButton.isEnabled = !isCanceled
    }

    fun removeAppointment(appointment: Appointment) {
        val updatedList = appointments.toMutableList()
        updatedList.remove(appointment)
        updateAppointments(updatedList)
    }

    override fun getItemCount(): Int = appointments.size

    fun updateAppointments(newAppointments: List<Appointment>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }
}