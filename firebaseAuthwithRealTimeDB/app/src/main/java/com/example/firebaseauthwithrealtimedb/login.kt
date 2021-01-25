package com.example.firebaseauthwithrealtimedb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class login : AppCompatActivity() {

    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            var intent = Intent(this, profile::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {
            if(TextUtils.isEmpty(usernameInput.text.toString())){
                usernameInput.error = "Please enter username"
                return@setOnClickListener
            }
            else if(TextUtils.isEmpty(passwordInput.text.toString())){
                usernameInput.error = "Please enter password"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(usernameInput.text.toString(), passwordInput.text.toString())
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        startActivity(Intent(this, profile::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Login failed, please try again! ", Toast.LENGTH_LONG).show()
                    }
                }
        }

        registerText.setOnClickListener{
            startActivity(Intent(this, register::class.java))
            finish()
        }


    }

}