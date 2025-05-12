package com.example.prog7313_part2.ui.category

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prog7313_part2.databinding.FragmentViewAllCategoriesBinding
import com.example.prog7313_part2.data.local.entities.Category
import com.example.prog7313_part2.ui.main.MainActivity

class ViewAllCategoriesFragment : Fragment() {
    private var _binding: FragmentViewAllCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CategoryViewModel
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewAllCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewModelProvider(this)[CategoryViewModel::class.java].also { viewModel = it }

        // Get userId
        val userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("user_id", -1)
        if (userId == -1) {
            (requireActivity() as MainActivity).loadFragment(com.example.prog7313_part2.ui.user.LoginFragment())
            return
        }

        // Setup RecyclerView
        adapter = CategoryAdapter(
            onEditClick = { category ->
                val bundle = Bundle().apply { putLong("categoryId", category.id) }
                val fragment = AddNewCategoryFragment()
                fragment.arguments = bundle
                (requireActivity() as MainActivity).loadFragment(fragment)
            }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter

        // Load categories
        viewModel.loadCategories(userId)
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories)
            binding.tvEmpty.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
        }

        // Add category
        binding.addCategoryButton.setOnClickListener {
            (requireActivity() as MainActivity).loadFragment(
                AddNewCategoryFragment()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class CategoryAdapter(
    private val onEditClick: (Category) -> Unit
) : androidx.recyclerview.widget.ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = com.example.prog7313_part2.databinding.ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: com.example.prog7313_part2.databinding.ItemCategoryBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name
            binding.tvCategoryDescription.text = category.description
            binding.tvCategoryLimit.text = "$${String.format("%.2f", category.priceLimit)}"
            binding.root.setOnClickListener { onEditClick(category) }
            // Add delete button if needed
        }
    }
}