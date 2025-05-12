package com.example.prog7313_part2.ui.home

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prog7313_part2.R
import com.example.prog7313_part2.data.local.AppDatabase
import com.example.prog7313_part2.data.local.entities.Expense
import com.example.prog7313_part2.data.local.entities.User
import com.example.prog7313_part2.ui.budget.BudgetViewModel
import com.example.prog7313_part2.ui.category.CategoryViewModel
import com.example.prog7313_part2.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var tvUser: android.widget.TextView
    private lateinit var tvWelcome: android.widget.TextView
    private lateinit var totalBudget: android.widget.TextView
    private lateinit var totalSpent: android.widget.TextView
    private lateinit var totalNet: android.widget.TextView
    private lateinit var budgetProgress: android.widget.ProgressBar
    private lateinit var categorySpinner: Spinner
    private lateinit var btnAddCategory: android.widget.Button
    private lateinit var fromDateEditText: android.widget.EditText
    private lateinit var toDateEditText: android.widget.EditText
    private lateinit var filterButton: android.widget.Button
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var tvNoExpenses: android.widget.TextView
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()
    private lateinit var expenseAdapter: ExpenseAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedCategory: String? = null
    private var fromDate: Date? = null
    private var toDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvUser = view.findViewById(R.id.User)
        tvWelcome = view.findViewById(R.id.Welcome)
        totalBudget = view.findViewById(R.id.totalBudget)
        totalSpent = view.findViewById(R.id.totalSpent)
        totalNet = view.findViewById(R.id.totalNet)
        budgetProgress = view.findViewById(R.id.budgetProgress)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        btnAddCategory = view.findViewById(R.id.btnAddCategory)
        fromDateEditText = view.findViewById(R.id.fromDateEditText)
        toDateEditText = view.findViewById(R.id.toDateEditText)
        filterButton = view.findViewById(R.id.filterButton)
        recyclerView = view.findViewById(R.id.recyclerView)
        tvNoExpenses = view.findViewById(R.id.tvNoExpenses)

        // Setup RecyclerView for expenses
        expenseAdapter = ExpenseAdapter(emptyList()) { expense ->
            val fragment = com.example.prog7313_part2.ui.expenses.UpdateExpenseFragment.newInstance(expense)
            (requireActivity() as MainActivity).loadFragment(fragment)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = expenseAdapter

        return view
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("user_id", -1)
        if (userId == -1) {
            android.widget.Toast.makeText(requireContext(), "User not logged in", android.widget.Toast.LENGTH_SHORT).show()
            (requireActivity() as MainActivity).loadFragment(com.example.prog7313_part2.ui.user.LoginFragment())
            return
        }

        // Set up welcome message
        CoroutineScope(Dispatchers.Main).launch {
            val user: User? = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext()).userDao().getUserById(userId)
            }
            tvUser.text = user?.let { "Hello, ${it.firstName}!" } ?: "Hello!"
        }

        // Load categories for spinner
        categoryViewModel.loadCategories(userId)
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            val categoryNames = listOf("All Categories") + categories.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
            categorySpinner.isEnabled = categories.isNotEmpty()
            if (categories.isEmpty()) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "No categories available. Add one in Settings or below.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }

        // Add Category button
        btnAddCategory.setOnClickListener {
            (requireActivity() as MainActivity).loadFragment(com.example.prog7313_part2.ui.category.AddNewCategoryFragment())
        }

        // Setup date pickers
        fromDateEditText.setOnClickListener {
            showDatePicker { date ->
                fromDate = date
                fromDateEditText.setText(dateFormat.format(date))
            }
        }

        toDateEditText.setOnClickListener {
            showDatePicker { date ->
                toDate = date
                toDateEditText.setText(dateFormat.format(date))
            }
        }

        // Filter button
        filterButton.setOnClickListener {
            if (fromDate == null || toDate == null) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Please select both start and end dates",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (fromDate!!.after(toDate)) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Start date cannot be after end date",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            applyFilters()
        }

        // Load budget data
        budgetViewModel.initialize(requireContext())
        budgetViewModel.loadBudgetData(userId)
        budgetViewModel.budgetGoals.observe(viewLifecycleOwner) { goals ->
            if (goals == null) {
                totalBudget.visibility = View.GONE
                totalSpent.visibility = View.GONE
                totalNet.visibility = View.GONE
                budgetProgress.visibility = View.GONE
            } else {
                totalBudget.visibility = View.VISIBLE
                totalSpent.visibility = View.VISIBLE
                totalNet.visibility = View.VISIBLE
                budgetProgress.visibility = View.VISIBLE
                totalBudget.text = "Total: R${String.format("%.2f", goals.maxMonthlyGoal)}"
                val totalSpending = budgetViewModel.totalSpending.value ?: 0.0
                totalSpent.text = "Spent: R${String.format("%.2f", totalSpending)}"
                totalNet.text = "Net: R${String.format("%.2f", goals.maxMonthlyGoal - totalSpending)}"
                val progress = if (goals.maxMonthlyGoal > 0) {
                    ((totalSpending / goals.maxMonthlyGoal) * 100).toInt().coerceIn(0, 100)
                } else 0
                budgetProgress.progress = progress
            }
        }

        budgetViewModel.totalSpending.observe(viewLifecycleOwner) { totalSpending ->
            val goals = budgetViewModel.budgetGoals.value
            if (goals != null) {
                totalSpent.text = "Spent: R${String.format("%.2f", totalSpending)}"
                totalNet.text = "Net: R${String.format("%.2f", goals.maxMonthlyGoal - totalSpending)}"
                val progress = if (goals.maxMonthlyGoal > 0) {
                    ((totalSpending / goals.maxMonthlyGoal) * 100).toInt().coerceIn(0, 100)
                } else 0
                budgetProgress.progress = progress
            }
        }

        // Load expenses and apply filters
        budgetViewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            applyFilters(expenses)
        }

        // Filter expenses by category
        categorySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = parent.getItemAtPosition(position).toString()
                if (selectedCategory == "All Categories") selectedCategory = null
                applyFilters()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                onDateSelected(selectedDate.time)
            },
            year,
            month,
            day
        ).show()
    }

    private fun applyFilters(expenses: List<Expense>? = budgetViewModel.expenses.value) {
        var filteredExpenses = expenses ?: emptyList()

        // Filter by category
        if (selectedCategory != null) {
            filteredExpenses = filteredExpenses.filter { it.category == selectedCategory }
        }

        // Filter by date range
        if (fromDate != null && toDate != null) {
            filteredExpenses = filteredExpenses.filter { expense ->
                try {
                    val expenseDate = dateFormat.parse(expense.date)
                    expenseDate != null && !expenseDate.before(fromDate) && !expenseDate.after(toDate)
                } catch (e: Exception) {
                    false
                }
            }
        }

        // Update RecyclerView
        expenseAdapter.updateData(filteredExpenses)
        if (filteredExpenses.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvNoExpenses.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvNoExpenses.visibility = View.GONE
        }
    }

}

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val onItemClick: (Expense) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val tvDescription: android.widget.TextView = view.findViewById(android.R.id.text1)
        val tvAmount: android.widget.TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvDescription.text = "${expense.description} (${expense.category})"
        holder.tvAmount.text = "R${String.format("%.2f", expense.amount)} - ${expense.date}"
        holder.itemView.setOnClickListener { onItemClick(expense) }
    }

    override fun getItemCount(): Int = expenses.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}