package com.example.prog7313_part2.ui.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.prog7313_part2.R
import com.example.prog7313_part2.databinding.FragmentRegisterBinding
import com.example.prog7313_part2.ui.main.MainActivity

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide bottom navigation
        requireActivity().findViewById<View>(R.id.navBarView)?.visibility = View.GONE

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        // Observe registration result
        viewModel.registrationResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Registration successful! Please log in.", Toast.LENGTH_SHORT).show()
                (requireActivity() as MainActivity).loadFragment(LoginFragment())
            } else {
                binding.tvError.text = "Registration failed. Email may already exist."
                binding.tvError.visibility = View.VISIBLE
            }
        }

        // Register button click
        binding.btnRegister.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val age = binding.etAge.text.toString().trim().toIntOrNull() ?: 0

            if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                binding.tvError.text = "Please fill in all fields"
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tvError.text = "Invalid email format"
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                binding.tvError.text = "Passwords do not match"
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.tvError.text = "Password must be at least 6 characters"
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            binding.tvError.visibility = View.GONE
            viewModel.register(firstName, lastName, email, password, age)
        }

        // Back to login
        binding.ivBackArrow.setOnClickListener {
            (requireActivity() as MainActivity).loadFragment(LoginFragment())
        }

        // Login text click
        binding.tvLogin.setOnClickListener {
            (requireActivity() as MainActivity).loadFragment(LoginFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}