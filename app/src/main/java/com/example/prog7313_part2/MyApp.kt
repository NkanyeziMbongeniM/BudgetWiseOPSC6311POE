package com.example.prog7313_part2

import android.app.Application
import com.example.prog7313_part2.data.local.AppDatabase


class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Trigger the database initialization on app start
        AppDatabase.getDatabase(applicationContext)
    }
}
