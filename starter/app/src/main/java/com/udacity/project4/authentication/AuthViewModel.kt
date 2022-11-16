package com.udacity.project4.authentication

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {


    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?>
        get() = _currentUser

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    init {
        _currentUser.value = FirebaseAuth.getInstance().currentUser
    }

    fun handleFirebaseUserState(res: FirebaseAuthUIAuthenticationResult) {
        if (res.resultCode == AppCompatActivity.RESULT_OK) {
            _currentUser.value = FirebaseAuth.getInstance().currentUser
        } else {
            res.idpResponse?.error?.message?.let {
                _message.value = it
            }
        }
    }
}
