package com.example.prog7313_part2.ui.expenses

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.prog7313_part2.data.local.entities.Expense
import com.example.prog7313_part2.databinding.FragmentEnterExpenseBinding
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import android.util.Log
import com.example.prog7313_part2.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.Locale

class EnterExpenseFragment : Fragment() {
    private var _binding: FragmentEnterExpenseBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EnterExpenseViewModel
    private var selectedDate: Date? = null
    private var photoPath: String? = null
    private var isSaving = false

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val file = saveBitmapToFile(bitmap)
            photoPath = file.absolutePath
            binding.ivExpensePhoto.setImageBitmap(bitmap)
            binding.ivExpensePhoto.isClickable = true
            Log.d("EnterExpenseFragment", "Photo saved at: $photoPath")
            Toast.makeText(context, "Photo captured", Toast.LENGTH_SHORT).show()
        } else {
            Log.w("EnterExpenseFragment", "Photo capture failed")
            Toast.makeText(context, "Failed to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val file = saveBitmapToFile(bitmap)
                    photoPath = file.absolutePath
                    binding.ivExpensePhoto.setImageBitmap(bitmap)
                    binding.ivExpensePhoto.isClickable = true
                    Log.d("EnterExpenseFragment", "Image selected from gallery: $photoPath")
                    Toast.makeText(context, "Image selected", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("EnterExpenseFragment", "Failed to decode image from URI")
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("EnterExpenseFragment", "Error loading image: ${e.message}")
                Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w("EnterExpenseFragment", "No image selected")
        }
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Log.d("EnterExpenseFragment", "Camera permission granted")
            takePicture.launch(null)
        } else {
            Log.w("EnterExpenseFragment", "Camera permission denied")
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEnterExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("EnterExpenseFragment", "Expense entry fragment initialized")

        viewModel = ViewModelProvider(this)[EnterExpenseViewModel::class.java]

        // Setup hardcoded categories
        viewModel.categoryNames.observe(viewLifecycleOwner) { categories ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }

        val userId = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("user_id", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            (requireActivity() as MainActivity).loadFragment(com.example.prog7313_part2.ui.user.LoginFragment())
            return
        }

        // Trigger category loading
        viewModel.loadCategories(userId)

        binding.tvDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.time
                    binding.tvDate.text = String.format("%02d/%02d/%d", day, month + 1, year)
                    Log.d("EnterExpenseFragment", "Date selected: $selectedDate")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.seekBarAmount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val amount = progress * 10.0
                binding.etAmount.setText(NumberFormat.getCurrencyInstance().format(amount))
                Log.d("EnterExpenseFragment", "Amount updated: $amount")
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.btnUploadPhoto.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Image Source")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                Log.i("EnterExpenseFragment", "Camera permission already granted, launching camera")
                                takePicture.launch(null)
                            } else {
                                Log.i("EnterExpenseFragment", "Requesting camera permission")
                                requestCameraPermission.launch(Manifest.permission.CAMERA)
                            }
                        }
                        1 -> {
                            Log.i("EnterExpenseFragment", "Launching gallery picker")
                            pickImage.launch("image/*")
                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    Log.d("EnterExpenseFragment", "Image source selection cancelled")
                }
                .show()
        }

        binding.ivExpensePhoto.setOnClickListener {
            if (photoPath != null) {
                Log.d("EnterExpenseFragment", "Opening full-screen image preview: $photoPath")
                val fragment = ImagePreviewFragment.newInstance(photoPath!!)
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Log.w("EnterExpenseFragment", "No image to preview")
                Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSaveExpense.setOnClickListener {
            if (isSaving) {
                Log.d("EnterExpenseFragment", "Save in progress, ignoring click")
                return@setOnClickListener
            }

            val description = binding.etDescription.text.toString()
            val amountText = binding.etAmount.text.toString()
            val category = binding.spinnerCategory.selectedItem?.toString()

            if (description.isEmpty() || amountText.isEmpty() || selectedDate == null || category == null) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                Log.w("EnterExpenseFragment", "Save attempt with incomplete fields")
                return@setOnClickListener
            }

            val amount = amountText.replace("[^0-9.]".toRegex(), "").toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
                Log.w("EnterExpenseFragment", "Invalid amount format")
                return@setOnClickListener
            }

            val formattedDate = selectedDate?.let { it1 ->
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                    it1
                )
            }

            val expense = Expense(
                date = formattedDate.toString(),
                description = description,
                category = category,
                amount = amount,
                imageUri = photoPath,
                userId = userId
            )

            isSaving = true
            binding.btnSaveExpense.isEnabled = false
            viewModel.saveExpense(expense, userId)
            Toast.makeText(context, "Expense saved!", Toast.LENGTH_SHORT).show()
            Log.i("EnterExpenseFragment", "Expense saved: $expense")

            binding.etDescription.text?.clear()
            binding.etAmount.text?.clear()
            binding.tvDate.text = "Select Date"
            binding.ivExpensePhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            selectedDate = null
            photoPath = null

            isSaving = false
            binding.btnSaveExpense.isEnabled = true
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(requireContext().filesDir, "expense_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}