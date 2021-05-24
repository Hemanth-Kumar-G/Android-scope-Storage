package com.hemanth.androidstorage.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hemanth.androidstorage.databinding.ActivityMainBinding
import com.hemanth.androidstorage.ui.main.adapter.InternalStoragePhotoAdapter
import com.hemanth.androidstorage.ui.main.adapter.SharedPhotoAdapter
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionGranted =
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted =
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted
        }

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

    private val externalStoragePhotoAdapter: SharedPhotoAdapter by lazy {
        SharedPhotoAdapter {

        }
    }

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            val isPrivate = binding.switchPrivate.isChecked
            val isSavedSuccessfully = when {
                isPrivate -> viewModel.savePhotoToInternalStorage(
                    UUID.randomUUID().toString(), it, this
                )
                writePermissionGranted -> viewModel.savePhotoExternalStorage(
                    this, UUID.randomUUID().toString(), it
                )
                else -> false
            }

            if (isPrivate) {
                loadPhotosFromInternalStorageIntoRecyclerView()
            }
            if (isSavedSuccessfully) {
                Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTakePhoto.setOnClickListener { takePhoto.launch() }
        updateOrRequestPermissions()
        setupInternalStorageRecyclerView()
        loadPhotosFromInternalStorageIntoRecyclerView()
    }

    fun updateOrRequestPermissions() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionRequest = mutableListOf<String>()

        if (!hasReadPermission) permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (!hasWritePermission) permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionRequest.toTypedArray())
        }

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
