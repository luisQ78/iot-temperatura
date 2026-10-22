package com.example.iottemp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.iottemp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (auth.currentUser != null) { goMain(); return }
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnLogin.setOnClickListener { submit(register = false) }
        b.btnRegister.setOnClickListener { submit(register = true) }
    }

    private fun submit(register: Boolean) {
        val email = b.etEmail.text.toString().trim()
        val pass = b.etPassword.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { b.tvMsg.text = "Correo no válido"; return }
        if (pass.length < 6) { b.tvMsg.text = "La contraseña debe tener al menos 6 caracteres"; return }

        setLoading(true)
        val task = if (register) auth.createUserWithEmailAndPassword(email, pass)
        else auth.signInWithEmailAndPassword(email, pass)
        task.addOnCompleteListener {
            setLoading(false)
            if (it.isSuccessful) goMain()
            else b.tvMsg.text = "No se pudo autenticar: ${it.exception?.localizedMessage}"
        }
    }

    private fun setLoading(loading: Boolean) {
        b.btnLogin.isEnabled = !loading
        b.btnRegister.isEnabled = !loading
        if (loading) b.tvMsg.text = ""
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
