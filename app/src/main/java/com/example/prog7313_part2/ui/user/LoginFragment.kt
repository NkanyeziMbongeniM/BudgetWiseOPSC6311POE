package com.example.prog7313_part2.ui.user

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.prog7313_part2.R
import com.example.prog7313_part2.databinding.FragmentLoginBinding
import com.example.prog7313_part2.ui.home.HomeFragment
import androidx.core.content.edit
import com.example.prog7313_part2.ui.main.MainActivity


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide bottom navigation
        requireActivity().findViewById<View>(R.id.navBarView)?.visibility = View.GONE

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Observe login result
        viewModel.loginResult.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .edit {
                        putInt("user_id", user.id)
                    }
                (requireActivity() as MainActivity).loadFragment(HomeFragment())
                requireActivity().findViewById<View>(R.id.navBarView)?.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Welcome, ${user.firstName}!", Toast.LENGTH_SHORT).show()
            } else {
                binding.tvError.text = "Invalid email or password"
                binding.tvError.visibility = View.VISIBLE
            }
        }

        // Login button click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                binding.tvError.text = "Please fill in all fields"
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tvError.text = "Invalid email format"
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            binding.tvError.visibility = View.GONE
            viewModel.login(email, password)
        }

        // Register text click
        binding.tvRegister.setOnClickListener {
            (requireActivity() as MainActivity).loadFragment(RegisterFragment())
        }

        // Forgot password (placeholder)
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(requireContext(), "Forgot password not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}