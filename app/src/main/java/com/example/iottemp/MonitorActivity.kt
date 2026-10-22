package com.example.iottemp

import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.iottemp.databinding.ActivityMonitorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Dispositivo B: escucha Firebase en tiempo real y muestra datos, gráfico y estadísticas. */
class MonitorActivity : AppCompatActivity() {

    companion object {
        const val MAX_LECTURAS = 50
        const val SIN_SENAL_MS = 30_000L
    }

    private lateinit var b: ActivityMonitorBinding
    private lateinit var base: DatabaseReference
    private var umbral = 30.0
    private var lecturas = listOf<Lectura>()
    private var ticker: Job? = null

    private val lecturasQuery by lazy {
        base.child("lecturas").orderByChild("ts").limitToLast(MAX_LECTURAS)
    }

    private val lecturasListener = object : ValueEventListener {
        override fun onDataChange(s: DataSnapshot) {
            lecturas = s.children.mapNotNull { c ->
                val t = c.child("temp").getValue(Double::class.java)
                val ts = c.child("ts").getValue(Long::class.java)
                if (t != null && ts != null) Lectura(t, ts) else null
            }
            render()
        }
        override fun onCancelled(e: DatabaseError) { b.tvEstado.text = "Error: ${e.message}" }
    }

    private val umbralListener = object : ValueEventListener {
        override fun onDataChange(s: DataSnapshot) {
            s.getValue(Double::class.java)?.let {
                umbral = it
                if (b.etUmbral.text.isNullOrEmpty()) b.etUmbral.setText(it.toString())
            }
            render()
        }
        override fun onCancelled(e: DatabaseError) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMonitorBinding.inflate(layoutInflater)
        setContentView(b.root)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run { finish(); return }
        base = FirebaseDatabase.getInstance().getReference("usuarios/$uid")

        b.btnGuardar.setOnClickListener {
            val v = b.etUmbral.text.toString().replace(',', '.').toDoubleOrNull()
            if (v == null || v < -50 || v > 150) {
                Toast.makeText(this, "Umbral no válido (-50 a 150)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            base.child("config/umbral").setValue(v)
        }
    }

    override fun onStart() {
        super.onStart()
        lecturasQuery.addValueEventListener(lecturasListener)
        base.child("config/umbral").addValueEventListener(umbralListener)
        // Refresca cada 5 s para detectar "sensor sin señal" aunque no lleguen datos nuevos
        ticker = lifecycleScope.launch { while (isActive) { delay(5_000); render() } }
    }

    override fun onStop() {
        lecturasQuery.removeEventListener(lecturasListener)
        base.child("config/umbral").removeEventListener(umbralListener)
        ticker?.cancel()
        super.onStop()
    }

    private fun render() {
        val s = computeStats(lecturas, umbral)
        if (s == null) {
            b.tvTempActual.text = "--"
            b.tvEstado.text = "Esperando datos..."
            b.tvEstado.setTextColor(Color.GRAY)
            b.chart.setData(emptyList(), umbral)
            return
        }
        val ultima = lecturas.last()
        val sinSenal = System.currentTimeMillis() - ultima.ts > SIN_SENAL_MS
        val alerta = s.last > umbral

        b.tvTempActual.text = fmt(s.last)
        when {
            sinSenal -> { b.tvEstado.text = "Sensor sin señal"; b.tvEstado.setTextColor(Color.parseColor("#EF6C00")) }
            alerta -> { b.tvEstado.text = "ALERTA: sobre el umbral (${fmt(umbral)})"; b.tvEstado.setTextColor(Color.RED) }
            else -> { b.tvEstado.text = "Temperatura normal"; b.tvEstado.setTextColor(Color.parseColor("#2E7D32")) }
        }
        b.tvUltima.text = "Última actualización: " + DateFormat.format("HH:mm:ss", ultima.ts)

        b.tvLast.text = fmt(s.last)
        b.tvMin.text = fmt(s.min)
        b.tvMax.text = fmt(s.max)
        b.tvAvg.text = fmt(s.avg)
        b.tvStd.text = fmt(s.std)
        b.tvCount.text = s.count.toString()
        b.tvOver.text = s.over.toString()

        b.chart.setData(lecturas.map { it.temp }, umbral)
    }
}
