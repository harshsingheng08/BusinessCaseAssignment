package com.demo.businesscase

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.demo.businesscase.databinding.AcivitySignupBinding
import com.demo.businesscase.model.DataModel
import com.demo.businesscase.model.MapModel
import com.demo.businesscase.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: AcivitySignupBinding
    private lateinit var db: DatabaseReference
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignup.setOnClickListener {
            if (isValidate()) {
                addData()
            }
        }
        binding.llSignin.setOnClickListener {
            finish()
        }

    }

    private fun isValidate(): Boolean {
        var isValid = true
        if (binding.etFullName.text.isNullOrEmpty()) {
            Toast.makeText(this, "Enter Full name", Toast.LENGTH_SHORT).show()
            isValid = false
        } else if (binding.etEmail.text.isNullOrEmpty()) {
            Toast.makeText(this, "Enter email address", Toast.LENGTH_SHORT).show()
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString())
                .matches()
        ) {
            Toast.makeText(this, "Enter valid email address", Toast.LENGTH_SHORT).show()
            isValid = false
        } else if (binding.etPassword.text.isNullOrEmpty()) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }

    private fun addData() {
        binding.btnSignup.visibility = View.INVISIBLE
        binding.progressCircular.visibility = View.VISIBLE
        val name = binding.etFullName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uID = task.result.user?.uid ?: ""
                db = FirebaseDatabase.getInstance().getReference("Users")
                val imageUrl =
                    "https://firebasestorage.googleapis.com/v0/b/businesscase-b84fc.appspot.com/o/642982-200.png?alt=media&token=4d5e4aa1-93f4-42f2-b2f9-13dd3ddba480"
                val user = Users(name, email, password, imageUrl)
                db.child(uID).setValue(user).addOnSuccessListener {

                    val db1 = FirebaseDatabase.getInstance().getReference("Map")
                    val mapModel = MapModel(
                        "Location",
                        "https://firebasestorage.googleapis.com/v0/b/businesscase-b84fc.appspot.com/o/map_pin.png?alt=media&token=4d61f028-2704-4f72-8b3f-483ff3bc39a5",
                        "0.0",
                        "0.0"
                    )
                    db1.child(uID).setValue(mapModel).addOnSuccessListener {
                        val db2 = FirebaseDatabase.getInstance().getReference("Data")
                        val dataModel =
                            DataModel("Information", "wss://echo.websocket.org", "Loading...")
                        db2.child(uID).setValue(dataModel).addOnSuccessListener {
                            binding.btnSignup.visibility = View.VISIBLE
                            binding.progressCircular.visibility = View.INVISIBLE
                            val ad = AlertDialog.Builder(this)
                            ad.setTitle("Message")
                            ad.setMessage("Account registered successfully")
                            ad.setPositiveButton(
                                "Ok"
                            ) { dialog, which ->
                                auth.signOut()
                                dialog.dismiss()
                                finish()
                            }
                            ad.show()
                            binding.etEmail.text.clear()
                            binding.etFullName.text.clear()
                            binding.etPassword.text.clear()
                        }.addOnFailureListener {

                        }
                    }.addOnFailureListener {

                    }

                }.addOnFailureListener {
                    binding.btnSignup.visibility = View.VISIBLE
                    binding.progressCircular.visibility = View.INVISIBLE
                    val ad = AlertDialog.Builder(this)
                    ad.setTitle("Message")
                    ad.setMessage("Account not register")
                    ad.setPositiveButton("Ok", null)
                    ad.show()
                }
            } else {
                binding.btnSignup.visibility = View.VISIBLE
                binding.progressCircular.visibility = View.INVISIBLE
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }


    }
}