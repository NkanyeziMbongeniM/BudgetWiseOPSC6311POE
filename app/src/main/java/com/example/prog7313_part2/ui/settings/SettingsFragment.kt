package com.example.prog7313_part2.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.prog7313_part2.databinding.FragmentSettingsBinding
import com.example.prog7313_part2.data.local.AppDatabase
import com.example.prog7313_part2.data.local.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.edit
import com.example.prog7313_part2.ui.main.MainActivity

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SettingsFragment", "SettingsFragment initialized")

        // Get userId
        val userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("user_id", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            (requireActivity() as MainActivity).loadFragment(com.example.prog7313_part2.ui.user.LoginFragment())
            return
        }

        // Fetch user name
        CoroutineScope(Dispatchers.Main).launch {
            val user: User? = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext()).userDao().getUserById(userId)
            }
            user?.let { userEntity: User ->
                binding.tvCurrentUser.text = "Logged in as: ${userEntity.firstName}"
            } ?: run {
                binding.tvCurrentUser.text = "Logged in as: Unknown"
                Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
            }
        }

        // Verify logout button visibility
        if (binding.btnLogout.visibility != View.VISIBLE) {
            Log.w("SettingsFragment", "Logout button is not visible, setting to VISIBLE")
            binding.btnLogout.visibility = View.VISIBLE
        }

        binding.btnUploadCategories.setOnClickListener {
            Log.d("SettingsFragment", "Navigating to AddNewCategoryFragment")
            (requireActivity() as MainActivity).loadFragment(
                com.example.prog7313_part2.ui.category.AddNewCategoryFragment()
            )
        }

        binding.btnViewAllCategories.setOnClickListener {
            Log.d("SettingsFragment", "Navigating to ViewAllCategoriesFragment")
            (requireActivity() as MainActivity).loadFragment(
                com.example.prog7313_part2.ui.category.ViewAllCategoriesFragment()
            )
        }

        binding.btnViewBudget.setOnClickListener {
            Log.d("SettingsFragment", "Navigating to BudgetFragment")
            (requireActivity() as MainActivity).loadFragment(
                com.example.prog7313_part2.ui.budget.BudgetFragment()
            )
        }

        binding.btnLogout.setOnClickListener {
            Log.d("SettingsFragment", "Logging out user")
            requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .edit {
                    clear()
                }
            (requireActivity() as MainActivity).loadFragment(
                com.example.prog7313_part2.ui.user.LoginFragment()
            )
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                com.example.prog7313_part2.R.id.navBarView
            ).visibility = View.GONE
        }

        // TODO: Implement btnUpdateProfile and btnChangePassword
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("SettingsFragment", "SettingsFragment destroyed")
        _binding = null
    }
}