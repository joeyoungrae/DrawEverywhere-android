package com.draw.free

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class AgreementActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agreement)

        val title : TextView = findViewById(R.id.title)
        val content : TextView = findViewById(R.id.content)

        findViewById<ImageView>(R.id.pop_button).setOnClickListener {
            finish()
        }
    }
}