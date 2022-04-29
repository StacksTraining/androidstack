package com.magda.noteapp.activity


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.magda.noteapp.databinding.ActivityLoginBinding
import com.magda.noteapp.model.User
import com.magda.noteapp.utils.Constants
import com.magda.noteapp.utils.SessionManager

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.apply {
            btSignUp.setOnClickListener { confirmDetails() }
            tvLogIn.setOnClickListener {
                startActivity(
                    Intent(
                        this@LoginActivity,
                        SignUpActivity::class.java
                    )
                )
            }
        }

    }

    private fun confirmDetails() {
        binding.apply {
            val emailAddress = etEmail.text.toString().trim()
            val password = password.editText?.text.toString().trim()
            if (emailAddress.isNotEmpty()) {
                if (isEmailValid(emailAddress)) {
                    if (password.isNotEmpty()) {
                        authentication(emailAddress, password)
                    } else Toast.makeText(
                        this@LoginActivity,
                        "Password can not be empty",
                        Toast.LENGTH_SHORT
                    ).show()
                } else Toast.makeText(
                    this@LoginActivity,
                    "This is not a valid email",
                    Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(
                this@LoginActivity,
                "Email Address can not be empty",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun authentication(emailAddress: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(emailAddress, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                getUser(auth)
            } else Toast.makeText(
                this@LoginActivity,
                "Error occurred while logging in. Try again.",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener { e ->
            Toast.makeText(
                this@LoginActivity,
                e.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getUser(auth: FirebaseAuth) {
        val db = FirebaseFirestore.getInstance()
        val userReference = db.collection(Constants.USERS)
        val userId = auth.currentUser!!.uid
        userReference.whereEqualTo(Constants.UUID, userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let {
                    //querysnapshot->toObjects()
                    //documentsnapshot -> toObject()
                    it.documents.map { snapshot ->
                        val user = snapshot.toObject(User::class.java)
                        val sessionManager = SessionManager(this@LoginActivity)
                        sessionManager.storeInfo(user!!)
                        goToTheNextPage(user)
                    }
                }
            } else Toast.makeText(
                this@LoginActivity,
                "An error occurred while logging in",
                Toast.LENGTH_SHORT
            ).show()

        }
    }

    private fun goToTheNextPage(user: User?) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(Constants.USER, user)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun isEmailValid(emailAddress: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()
    }
}