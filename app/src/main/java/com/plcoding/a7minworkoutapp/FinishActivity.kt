package com.plcoding.a7minworkoutapp

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.plcoding.a7minworkoutapp.databinding.ActivityFinishBinding
import kotlinx.coroutines.launch
import java.util.Locale

class FinishActivity : AppCompatActivity() {

    private var binding:ActivityFinishBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityFinishBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarFinishActivity)
        if(supportActionBar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarFinishActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
        binding?.btnFinish?.setOnClickListener {
//            val intent = Intent(this,MainActivity::class.java)
//            startActivity(intent)
            finish()
        }

        val historyDao = (application as WorkOutApp).db.historyDao()
        addDateToDatabase(historyDao)


    }

    private fun addDateToDatabase(historyDao : HistoryDao){
        val myCalendar = Calendar.getInstance()
        val dateTime = myCalendar.time
        Log.e("Date : ",""+dateTime)

        val sdf = java.text.SimpleDateFormat("dd MM yyyy HH:mm:ss", Locale.getDefault())
        val date = sdf.format(dateTime)
        Log.e("Formatted Date : ",""+date)


        lifecycleScope.launch {
            historyDao.insert(HistoryEntity(date))
            Log.e("Date","Added")

        }

    }
}