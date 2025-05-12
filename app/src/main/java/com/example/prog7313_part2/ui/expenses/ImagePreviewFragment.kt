package com.example.prog7313_part2.ui.expenses

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.prog7313_part2.R
import android.util.Log

class ImagePreviewFragment : Fragment() {
    private var photoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            photoPath = it.getString(ARG_PHOTO_PATH)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = view.findViewById<ImageView>(R.id.fullScreenImageView)
        val closeButton = view.findViewById<View>(R.id.btnClosePreview)

        try {
            // Load and display the image
            photoPath?.let {
                val bitmap = BitmapFactory.decodeFile(it)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    Log.d("ImagePreviewFragment", "Image loaded successfully")
                } else {
                    Toast.makeText(context, "Could not load image", Toast.LENGTH_SHORT).show()
                    Log.e("ImagePreviewFragment", "Failed to decode bitmap from path: $it")
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ImagePreviewFragment", "Error loading image", e)
        }

        // Close button functionality
        closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
            Log.d("ImagePreviewFragment", "Image preview closed")
        }
    }

    companion object {
        private const val ARG_PHOTO_PATH = "photo_path"

        @JvmStatic
        fun newInstance(photoPath: String) =
            ImagePreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PHOTO_PATH, photoPath)
                }
            }
    }
}