package com.example.prog7313_part2.ui.expenses

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_part2.R
import com.example.prog7313_part2.data.local.entities.Category
import com.example.prog7313_part2.data.local.entities.Expense
import com.example.prog7313_part2.ui.main.MainActivity
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViewAllExpensesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var filterPeriodSpinner: AutoCompleteTextView
    private lateinit var dateRangeContainer: LinearLayout
    private lateinit var fromDateField: TextInputEditText
    private lateinit var toDateField: TextInputEditText
    private lateinit var categorySummaryTextView: TextView
    private val viewModel: ViewAllExpensesViewModel by viewModels()

    private var selectedFromDate: String? = null
    private var selectedToDate: String? = null
    private var allExpenses: List<Expense> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_all_expenses, container, false)

        // Initialize views
        recyclerView = view.findViewById(R.id.rvExpenses)
        filterPeriodSpinner = view.findViewById(R.id.spinnerPeriod)
        dateRangeContainer = view.findViewById(R.id.dateRangeContainer)
        fromDateField = view.findViewById(R.id.etFromDate)
        toDateField = view.findViewById(R.id.etToDate)
        categorySummaryTextView = view.findViewById(R.id.textViewCategorySummary)

        // UI colors and dropdown
        filterPeriodSpinner.setDropDownBackgroundResource(R.color.white)
        val textInputLayout = filterPeriodSpinner.parent.parent as? com.google.android.material.textfield.TextInputLayout
        textInputLayout?.defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.BlueP))
        textInputLayout?.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.BlueP)))

        // Adapter and RecyclerView
        expenseAdapter = ExpenseAdapter(emptyList()) { selectedExpense ->
            val bundle = Bundle().apply {
                putParcelable("selected_expense", selectedExpense)
            }
            val fragment = UpdateExpenseFragment()
            fragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = expenseAdapter

        // Setup spinner filter
        val timePeriodOptions = arrayOf(
            "This month", "1 Month ago", "2 Months ago", "3 Months ago",
            "6 Months ago", "1 Year ago", "Show All"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, timePeriodOptions)
        filterPeriodSpinner.setAdapter(adapter)
        filterPeriodSpinner.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = timePeriodOptions[position]
            filterExpensesByPeriod(selectedOption)
        }

        view.findViewById<ImageView>(R.id.icCalendar).setOnClickListener { showFromDatePicker() }
        fromDateField.setOnClickListener {
            showDatePickerDialog { date ->
                selectedFromDate = date
                fromDateField.setText(date)
                showToDatePicker()
            }
        }
        toDateField.setOnClickListener {
            showDatePickerDialog { date ->
                selectedToDate = date
                toDateField.setText(date)
                filterExpensesByDateRange()
            }
        }

        // Load expenses for the current user
        val userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("user_id", -1)
        if (userId == -1) {
            (requireActivity() as MainActivity).loadFragment(com.example.prog7313_part2.ui.user.LoginFragment())
            return view
        }

        viewModel.loadExpenses(userId)
        viewModel.loadCategories(userId)

        viewModel.userExpenses.observe(viewLifecycleOwner) { expenses ->
            allExpenses = expenses.filter { it.userId == userId } // Filter expenses for current user
            expenseAdapter.updateData(expenses)
            updateTotalAmount(view)
            viewModel.userCategories.value?.let { categories ->
                updateCategorySummary(expenses, categories)
            }
        }

        viewModel.userCategories.observe(viewLifecycleOwner) { categories ->
            updateCategorySummary(allExpenses, categories)
        }

        return view
    }

    private fun filterExpensesByPeriod(period: String) {
        val dateRange = getDateRangeForPeriod(period)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateRange.first)
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateRange.second)

        // Get the current user's ID
        val userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Filter the expenses for the current user
        val filteredList = allExpenses.filter { expense ->
            expense.userId == userId && // Check if the expense belongs to the current user
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expense.date) in startDate..endDate
        }

        // Update the adapter with the filtered list of expenses
        expenseAdapter.updateData(filteredList)

        // Update the category summary based on the filtered expenses
        updateCategorySummary(filteredList, viewModel.userCategories.value ?: emptyList())

        // Update the total amount
        updateTotalAmount(requireView())
    }

    @SuppressLint("SetTextI18n")
    private fun updateCategorySummary(
        expenses: List<Expense>,
        categories: List<Category>
    ) {
        if (expenses.isEmpty()) {
            categorySummaryTextView.text = "No expenses to show"
            return
        }

        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { (_, expensesInCategory) ->
                expensesInCategory.sumOf { it.amount }
            }

        val builder = StringBuilder()

        categoryTotals.forEach { (category, totalSpent) ->
            val limit = categories.find { it.name == category }?.priceLimit ?: 0.0
            val percentage = if (limit > 0) (totalSpent / limit) * 100 else 0.0

            val color = when {
                percentage <= 50 -> ContextCompat.getColor(requireContext(), R.color.green)
                percentage <= 80 -> ContextCompat.getColor(requireContext(), R.color.orange)
                else -> ContextCompat.getColor(requireContext(), R.color.red)
            }

            val coloredText = "<font color='#${Integer.toHexString(color).drop(2)}'>" +
                    "$category: R%.2f (%.0f%% of R%.2f)</font><br>".format(totalSpent, percentage, limit)

            builder.append(coloredText)
        }

        categorySummaryTextView.text = android.text.Html.fromHtml(builder.toString(), android.text.Html.FROM_HTML_MODE_LEGACY)
    }

    private fun filterExpensesByDateRange() {
        if (selectedFromDate != null && selectedToDate != null) {
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(
                selectedFromDate.toString()
            )
            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedToDate.toString())

            // Get the current user's ID
            val userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getInt("user_id", -1)

            if (userId == -1) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return
            }

            // Filter the expenses for the current user
            val filteredList = allExpenses.filter { expense ->
                expense.userId == userId && // Check if the expense belongs to the current user
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expense.date) in startDate..endDate
            }

            // Update the adapter with the filtered list of expenses
            expenseAdapter.updateData(filteredList)

            // Update the category summary based on the filtered expenses
            updateCategorySummary(filteredList, viewModel.userCategories.value ?: emptyList())

            // Update the total amount
            updateTotalAmount(requireView())
        } else {
            Toast.makeText(requireContext(), "Please select both From and To dates", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                onDateSelected(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showFromDatePicker() {
        showDatePickerDialog { date ->
            selectedFromDate = date
            fromDateField.setText(date)
            showToDatePicker()
        }
    }

    private fun showToDatePicker() {
        showDatePickerDialog { date ->
            selectedToDate = date
            toDateField.setText(date)
            filterExpensesByDateRange()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTotalAmount(view: View) {
        val totalAmountTextView = view.findViewById<TextView>(R.id.tvTotalAmount)
        val currentExpenses = expenseAdapter.getCurrentExpenses()
        val totalAmount = currentExpenses.sumOf { it.amount }
        totalAmountTextView.text = "Total Amount: R%.2f".format(totalAmount)
    }

    private fun getDateRangeForPeriod(period: String): Pair<String, String> {
        val calendar = Calendar.getInstance()
        when (period) {
            "This month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                val endOfMonth = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.apply { add(Calendar.MONTH, 1) }.time)
                return Pair(startOfMonth, endOfMonth)
            }
            "1 Month ago" -> {
                calendar.add(Calendar.MONTH, -1)
                val startOfMonth = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                val endOfMonth = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.apply { add(Calendar.MONTH, 1) }.time)
                return Pair(startOfMonth, endOfMonth)
            }
            else -> return Pair("2000-01-01", "9999-12-31") // Show all expenses
        }
    }
}
