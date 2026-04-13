package com.app.pocketfinance

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = FirebaseAuth.getInstance()

        val fragEmail = supportFragmentManager.findFragmentById(R.id.containerEmailReg) as EmailInputFragment
        val fragPassword = supportFragmentManager.findFragmentById(R.id.containerPasswordReg) as PasswordInputFragment
        val fragButton = supportFragmentManager.findFragmentById(R.id.containerButtonReg) as ActionButtonFragment

        fragButton.setButtonText(getString(R.string.btn_finish_register))

        fragButton.setOnClickListener {
            val email = fragEmail.getEmail()
            val password = fragPassword.getPassword()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, (getString(R.string.login_success)), Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "${getString(R.string.msg_error)}: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, (getString(R.string.msg_fill_all_fields)), Toast.LENGTH_SHORT).show()
            }
        }
    }
}