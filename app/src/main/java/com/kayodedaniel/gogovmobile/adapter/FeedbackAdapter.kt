package com.kayodedaniel.gogovmobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.model.UserFeedback

class FeedbackAdapter(private var feedbackList: List<UserFeedback>) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailTextView: TextView = itemView.findViewById(R.id.textViewEmail)
        val phoneTextView: TextView = itemView.findViewById(R.id.textViewPhone)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBarFeedback)
        val feedbackTextView: TextView = itemView.findViewById(R.id.textViewFeedback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feedback_item, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]
        holder.emailTextView.text = feedback.email
        holder.phoneTextView.text = feedback.phone
        holder.ratingBar.rating = feedback.rating
        holder.feedbackTextView.text = feedback.feedbackText
    }

    override fun getItemCount(): Int = feedbackList.size

    fun updateFeedbackList(newFeedbackList: List<UserFeedback>) {
        feedbackList = newFeedbackList
        notifyDataSetChanged()
    }
}
