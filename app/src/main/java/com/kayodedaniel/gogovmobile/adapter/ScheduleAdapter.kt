package com.kayodedaniel.gogovmobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.model.Schedule

class ScheduleAdapter(
    private var schedules: List<Schedule>,
    private val listener: OnScheduleClickListener
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    // approves, updates and reschedule interfaces
    interface OnScheduleClickListener {
        fun onApproveClick(schedule: Schedule)
        fun onDeclineClick(schedule: Schedule)
        fun onRescheduleClick(schedule: Schedule)
    }

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tvScheduleName)
        val dateTextView: TextView = itemView.findViewById(R.id.tvScheduleDate)
        val timeTextView: TextView = itemView.findViewById(R.id.tvScheduleTime)
        val statusTextView: TextView = itemView.findViewById(R.id.tvScheduleStatus)
        val approveButton: Button = itemView.findViewById(R.id.btnApproveSchedule)
        val declineButton: Button = itemView.findViewById(R.id.btnDeclineSchedule)
        val rescheduleButton: Button = itemView.findViewById(R.id.btnRescheduleSchedule)

        init {
            approveButton.setOnClickListener {
                listener.onApproveClick(schedules[adapterPosition])
            }
            declineButton.setOnClickListener {
                listener.onDeclineClick(schedules[adapterPosition])
            }
            rescheduleButton.setOnClickListener {
                listener.onRescheduleClick(schedules[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
        return ScheduleViewHolder(view)
    }

    // binding view holder to existing items to recycler view
    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.nameTextView.text = "${schedule.name}"
        holder.dateTextView.text = "Date: ${schedule.appointmentDate}"
        holder.timeTextView.text = "Time: ${schedule.appointmentTime}"
        holder.statusTextView.text = "Status: ${schedule.status}"

        // Disable buttons if the schedule is already canceled or approved
        val isFinalized = schedule.status.equals("Canceled", ignoreCase = true) ||
                schedule.status.equals("Approved", ignoreCase = true)
        holder.approveButton.isEnabled = !isFinalized
        holder.declineButton.isEnabled = !isFinalized
        holder.rescheduleButton.isEnabled = !isFinalized
    }

    override fun getItemCount(): Int = schedules.size

    fun updateSchedules(newSchedules: List<Schedule>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }
}
