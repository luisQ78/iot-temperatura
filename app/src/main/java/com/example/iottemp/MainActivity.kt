package com.example.iottemp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.iottemp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.tvUser.text = FirebaseAuth.getInstance().currentUser?.email ?: ""
        b.btnSensor.setOnClickListener { startActivity(Intent(this, SensorActivity::class.java)) }
        b.btnMonitor.setOnClickListener { startActivity(Intent(this, MonitorActivity::class.java)) }
        b.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }
}
