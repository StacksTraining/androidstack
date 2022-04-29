package com.magda.noteapp.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.magda.noteapp.R
import com.magda.noteapp.businesslogic.NoteViewModel
import com.magda.noteapp.databinding.ActivityAddNoteBinding
import com.magda.noteapp.model.Note
import com.magda.noteapp.model.User
import com.magda.noteapp.utils.Constants
import com.magda.noteapp.utils.Status
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddNoteActivity : AppCompatActivity() {
    private val TAG = "Add Note Activity"
    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var user: User
    private var photoPath= ""
    private lateinit var cameraUri: Uri
    private lateinit var noteViewModel: NoteViewModel
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResults ->
        if (activityResults.resultCode == RESULT_OK) {
            photoPath = cameraUri.toString()
            Glide.with(this).load(photoPath).into(binding.ivImageHolder)
        }
    }
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResults ->
        if (activityResults.resultCode == RESULT_OK) {
            val imageUri = activityResults.data?.data
            Log.d(TAG, "oda: ${activityResults.data?.data},... ${activityResults.data} ")
            if (imageUri != null) {
                photoPath = imageUri.toString()
                Glide.with(this).load(photoPath).into(binding.ivImageHolder)
            }
        } else Toast.makeText(this, "No picture was selected", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getParcelableExtra(Constants.USER)!!

        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)



        binding.btSave.setOnClickListener {
            validateUserInputs()
        }

        binding.ivCamera.setOnClickListener { checkPermissions() }
        addNoteLiveData()
    }

    private fun addNoteLiveData() {
        noteViewModel.publicCreateNoteLiveData.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    //progressbar
                    Log.i(TAG, "addNoteLiveData: loading")
                }
                Status.SUCCESS -> {
                    Log.i(TAG, "addNoteLiveData: Success")
                    toHomeActivity()
                }
                Status.ERROR -> {
                    Toast.makeText(this, it.error, Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "addNoteLiveData: failed")
                }
            }
        }
    }

    private fun addPhotoDialog() {
        AlertDialog.Builder(this)
            .setTitle("Upload using:")
            .setItems(R.array.media_options) { _, which ->
                if (which == 0) {
                    toGallery()
                } else if (which == 1) {
                    toCamera()
                }
            }.create()
            .show()
    }

    private fun toCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //store it within our local device
        val file = createStorageFile()
        cameraUri = FileProvider.getUriForFile(
            this@AddNoteActivity, "com.magda.noteapp.fileprovider",
            file
        )


        Log.d(TAG, "toCamera: $cameraUri")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun toStoreImageInFireStore(title: String, noteBody: String) {
        //check if the user is logged in
        //instantiate Firebase Storage
        //instantiate the storage reference
        //getFileExtensions of our files (.jpeg)
        // add progress listener, add progress bar
        binding.progressBar.visibility = View.VISIBLE
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
            val fileRef = mStorageRef.child("notes_images")
                .child("${System.currentTimeMillis()}.${getFileExtensions()}")
            Log.d(TAG, "toStoreImageInFireStore: $photoPath")
            fileRef.putFile(photoPath.toUri()).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@AddNoteActivity,
                        "Upload was successful",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressBar.visibility = View.GONE

                    saveNote(title, noteBody)

                }
            }.addOnFailureListener { e ->
                Toast.makeText(this@AddNoteActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileExtensions(): String? {
        val resolver = this.contentResolver
        val mimeType = MimeTypeMap.getSingleton()
        return mimeType.getExtensionFromMimeType(resolver.getType(photoPath.toUri()))
    }

    private fun createStorageFile(): File {
        val timestamp = SimpleDateFormat("yyyymmdd_hhmmss", Locale.getDefault()).format(Date())
        val imageName = "/jpeg_$timestamp"
        return File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!, imageName)
    }

    private fun checkPermissions() {
        //ask permission
        //camera pop up
        //store the image
        //embed to our image view
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (ActivityCompat.checkSelfPermission(
                this@AddNoteActivity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) ==
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@AddNoteActivity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) ==
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@AddNoteActivity,
                android.Manifest.permission.CAMERA
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            addPhotoDialog()

        } else {
            ActivityCompat.requestPermissions(this@AddNoteActivity, permissions, 100)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addPhotoDialog()
            } else Toast.makeText(
                this@AddNoteActivity,
                "The app will not work without the permissions", Toast.LENGTH_LONG
            ).show()
        } else Toast.makeText(
            this@AddNoteActivity,
            "Permissions denied", Toast.LENGTH_LONG
        ).show()
    }


    private fun toGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        galleryActivityResultLauncher.launch(Intent.createChooser(intent, "Select picture:"))
    }

    private fun validateUserInputs() {
        val title = binding.addNoteTitle.editText?.text.toString().trim()
        val noteBody = binding.addNoteDetail.editText?.text.toString().trim()

        if (title.isNotEmpty()) {

            if (noteBody.isNotEmpty()) {

                if (photoPath.isNotEmpty()) {
                    toStoreImageInFireStore(title, noteBody)
                } else saveNote(title, noteBody)

            } else {
                binding.addNoteDetail.error = "Cannot be empty"
            }
        } else {
            binding.addNoteTitle.error = "Cannot be empty"
        }
    }

    private fun saveNote(title: String, noteBody: String) {
        val uId = user.userId!!
        val currentDate =
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(System.currentTimeMillis())
        val currentTime =
            SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(System.currentTimeMillis())
        val currentDateTime = "$currentDate $currentTime"
        val noteId = noteViewModel.getNoteId()
        val note = Note(noteId, uId, title, currentDateTime, noteBody, photoPath)
        noteViewModel.createNote(note)
        Log.d(TAG, "saveNote: $note")
    }

    private fun toHomeActivity() {
        val intent = Intent(this@AddNoteActivity, HomeActivity::class.java)
        intent.putExtra(Constants.USER, user)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}