package com.bussiness.pickup

import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.bussiness.pickup.databinding.ActivityChoiceBinding
import com.bussiness.pickup.riderStack.RiderLoginActivity

class ChoiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChoiceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using the binding class
        binding = ActivityChoiceBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        binding.iMRider.setOnClickListener {
            startActivity(Intent(this@ChoiceActivity, RiderLoginActivity::class.java))
            
        }
    }
}