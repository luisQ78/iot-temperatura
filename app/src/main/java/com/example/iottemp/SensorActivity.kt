package com.example.iottemp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.iottemp.databinding.ActivitySensorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Dispositivo A: simula un sensor y publica una lectura cada 10 segundos. */
class SensorActivity : AppCompatActivity() {

    companion object { const val INTERVAL_MS = 10_000L }

    private lateinit var b: ActivitySensorBinding
    private var job: Job? = null
    private var temp = 24.0
    private var sent = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySensorBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnToggle.setOnClickListener { if (job?.isActive == true) stop() else start() }
    }

    private fun start() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run { finish(); return }
        val ref = FirebaseDatabase.getInstance().getReference("usuarios/$uid/lecturas")

        b.btnToggle.text = "Detener envío"
        b.tvInfo.text = "Enviando cada ${INTERVAL_MS / 1000} s"
        b.tvError.text = ""

        job = lifecycleScope.launch {
            while (isActive) {
                // Caminata aleatoria: simula variaciones realistas de temperatura
                temp = (temp + Random.nextDouble(-0.8, 0.8)).coerceIn(15.0, 35.0)
                val value = Math.round(temp * 10) / 10.0

                ref.push().setValue(mapOf("temp" to value, "ts" to ServerValue.TIMESTAMP))
                    .addOnSuccessListener {
                        sent++
                        b.tvTemp.text = fmt(value)
                        b.tvCount.text = "Lecturas enviadas: $sent"
                    }
                    .addOnFailureListener { b.tvError.text = "Error al enviar: ${it.localizedMessage}" }

                delay(INTERVAL_MS)
            }
        }
    }

    private fun stop() {
        job?.cancel()
        job = null
        b.btnToggle.text = "Iniciar envío"
        b.tvInfo.text = "Detenido"
    }

    // La pantalla se mantiene encendida (keepScreenOn); si la app pasa a segundo plano, se detiene.
    override fun onStop() {
        stop()
        super.onStop()
    }
}
