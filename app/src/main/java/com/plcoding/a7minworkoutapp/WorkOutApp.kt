package com.plcoding.a7minworkoutapp

import android.app.Application

class WorkOutApp:Application() {
    val db by lazy{
        HistoryDatabase.getInstance(this)
    }


}