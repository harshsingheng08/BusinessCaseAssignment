package com.demo.businesscase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.demo.businesscase.databinding.AcivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    lateinit var bind: AcivityLoginBinding
    private lateinit var db: DatabaseReference
    var email: String = ""
    var password: String = ""
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = AcivityLoginBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.btnLogin.setOnClickListener {
            db = FirebaseDatabase.getInstance().getReference("Users")
            if (isValidate()) {
                email = bind.etEmail.text.toString()
                password = bind.etPassword.text.toString()
                fetchData(email, password)
            }
        }
        bind.llRegister.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    private fun isValidate(): Boolean {
        var isValid = true
        if (bind.etEmail.text.isNullOrEmpty()) {
            Toast.makeText(this, "Enter email address", Toast.LENGTH_SHORT).show()
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(bind.etEmail.text.toString())
                .matches()
        ) {
            Toast.makeText(this, "Enter valid email address", Toast.LENGTH_SHORT).show()
            isValid = false
        } else if (bind.etPassword.text.isNullOrEmpty()) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }

    private fun fetchData(email: String, password: String) {
        bind.btnLogin.visibility=View.INVISIBLE
        bind.progressCircular.visibility=View.VISIBLE
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            bind.btnLogin.visibility=View.VISIBLE
            bind.progressCircular.visibility=View.INVISIBLE
            if (task.isSuccessful) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                val ad = AlertDialog.Builder(this)
                ad.setTitle("Message")
                ad.setMessage("Failed to login")
                ad.setPositiveButton("Ok", null)
                ad.show()
            }
        }
    }
}