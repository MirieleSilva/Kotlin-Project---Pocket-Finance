package com.app.pocketfinance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)


        auth = FirebaseAuth.getInstance()

        val btnFinish = findViewById<android.widget.Button>(R.id.btnFinalizarRegistro)
        val editEmail = findViewById<android.widget.EditText>(R.id.editEmailRegistro)
        val editPassword = findViewById<android.widget.EditText>(R.id.editPasswordRegistro)

        btnFinish.setOnClickListener {
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            android.widget.Toast.makeText(this, "Sucesso!", android.widget.Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            android.widget.Toast.makeText(this, "Erro: ${task.exception?.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                android.widget.Toast.makeText(this, "Preencha todos os campos", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}