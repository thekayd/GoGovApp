package com.kayodedaniel.gogovmobile.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kayodedaniel.gogovmobile.R


class ApplicationActivity : AppCompatActivity() {
    private val PICK_PDF_CODE = 234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application)

        findViewById<Button>(R.id.btnUploadDocument).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, PICK_PDF_CODE)
        }

        findViewById<Button>(R.id.btnSubmitApplication).setOnClickListener {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null) {
            val uri = data.data

        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
