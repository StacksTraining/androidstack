package com.magda.noteapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.magda.noteapp.R
import com.magda.noteapp.model.User
import com.magda.noteapp.utils.Constants
import com.magda.noteapp.utils.SessionManager

class SignUpActivity : AppCompatActivity() {

    private lateinit var fName: TextInputLayout
    private lateinit var lName: TextInputLayout
    private lateinit var eAddress: TextInputLayout
    private lateinit var mPassword: TextInputLayout
    private lateinit var mConfirmPassword: TextInputLayout
    private lateinit var mSignUpButton: Button
    private lateinit var mLogIn: TextView
    private val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        initViews()
        mSignUpButton.setOnClickListener { toValidateDataSent() }
        mLogIn.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
    }

    private fun toValidateDataSent() {
        val firstName = fName.editText?.text?.toString()?.trim()
        val lastName = lName.editText?.text?.toString()?.trim()
        val emailAddress = eAddress.editText?.text?.toString()?.trim()
        val password = mPassword.editText?.text?.toString()?.trim()
        val confirmPassword = mConfirmPassword.editText?.text?.toString()?.trim()

        if (firstName!!.isEmpty() || lastName!!.isEmpty() || emailAddress!!.isEmpty() || password!!.isEmpty() ||
            confirmPassword!!.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
        } else {
            toConfirmPassword(firstName, lastName, emailAddress, password, confirmPassword)
        }
    }

    private fun toConfirmPassword(
        firstName: String, lastName: String, emailAddress: String, password: String,
        confirmPassword: String
    ) {
        if (isEmailValid(emailAddress)) {
            if (password == confirmPassword) {
                authentication(firstName, lastName, emailAddress, password)
            } else {
                mPassword.error = "Password Mismatch"
                Toast.makeText(this@SignUpActivity, "Confirm your password", Toast.LENGTH_SHORT)
                    .show()
            }
        } else eAddress.error = "Invalid password"

    }

    private fun authentication(
        firstName: String,
        lastName: String,
        emailAddress: String,
        password: String
    ) {
        auth.createUserWithEmailAndPassword(emailAddress, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUser(firstName, lastName, emailAddress)
                } else Toast.makeText(this, "Error in sign up, try again", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUser(firstName: String, lastName: String, emailAddress: String) {
        val db = FirebaseFirestore.getInstance()
        val userReference = db.collection(Constants.USERS)
        val userId = if (auth.currentUser != null) {
            auth.currentUser!!.uid
        } else null
        val user = User(userId, firstName, lastName, emailAddress)
        userReference.document(userId!!).set(user).addOnSuccessListener {
            Toast.makeText(this, "Save data  successful", Toast.LENGTH_SHORT).show()
            val sessionManager = SessionManager(this@SignUpActivity)
            sessionManager.storeInfo(user)
            toTheNextPage(user)
        }.addOnFailureListener { e ->
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun toTheNextPage(user: User) {
        //intent
        val intent = Intent(this@SignUpActivity, HomeActivity::class.java)
        intent.putExtra(Constants.USER, user)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }

    private fun initViews() {
        fName = findViewById(R.id.fName)
        lName = findViewById(R.id.lName)
        eAddress = findViewById(R.id.emailAddress)
        mPassword = findViewById(R.id.password)
        mConfirmPassword = findViewById(R.id.confirmPassword)
        mSignUpButton = findViewById(R.id.btSignUp)
        mLogIn = findViewById(R.id.tvLogIn)
    }

    private fun isEmailValid(emailAddress: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()
    }

    //Scenario that this is a  splash screen
    private fun checkIfLoggedIn() {
        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this@SignUpActivity, HomeActivity::class.java)
            intent.putExtra(Constants.USER, sessionManager.getInfo())
            startActivity(intent)
        }else startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
    }

    //Scenario that we are logging out
    private fun logOut() {
        val mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        if (user != null) {
            mAuth.signOut()
            val sessionManager = SessionManager(this)
            sessionManager.clear()
        }
    }


}