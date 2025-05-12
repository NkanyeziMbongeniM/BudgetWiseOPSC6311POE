package com.example.prog7313_part2.ui.category

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.prog7313_part2.databinding.FragmentAddNewCategoryBinding
import com.example.prog7313_part2.data.local.entities.Category
import com.example.prog7313_part2.ui.main.MainActivity

class AddNewCategoryFragment : Fragment() {
    private var _binding: FragmentAddNewCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CategoryViewModel
    private var categoryId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[CategoryViewModel::class.java]

        // Get userId
        val userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("user_id", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            (requireActivity() as MainActivity).loadFragment(com.example.prog7313_part2.ui.user.LoginFragment())
            return
        }

        // Check if editing
        categoryId = arguments?.getLong("categoryId")
        if (categoryId != null) {
            viewModel.loadCategory(categoryId!!)
            viewModel.category.observe(viewLifecycleOwner) { category ->
                category?.let {
                    binding.categoryName.setText(it.name)
                    binding.categoryDescription.setText(it.description)
                    binding.categoryPriceLimit.setText(it.priceLimit.toString())
                }
            }
        }

        // Save category
        binding.addCategoryButton.setOnClickListener {
            val name = binding.categoryName.text.toString().trim()
            val desc = binding.categoryDescription.text.toString().trim()
            val limit = binding.categoryPriceLimit.text.toString().trim().toDoubleOrNull()

            if (name.isEmpty() || desc.isEmpty() || limit == null) {
                Toast.makeText(requireContext(), "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = Category(
                id = categoryId ?: 0,
                name = name,
                description = desc,
                priceLimit = limit,
                userId = userId
            )

            if (categoryId != null) {
                viewModel.updateCategory(category)
                Toast.makeText(requireContext(), "Category updated", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.insertCategory(category)
                Toast.makeText(requireContext(), "Category added", Toast.LENGTH_SHORT).show()
            }

            // Navigate back to SettingsFragment
            (requireActivity() as MainActivity).loadFragment(com.example.prog7313_part2.ui.settings.SettingsFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}