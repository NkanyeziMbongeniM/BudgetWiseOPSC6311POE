package com.example.prog7313_part2.ui.budget

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313_part2.R
import com.example.prog7313_part2.data.local.entities.BudgetGoals
import com.example.prog7313_part2.ui.main.MainActivity

class BudgetFragment : Fragment() {

    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var tvBudgetStatus: TextView
    private lateinit var rvCategoryTotals: RecyclerView
    private lateinit var btnCreateEditBudget: Button
    private lateinit var categoryAdapter: CategoryTotalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget, container, false)

        tvBudgetStatus = view.findViewById(R.id.tvBudgetStatus)
        rvCategoryTotals = view.findViewById(R.id.rvCategoryTotals)
        btnCreateEditBudget = view.findViewById(R.id.btnCreateEditBudget)

        // Setup RecyclerView
        categoryAdapter = CategoryTotalAdapter(emptyList())
        rvCategoryTotals.layoutManager = LinearLayoutManager(requireContext())
        rvCategoryTotals.adapter = categoryAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("user_id", -1)
        if (userId == -1) {
            (requireActivity() as MainActivity).loadFragment(com.example.prog7313_part2.ui.user.LoginFragment())
            return
        }

        // Initialize ViewModel with context
        viewModel.initialize(requireContext())
        viewModel.loadBudgetData(userId)

        // Observe ViewModel
        viewModel.budgetGoals.observe(viewLifecycleOwner) { goals ->
            updateBudgetStatus(goals, viewModel.totalSpending.value ?: 0.0)
        }

        viewModel.totalSpending.observe(viewLifecycleOwner) { total ->
            updateBudgetStatus(viewModel.budgetGoals.value, total)
        }

        viewModel.categoryTotals.observe(viewLifecycleOwner) { totals ->
            categoryAdapter.updateData(totals)
        }

        // Create/Edit budget button
        btnCreateEditBudget.setOnClickListener {
            showBudgetGoalsDialog(viewModel.budgetGoals.value)
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun updateBudgetStatus(goals: BudgetGoals?, totalSpending: Double) {
        if (goals == null) {
            tvBudgetStatus.text = "No budget goals set"
            btnCreateEditBudget.text = "Create Budget"
            return
        }

        val progress = if (goals.maxMonthlyGoal > 0) {
            (totalSpending / goals.maxMonthlyGoal * 100).coerceIn(0.0, 100.0)
        } else 0.0

        tvBudgetStatus.text = """
            Budget Goals for ${goals.monthYear}:
            Min: R${String.format("%.2f", goals.minMonthlyGoal)}
            Max: R${String.format("%.2f", goals.maxMonthlyGoal)}
            Spent: R${String.format("%.2f", totalSpending)}
            Progress: ${String.format("%.1f", progress)}%
        """.trimIndent()
        btnCreateEditBudget.text = "Edit Budget"
    }

    private fun showBudgetGoalsDialog(currentGoals: BudgetGoals?) {
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_budget_goals, null)
        val etMinGoal = dialogView.findViewById<EditText>(R.id.etMinGoal)
        val etMaxGoal = dialogView.findViewById<EditText>(R.id.etMaxGoal)

        currentGoals?.let {
            etMinGoal.setText(it.minMonthlyGoal.toString())
            etMaxGoal.setText(it.maxMonthlyGoal.toString())
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle(if (currentGoals == null) "Create Budget Goals" else "Edit Budget Goals")
            .setView(dialogView)
            .setPositiveButton("Save", null) // We handle validation inside onShowListener
            .setNegativeButton("Cancel", null)

        val dialog = dialogBuilder.create()

        // Apply Enter Animation
        dialog.window?.attributes?.windowAnimations = R.style.DialogSlideAnimationStyle // Apply entry animation

        // Apply Exit Animation (using dismiss listener)
        dialog.setOnDismissListener {
            dialog.window?.attributes?.windowAnimations = R.style.DialogExitAnimationStyle // Exit animation
        }

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val min = etMinGoal.text.toString().toDoubleOrNull()
                val max = etMaxGoal.text.toString().toDoubleOrNull()

                if (min == null || max == null || min > max) {
                    Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.setBudgetGoals(min, max)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }


}

class CategoryTotalAdapter(private var totals: List<Pair<String, Double>>) :
    RecyclerView.Adapter<CategoryTotalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(android.R.id.text1)
        val tvAmount: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (category, amount) = totals[position]
        holder.tvCategory.text = category
        holder.tvAmount.text = "R${String.format("%.2f", amount)}"
    }

    override fun getItemCount(): Int = totals.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newTotals: List<Pair<String, Double>>) {
        totals = newTotals
        notifyDataSetChanged()
    }
}