package com.otter66.newMoTo.Activity

import android.util.Log
import android.os.Bundle
import android.widget.Toast
import com.otter66.newMoTo.R
import android.content.Intent
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.otter66.newMoTo.Fragment.MyPageFragment
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.otter66.newMoTo.Fragment.NoticeBoardFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var db: FirebaseFirestore? = null
    private var currentUserRef: DocumentReference? = null

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //로그인
        auth = Firebase.auth
        user = Firebase.auth.currentUser
        db = Firebase.firestore
        if (user != null) currentUserRef = db?.collection("users")?.document(user!!.uid)
        if(loginCheck()) {
            firstLoginCheck()
        }

        //버튼 이벤트 연결
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener(onItemSelectedListener)

        // bottom navigation menu 아이콘을 커스텀하기 위해 기존 tint null
        bottomNavigationView.itemIconTintList = null

        supportFragmentManager.beginTransaction() .replace(R.id.contentLayout, NoticeBoardFragment()) .commit()
    }

    private var onItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener
    = BottomNavigationView.OnNavigationItemSelectedListener {
        when (it.itemId) {
            R.id.homePageItem -> {
                supportFragmentManager.beginTransaction() .replace(R.id.contentLayout, NoticeBoardFragment()) .commit()
                true
            }
            R.id.myPageItem -> {
                supportFragmentManager.beginTransaction() .replace(R.id.contentLayout, MyPageFragment()) .commit()
                true
            }
            else -> {
                false
            }
        }
    }

    private fun loginCheck(): Boolean {
        if (auth == null || user?.uid == null) {
            turnActivity(LoginActivity::class.java)
            return false
        }
        return true
    }

    private fun firstLoginCheck() {
        var documentReference: DocumentReference? = null

        if(user != null) documentReference = db?.collection("users")?.document(user!!.uid)
        documentReference?.get()
            ?.addOnSuccessListener { document ->
                if (document.data?.get("id") != null) {
                    Log.d("firstLogin", "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d("firstLogin", "No such document")
                    turnActivity(FirstLoginActivity::class.java)
                }
            }
            ?.addOnFailureListener { exception ->
                Log.d("firstLogin", "get failed with ", exception)
            }
    }

    private fun turnActivity(c: Class<*>) {
        val intent = Intent(this@MainActivity, c)
        startActivity(intent)
        finish()
    }

}