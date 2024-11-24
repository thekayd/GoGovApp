package com.kayodedaniel.gogovmobile.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.kayodedaniel.gogovmobile.R

class StatusActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    // status activity scrapped as we are using supabase instead
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        firestore = FirebaseFirestore.getInstance()
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//
//        firestore.collection("applications").document(userId!!)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document != null) {
//                    val status = document.getString("status")
//                    // Display status
//                }
//            }
   }
}