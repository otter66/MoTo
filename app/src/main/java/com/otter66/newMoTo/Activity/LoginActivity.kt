package com.otter66.newMoTo.Activity

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.otter66.newMoTo.R
import com.otter66.newMoTo.Util.Util.setTextColor


class LoginActivity : AppCompatActivity() {

    lateinit var goToGoogleLoginButton: Button

    private val RC_SIGN_IN = 2468
    private lateinit var auth: FirebaseAuth
    private var googleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        //버튼 이벤트 연결
        goToGoogleLoginButton = findViewById(R.id.goToGoogleLoginButton)
        goToGoogleLoginButton.setOnClickListener(onClickListener)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setTextColor(this@LoginActivity, findViewById<TextView>(R.id.appTitleInLogin), "Mo To")
    }

    var onClickListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            when (v.id) {
                R.id.goToGoogleLoginButton -> googleSignIn()
            }
        }
    }

    private fun googleSignIn() {
        // Configure Google Sign In
        val signInIntent: Intent = googleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(ContentValues.TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(ContentValues.TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    turnActivity(MainActivity::class.java)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this@LoginActivity, "로그인에 실패하였습니다", Toast.LENGTH_SHORT).show()
                    //todo 로그인 실패시 액션 updateUI(null)
                }
            }
    }

    private fun turnActivity(c: Class<*>) {
        val intent = Intent(this@LoginActivity, c)
        startActivity(intent)
        finish()
    }
}