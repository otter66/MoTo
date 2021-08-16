package com.otter66.newMoTo.Activity

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.otter66.newMoTo.R
import com.otter66.newMoTo.Data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirstLoginActivity : AppCompatActivity() {
    private lateinit var userWantIdEditText: EditText
    private lateinit var doubleCheckButton: TextView
    private lateinit var goToUseAppButton: Button
    private var idCheck = 2
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_login)

        userWantIdEditText = findViewById(R.id.userWantIdEditText)
        doubleCheckButton = findViewById(R.id.doubleCheckButton)
        goToUseAppButton = findViewById(R.id.goToUseAppButton)

        doubleCheckButton.setOnClickListener(onClickListener)
        goToUseAppButton.setOnClickListener(onClickListener)
    }

    var onClickListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            when (v.id) {
                R.id.doubleCheckButton -> doubleCheck()
                R.id.goToUseAppButton -> canUseApp()
            }
        }

        private fun doubleCheck() {
            Log.d("doubleCheck", "in doubleCheck loot")

            if (userWantIdEditText.text == null || userWantIdEditText.text.toString() == "") {
                Log.d("doubleCheck", "userWantIdEditText.text == null")
                Toast.makeText(this@FirstLoginActivity, "사용 할 아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                idCheck = 0
            } else {
                Log.d("doubleCheck", "userWantIdEditText.text != null")
                db.collection("users")
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            if (document.data["id"].toString() == userWantIdEditText.text.toString()) {
                                Log.d("doubleCheck", "already using id")
                                Toast.makeText(this@FirstLoginActivity, "이미 사용중인 아이디입니다", Toast.LENGTH_SHORT).show()
                                idCheck = 0
                                break
                            } else {
                                idCheck = 1
                            }
                        }
                        if(idCheck == 1) {
                            Log.d("doubleCheck", "can using id")
                            userWantIdEditText.isEnabled = false
                            Toast.makeText(this@FirstLoginActivity, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        idCheck = 0
//                        Log.w(TAG, "Error getting documents: ", exception)
                    }
            }
        }

        private fun canUseApp() {
            if(idCheck == 2) {
                Toast.makeText(this@FirstLoginActivity, "중복체크를 완료해주세요", Toast.LENGTH_SHORT).show()
            } else if(idCheck == 0) {
                Toast.makeText(this@FirstLoginActivity, "사용할 수 없는 아이디입니다", Toast.LENGTH_SHORT).show()
            } else {
                storeUpload()
            }
        }

        private fun storeUpload() {

            val user = Firebase.auth.currentUser
            val userInfo = User(userWantIdEditText.text.toString())

            db.collection("users").document(user!!.uid).set(userInfo)
                .addOnSuccessListener {
                    startActivity(MainActivity::class.java)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                    Toast.makeText(
                        this@FirstLoginActivity,
                        "회원 정보 등록 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun startActivity(c: Class<*>) {
        val intent = Intent(this@FirstLoginActivity, c)
        startActivity(intent)
        finish()
    }
}