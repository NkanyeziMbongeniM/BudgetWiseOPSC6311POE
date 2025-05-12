package com.example.prog7313_part2.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.prog7313_part2.R
import com.example.prog7313_part2.ui.coupon.CouponFragment
import com.example.prog7313_part2.ui.expenses.EnterExpenseFragment
import com.example.prog7313_part2.ui.expenses.ViewAllExpensesFragment
import com.example.prog7313_part2.ui.home.HomeFragment
import com.example.prog7313_part2.ui.settings.SettingsFragment
import com.example.prog7313_part2.ui.user.LoginFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check login status from SharedPreferences
        val userId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("user_id", -1) // Default value is -1 if not logged in

        // If no user is logged in, load LoginFragment
        if (userId == -1) {
            loadFragment(LoginFragment())
            findViewById<BottomNavigationView>(R.id.navBarView).visibility = View.GONE
        } else {
            loadFragment(HomeFragment())
            findViewById<BottomNavigationView>(R.id.navBarView).visibility = View.VISIBLE
        }

        // Setup bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.navBarView)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> loadFragment(HomeFragment())
                R.id.view_all_expenses -> loadFragment(ViewAllExpensesFragment())
                R.id.enter_expense -> loadFragment(EnterExpenseFragment())
                R.id.coupon -> loadFragment(CouponFragment())
                R.id.settings -> loadFragment(SettingsFragment())
            }
            true
        }
    }
}
