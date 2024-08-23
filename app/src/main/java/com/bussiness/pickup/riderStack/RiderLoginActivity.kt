package com.bussiness.pickup.riderStack

import RiderCommon
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bussiness.pickup.databinding.ActivityRiderLoginBinding
import com.bussiness.pickup.riderStack.riderModel.DriverInfoModel
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.concurrent.TimeUnit

class RiderLoginActivity : AppCompatActivity() {

    companion object {
         lateinit var firebaseAuth: FirebaseAuth
         lateinit var database: FirebaseDatabase
         lateinit var driverInfoReference: DatabaseReference
         lateinit var storageReference: StorageReference

        fun init() {
            if (!Companion::firebaseAuth.isInitialized) {
                firebaseAuth = FirebaseAuth.getInstance()
                database = FirebaseDatabase.getInstance()
                storageReference = FirebaseStorage.getInstance().reference
                driverInfoReference = database.getReference("Users").child(RiderCommon.DRIVER_INFO_REFERENCE)
            }
        }
    }

    private lateinit var binding: ActivityRiderLoginBinding
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        // Initialize Firebase and other necessary components
        init()

        if (firebaseAuth.currentUser != null) {
            // Show progress bar while checking user role
            checkUserRoles()
        } else {
            // Initialize the UI components and set the content view
            initUI()
            showLoginLayout()
        }
    }

    private fun initUI() {
        // Initialize binding and set the content view only after initialization

        binding = ActivityRiderLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun checkUserRoles() {
        // Show a progress dialog or similar UI component here
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val userId = firebaseAuth.currentUser?.uid ?: return
        val userRef = driverInfoReference.child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                val role = dataSnapshot.child("role").getValue<String>()
                if (role == "rider") {
                    startActivity(Intent(this@RiderLoginActivity, RiderActivity::class.java))
                    finish() // Close the login activity
                } else {
                    initUI() // Initialize the UI only if the role check fails
                    Toast.makeText(
                        this@RiderLoginActivity,
                        "User is not a customer.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss() // Dismiss progress dialog
                initUI() // Initialize the UI if there's an error
                Toast.makeText(
                    this@RiderLoginActivity,
                    "Failed to check user roles: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun sendOTP() {
        val phoneNumber = binding.phoneNumber.text.toString().trim()
        if (phoneNumber.isNotEmpty() && phoneNumber.length >= 10) { // Validate phone number
            val fullPhoneNumber = "+${binding.ccp.selectedCountryCode}${phoneNumber}"
            binding.OTPLayout.visibility = View.VISIBLE
            Toast.makeText(this,"$fullPhoneNumber",Toast.LENGTH_SHORT).show()
            sendVerificationCode(fullPhoneNumber)
            binding.loginLayout.visibility = View.INVISIBLE
        } else {
            binding.progress.visibility = View.GONE
            Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendOtp() {
        val phoneNumber = binding.phoneNumber.text.toString().trim()
        if (phoneNumber.isNotEmpty() && phoneNumber.length >= 10) { // Validate phone number
            val fullPhoneNumber = "+${binding.ccp.selectedCountryCode}${phoneNumber}"
            resendVerificationCode(fullPhoneNumber, resendToken)
            binding.loginLayout.visibility = View.INVISIBLE
        } else {
            binding.progress.visibility = View.GONE
            Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resendVerificationCode(phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken?) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(token!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
            binding.progress.visibility = View.GONE
        }

        override fun onVerificationFailed(e: FirebaseException) {
            binding.progress.visibility = View.GONE
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    Toast.makeText(this@RiderLoginActivity, "Invalid request. Please check your phone number.${e.message}", Toast.LENGTH_LONG).show()
                }
                is FirebaseTooManyRequestsException -> {
                    Toast.makeText(this@RiderLoginActivity, "Too many requests. Please wait a while before trying again.${e.message
                    }", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(this@RiderLoginActivity, "Verification failed. Please try again.${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            binding.resendOtp.visibility = View.VISIBLE
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, token)
            this@RiderLoginActivity.verificationId = verificationId
            this@RiderLoginActivity.resendToken = token
            binding.progress.visibility = View.GONE
            binding.resendOtp.visibility = View.VISIBLE
        }
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.progress.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Toast.makeText(this, "Logged in as: ${user?.phoneNumber}", Toast.LENGTH_LONG).show()
                    binding.progress.visibility = View.VISIBLE
                    checkUserFromFirebase()
                } else {
                    Log.e("PhoneAuth", "Error: ${task.exception?.message}")
                    Toast.makeText(this, "Incorrect OTP code. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkUserFromFirebase() {
        driverInfoReference
            .child(firebaseAuth.currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val model = snapshot.getValue(DriverInfoModel::class.java)
                        goToRiderActivity(model)
                    } else {
                        showRegisterLayout()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RiderLoginActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showLoginLayout() {
        binding.loginLayout.visibility = View.VISIBLE

        binding.continueWithPhoneNumber.setOnClickListener {
            binding.progress.visibility = View.VISIBLE
            sendOTP()
        }

        binding.startYourJourney.setOnClickListener {
            val otp = binding.otp.text.toString().trim()
            if (otp.isNotEmpty()) {
                verifyCode(otp)
            } else {
                Toast.makeText(this, "Enter the verification code", Toast.LENGTH_SHORT).show()
            }
        }

        binding.resendOtp.setOnClickListener {
            binding.progress.visibility = View.VISIBLE
            resendOtp()
        }
    }

    private fun showRegisterLayout() {
        binding.progress.visibility = View.VISIBLE
        binding.loginLayout.visibility = View.GONE
        binding.OTPLayout.visibility = View.GONE
        binding.registerLayout.visibility = View.VISIBLE

        val edtFirstName = binding.firstName
        val edtLastName = binding.lastName
        val edtPhone = binding.txtPhone
        val btnContinue = binding.continueTag
        // Use libphonenumber to parse the phone number

        val phoneNumberUtil = PhoneNumberUtil.getInstance()

        // Get the current phone number
        FirebaseAuth.getInstance().currentUser?.phoneNumber?.let { phoneNumber ->
            try {
                // Parse the phone number
                val parsedNumber = phoneNumberUtil.parse(phoneNumber, null)

                // Format the phone number without the country code
                val nationalNumber = phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)

                // Remove leading zero and country code
                val numberWithoutCountryCode = nationalNumber
                    .replaceFirst("0", "")  // Remove leading zeros
                    .replaceFirst("^\\+", "") // Remove country code if present

                // Set the formatted number to the EditText
                edtPhone.setText(numberWithoutCountryCode)
            } catch (e: Exception) {
                // Handle parsing exception if the phone number is invalid
                Log.e("PhoneNumber", "Error parsing phone number: ${e.message}")
            }
        }

        btnContinue.setOnClickListener {
            val firstName = edtFirstName.text.toString().trim()
            val lastName = edtLastName.text.toString().trim()
            val phoneNumber = edtPhone.text.toString().trim()
            val fullPhoneNumber = "+${binding.ccp2.selectedCountryCode}${phoneNumber}"

            when {
                firstName.isEmpty() -> Toast.makeText(this, "Please Enter First Name", Toast.LENGTH_SHORT).show()
                lastName.isEmpty() -> Toast.makeText(this, "Please Enter Last Name", Toast.LENGTH_SHORT).show()
                phoneNumber.isEmpty() -> Toast.makeText(this, "Please Enter Phone Number", Toast.LENGTH_SHORT).show()
                else -> {
                    val model = DriverInfoModel().apply {
                        this.firstName = firstName
                        this.lastName = lastName
                        this.phoneNumber = fullPhoneNumber
                        this.rating = 0.0
                        this.role = "rider"
                    }

                    driverInfoReference.child(firebaseAuth.currentUser!!.uid)
                        .setValue(model)
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            binding.progress.visibility = View.GONE
                        }
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show()
                            binding.registerLayout.visibility = View.GONE
                            binding.progress.visibility = View.VISIBLE
                            goToRiderActivity(model)
                        }
                }
            }
        }

    }

    private fun goToRiderActivity(model: DriverInfoModel?) {
        RiderCommon.currentUser = model
        startActivity(Intent(this@RiderLoginActivity, RiderActivity::class.java))
        finish()
    }
}
