package com.example.googleloginsample

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()

        google_btn.setOnClickListener {
            googleLogin()
        }


    }



    private fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null) {
                Log.d("onActivityResult", result.status.toString())
            }

            if (result != null) {
                if (result.isSuccess) {
                    val account = result.signInAccount
                    firebaseAuthWithGoogle(account!!)
                } else {

                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        moveMain(user)
                    }
                    else {
                        val loginfailalert = AlertDialog.Builder(this)
                        loginfailalert.setMessage("잠시 후 다시 시도해 주세요.")
                        loginfailalert.setPositiveButton("확인", null)
                        loginfailalert.show()
                        moveMain(null)
                    }
                }
    }

    public override fun onStart() {     // 자동 로그인 설정
        super.onStart()
        sendToServer()
//        val currentUser = auth.currentUser
//        moveMain(currentUser)
    }

    private fun moveMain(user: FirebaseUser?) {     //
        sendToServer()
        if (user != null) {
            var intent = Intent(applicationContext, LoggedInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun sendToServer() {
        val retrofit = Retrofit.Builder()
                .baseUrl("http://ec2-13-124-208-47.ap-northeast-2.compute.amazonaws.com:8000")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val googleLoginRequest: googleRequest = retrofit.create(googleRequest::class.java)

        val user = FirebaseAuth.getInstance().currentUser
        Log.d("sendToServer", user.toString())

        var uid : String = ""
        var email : String = ""

        if (user != null) {
            uid = user.uid
            email = user.email.toString()
        }
        else {
            Log.d("SendToServer", "No Current User")
        }

        googleLoginRequest.requestGoogleLogin(uid = uid, email = email).enqueue(object : Callback<googleResponse> {
            override fun onFailure(call: Call<googleResponse>, t: Throwable) {
                Log.e("Login", t.message)
                var dialog = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                dialog.setTitle("ERROR")
                dialog.setMessage("서버와의 통신에 실패하였습니다.")
                dialog.show()
            }

            override fun onResponse(call: Call<googleResponse>, response: Response<googleResponse>) {
                if (response.isSuccessful) {
                    var googleResponse = response.body()
                    val code = googleResponse?.code?.let { it1 -> Integer.parseInt(it1) }
                    if (code == 503) {      // uid, email 둘 중 하나 null
                        Log.d("onResponse", googleResponse?.code)
                        Log.d("onResponse", googleResponse?.msg)
                        Log.d("onResponse", googleResponse?.token)
                    }
                    else if (code == 201) {     // Login 성공
                        Log.d("onResponse", googleResponse?.code)
                        Log.d("onResponse", googleResponse?.msg)
                        Log.d("onResponse", googleResponse?.token)
                    }
                }
                else { }
            }
        })
    }
}