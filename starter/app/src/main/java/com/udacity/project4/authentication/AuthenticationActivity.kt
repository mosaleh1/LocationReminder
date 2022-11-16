package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*
import kotlinx.android.synthetic.main.activity_authentication.view.*

private const val SIGN_IN_REQUEST_CODE = 25

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {


    private lateinit var authenticationViewModel: AuthViewModel
    private lateinit var binding: ActivityAuthenticationBinding



    private val launcher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        authenticationViewModel.handleFirebaseUserState(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // DONE TODO: If the user was authenticated, send him to RemindersActivity
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            navigateToMain()
        }
        binding.signInBtn.setOnClickListener {
            Toast.makeText(this, "clicked", Toast.LENGTH_LONG).show()
        }

        //DONE TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        authenticationViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.signInBtn.setOnClickListener {
            startLoginUser()
        }

        authenticationViewModel.currentUser.observe(this) { user ->
            if (user != null) {
                navigateToMain()
            }

        }
        authenticationViewModel.message.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }

//       Not Mandatory   TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }


    private fun startLoginUser() {
        Toast.makeText(this, "clicked", Toast.LENGTH_LONG).show()
        val provider = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(provider)
            .setIsSmartLockEnabled(false)
            .build()
        launcher.launch(signInIntent)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, RemindersActivity::class.java))
        finish()
    }
}
