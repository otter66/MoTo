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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.ktx.firestore
import com.otter66.newMoTo.Fragment.MyPageFragment
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.otter66.newMoTo.Fragment.NoticeBoardFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.firestore.ktx.toObject
import com.otter66.newMoTo.Data.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var db: FirebaseFirestore? = null
    private var currentUserRef: DocumentReference? = null
    private var currentUserInfo: User? = null

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        * 로그인 정보를 확인하여 로그인되어있다면 현재의 Activity를 그대로 이용합니다.
        * 첫 이용이라면 구글 로그인 -> 회원가입Activity 를 실행합니다. */
        lifecycleScope.launch {
            //로그인
            auth = Firebase.auth
            user = Firebase.auth.currentUser
            db = Firebase.firestore
            if (user != null) currentUserRef = db?.collection("users")?.document(user!!.uid)
            if(loginCheck()) {
                if(!firstLoginCheck()) {
                    db?.collection("users")?.document(user!!.uid)?.get()?.addOnSuccessListener { documentSnapshot ->
                        currentUserInfo = documentSnapshot.toObject<User>()
                    }?.await()

                    bottomNavigationView = findViewById(R.id.bottomNavigationView)
                    bottomNavigationView.setOnItemSelectedListener(onItemSelectedListener)

                    // bottom navigation menu 아이콘을 커스텀하기 위해 기존 tint null
                    bottomNavigationView.itemIconTintList = null
                    supportFragmentManager.beginTransaction() .replace(R.id.contentLayout, NoticeBoardFragment()) .commit()
                }
            }
        }
    }

    private var onItemSelectedListener: NavigationBarView.OnItemSelectedListener
    = NavigationBarView.OnItemSelectedListener {
        when (it.itemId) {
            R.id.homePageItem -> {
                val fragment = NoticeBoardFragment()
                val bundle = Bundle()
                bundle.putSerializable("currentUserInfo", currentUserInfo)
                fragment.arguments = bundle
                supportFragmentManager.beginTransaction() .replace(R.id.contentLayout, fragment) .commit()
                true
            }
            R.id.myPageItem -> {
                val fragment = MyPageFragment()
                val bundle = Bundle()
                bundle.putSerializable("currentUserInfo", currentUserInfo)
                fragment.arguments = bundle
                supportFragmentManager.beginTransaction() .replace(R.id.contentLayout, fragment) .commit()
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

    private fun firstLoginCheck(): Boolean {
        var documentReference: DocumentReference? = null
        var returnValue: Boolean = false

        if(user != null) documentReference = db?.collection("users")?.document(user!!.uid)
        documentReference?.get()
            ?.addOnSuccessListener { document ->
                if (document.data?.get("id") != null) {
                    returnValue = true
                    Log.d("firstLogin", "DocumentSnapshot data: ${document.data}")
                } else {
                    returnValue = false
                    Log.d("firstLogin", "No such document")
                    turnActivity(FirstLoginActivity::class.java)
                }
            }
            ?.addOnFailureListener { exception ->
                returnValue = false
                Log.d("firstLogin", "get failed with ", exception)
            }
        return returnValue
    }

    private fun turnActivity(c: Class<*>) {
        val intent = Intent(this@MainActivity, c)
        startActivity(intent)
        finish()
    }

}