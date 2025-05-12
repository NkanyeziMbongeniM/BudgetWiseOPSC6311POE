package com.example.prog7313_part2.ui.expenses


import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prog7313_part2.data.local.entities.Expense
import com.example.prog7313_part2.databinding.ItemExpenseBinding
import java.text.NumberFormat


class ExpenseAdapter(
    private var expenseList: List<Expense>,
    private val onItemClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val expandedItems = mutableSetOf<Int>()
    private var lastClickTime = 0L
    private var lastClickedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        val isExpanded = expandedItems.contains(position)

        holder.bind(expense, isExpanded)

        holder.itemView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            val currentAdapterPosition = holder.adapterPosition

            if (lastClickedPosition == currentAdapterPosition && currentTime - lastClickTime < 300) {
                onItemClick(expense) // Double-click
                lastClickTime = 0L
                lastClickedPosition = -1
            } else {
                if (expandedItems.contains(currentAdapterPosition)) {
                    expandedItems.remove(currentAdapterPosition)
                } else {
                    expandedItems.add(currentAdapterPosition)
                }
                notifyItemChanged(currentAdapterPosition)

                lastClickTime = currentTime
                lastClickedPosition = currentAdapterPosition
            }
        }
    }

    override fun getItemCount(): Int = expenseList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<Expense>) {
        this.expenseList = newList
        expandedItems.clear()
        notifyDataSetChanged()
    }

    fun getCurrentExpenses(): List<Expense> = expenseList

    class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(expense: Expense, isExpanded: Boolean) {
            binding.tvExpenseDescription.text = expense.description
            expense.date.also { binding.tvExpenseDate.text = it }
            binding.tvExpenseAmount.text = NumberFormat.getCurrencyInstance().format(expense.amount)
            binding.tvExpenseCategory.text = "Category: ${expense.category}"

            // Load photo using Glide
            if (!expense.imageUri.isNullOrEmpty()) {
                Glide.with(binding.ivExpensePhoto.context)
                    .load(expense.imageUri) // URI or URL of the image
                    .placeholder(android.R.drawable.ic_menu_gallery) // Placeholder while loading
                    .error(android.R.drawable.ic_menu_report_image) // Error image in case of failure
                    .into(binding.ivExpensePhoto)

                Log.d("ExpenseAdapter", "Loaded photo for expense ${expense.id} from ${expense.imageUri}")
            } else {
                binding.ivExpensePhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            binding.detailsContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
        }
    }
}
