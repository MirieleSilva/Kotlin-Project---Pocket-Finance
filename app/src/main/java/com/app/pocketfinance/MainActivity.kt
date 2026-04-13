package com.app.pocketfinance

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        analytics = FirebaseAnalytics.getInstance(this)

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "Main Activity")
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        val fragEmail = supportFragmentManager.findFragmentById(R.id.containerEmail) as EmailInputFragment
        val fragPassword = supportFragmentManager.findFragmentById(R.id.containerPassword) as PasswordInputFragment
        val fragButton = supportFragmentManager.findFragmentById(R.id.containerButton) as ActionButtonFragment

        fragButton.setButtonText(getString(R.string.btn_login))

        fragButton.setOnClickListener {
            val email = fragEmail.getEmail()
            val password = fragPassword.getPassword()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, getString(R.string.msg_login_success), Toast.LENGTH_SHORT).show()

                            val pref = getSharedPreferences("config", MODE_PRIVATE)
                            pref.edit().putString("email", email).apply()

                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "${getString(R.string.msg_error)}: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, getString(R.string.msg_fill_fields), Toast.LENGTH_SHORT).show()
            }
        }

        val txtForgotPass = findViewById<TextView>(R.id.txtForgotPass)
        txtForgotPass.setOnClickListener {
            val email = fragEmail.getEmail()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, getString(R.string.msg_recovery_sent), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.msg_enter_email), Toast.LENGTH_SHORT).show()
            }
        }

        val txtGoToRegister = findViewById<TextView>(R.id.txtGoToRegister)
        txtGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}