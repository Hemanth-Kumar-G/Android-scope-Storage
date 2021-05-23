package com.hemanth.androidstorage.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hemanth.androidstorage.databinding.ActivityMainBinding
import com.hemanth.androidstorage.ui.main.adapter.InternalStoragePhotoAdapter
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private val internalStoragePhotoAdapter: InternalStoragePhotoAdapter by lazy {
        InternalStoragePhotoAdapter {
            val isDeletionSuccessful = viewModel.deletePhotoFromInternalStorage(it.name, this)
            if (isDeletionSuccessful) {
                loadPhotosFromInternalStorageIntoRecyclerView()
                Toast.makeText(this, "Photo successfully deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            val isPrivate = binding.switchPrivate.isChecked
            if (isPrivate) {

                val isSavedSuccessfully = viewModel.savePhotoToInternalStorage(
                    UUID.randomUUID().toString(), it, this@MainActivity
                )

                if (isSavedSuccessfully) {
                    loadPhotosFromInternalStorageIntoRecyclerView()
                    Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTakePhoto.setOnClickListener { takePhoto.launch() }
        setupInternalStorageRecyclerView()
        loadPhotosFromInternalStorageIntoRecyclerView()
    }

    private fun loadPhotosFromInternalStorageIntoRecyclerView() = lifecycleScope.launch {
        val photos = viewModel.loadPhotosFromInternalStorage(this@MainActivity)
        internalStoragePhotoAdapter.submitList(photos)
    }


    private fun setupInternalStorageRecyclerView() = binding.rvPrivatePhotos.apply {
        adapter = internalStoragePhotoAdapter
        layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
    }
}
