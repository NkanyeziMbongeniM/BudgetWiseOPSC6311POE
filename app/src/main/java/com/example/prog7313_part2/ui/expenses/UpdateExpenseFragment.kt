package com.example.prog7313_part2.ui.expenses

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.prog7313_part2.R
import com.example.prog7313_part2.data.local.entities.Expense
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri
import com.example.prog7313_part2.ui.main.MainActivity

@Suppress("DEPRECATION")
class UpdateExpenseFragment : Fragment() {

    private lateinit var expense: Expense
    private var selectedImageUri: Uri? = null

    // Views
    private lateinit var tvTitle: TextView
    private lateinit var ivExpensePhoto: ImageView
    private lateinit var etAmount: TextInputEditText
    private lateinit var tvDate: TextView
    private lateinit var etDescription: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnUpdateExpense: Button
    private lateinit var btnAddReceiptImage: ImageButton

    private val calendar = Calendar.getInstance()
    private val expenseViewModel: ExpenseViewModel by activityViewModels()

    // Image picker launcher with persistable permission
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedImageUri = it
            ivExpensePhoto.setImageURI(it)
            ivExpensePhoto.tag = it.toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_update_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tvTitle)
        ivExpensePhoto = view.findViewById(R.id.ivExpensePhoto)
        etAmount = view.findViewById(R.id.etAmount)
        tvDate = view.findViewById(R.id.tvDate)
        etDescription = view.findViewById(R.id.etDescription)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        btnUpdateExpense = view.findViewById(R.id.btnUpdateExpense)
        btnAddReceiptImage = view.findViewById(R.id.btnAddReceiptImage)

        arguments?.getParcelable<Expense>("selected_expense")?.let {
            expense = it
            bindExpenseData(expense)
        }

        btnUpdateExpense.setOnClickListener {
            updateExpenseDetails()
        }

        tvDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnAddReceiptImage.setOnClickListener {
            openImagePicker()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindExpenseData(expense: Expense) {
        tvTitle.text = "Update: ${expense.description}"
        etAmount.setText(expense.amount.toString())
        tvDate.text = expense.date
        etDescription.setText(expense.description)

        // Load image from URI
        if (!expense.imageUri.isNullOrEmpty()) {
            try {
                val uri = expense.imageUri.toUri()
                ivExpensePhoto.setImageURI(uri)
                ivExpensePhoto.tag = expense.imageUri
            } catch (e: Exception) {
                ivExpensePhoto.setImageResource(R.drawable.ic_placeholder_image)
                Log.e("UpdateExpense", "Failed to load image URI", e)
            }
        } else {
            ivExpensePhoto.setImageResource(R.drawable.ic_placeholder_image)
        }

        // Always observe categories
        expenseViewModel.categoryNames.observe(viewLifecycleOwner) { categoryList ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter

            if (::expense.isInitialized) {
                val position = categoryList.indexOf(expense.category)
                if (position >= 0) {
                    spinnerCategory.setSelection(position)
                }
            }
        }

        val userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("user_id", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            (requireActivity() as MainActivity).loadFragment(com.example.prog7313_part2.ui.user.LoginFragment())
            return
        }

        // Always call this to populate spinner
        expenseViewModel.loadCategories(userId)
    }

    private fun showDatePickerDialog() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.MONTH, selectedMonth)
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            tvDate.text = format.format(calendar.time)
        }, year, month, day).show()
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun updateExpenseDetails() {
        val updatedAmount = etAmount.text.toString().toDoubleOrNull()
        val updatedDescription = etDescription.text.toString()
        val updatedCategory = spinnerCategory.selectedItem.toString()
        val updatedDate = tvDate.text.toString()
        val updatedImageUri = ivExpensePhoto.tag?.toString() ?: ""

        Log.d("UpdateExpense", "Saving updated image URI: $updatedImageUri")

        if (updatedAmount != null && updatedDescription.isNotEmpty()) {
            val updatedExpense = expense.copy(
                amount = updatedAmount,
                description = updatedDescription,
                category = updatedCategory,
                date = updatedDate,
                imageUri = updatedImageUri
            )

            expenseViewModel.updateExpense(updatedExpense)
            Toast.makeText(requireContext(), "Expense Updated!", Toast.LENGTH_SHORT).show()
            activity?.onBackPressed()
        } else {
            Toast.makeText(requireContext(), "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(expense: Expense): UpdateExpenseFragment {
            val fragment = UpdateExpenseFragment()
            val bundle = Bundle()
            bundle.putParcelable("selected_expense", expense)
            fragment.arguments = bundle
            return fragment
        }
    }
}
