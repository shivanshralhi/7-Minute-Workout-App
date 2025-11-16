package com.plcoding.a7minworkoutapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plcoding.a7minworkoutapp.databinding.ActivityUserprofileactivityBinding

class UserPreferencesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserprofileactivityBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserprofileactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)

        setupDropdowns()
        setupSaveButton()
    }

    private fun setupDropdowns() {
        val bodyTypes = listOf("Slim", "Average", "Overweight", "Athletic")
        val strengthLevels = listOf("Beginner", "Intermediate", "Advanced")

        binding.spinnerBodyType.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bodyTypes)

        binding.spinnerStrength.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, strengthLevels)
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val bodyType = binding.spinnerBodyType.selectedItem.toString()
            val strength = binding.spinnerStrength.selectedItem.toString()

            prefs.edit()
                .putString("BODY_TYPE", bodyType)
                .putString("STRENGTH_LEVEL", strength)
                .apply()

            Toast.makeText(this, "Preferences Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
