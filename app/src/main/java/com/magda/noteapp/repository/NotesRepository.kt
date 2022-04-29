package com.magda.noteapp.repository

import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.magda.noteapp.model.Note
import com.magda.noteapp.utils.Constants
import com.magda.noteapp.utils.Results
import java.text.SimpleDateFormat
import java.util.*

class NotesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val noteReference = db.collection(Constants.NOTES)
    private val TAG = "NotesRepository"

    /**
     * MVC, MVP, MVVM
     * Model, View, Controller => Laravel (php)
     * Model, View, Presentation (Business logic)
     * Model, View, View Model => android
     * standard architecture
     * Model => in between the db and the app
     * View=> users are able to interact with
     * View model => business logic =>firebase
     * Viewmodel-> model and view
     * Repo
     * fetching or creating data
     * based on the model
     * Notes repository can communicate with the view model
     * return the data, now to our UI, lifecycle observer to get the info we need that is on our view model
     *
     */
   fun createNote(note: Note, result: (Results<Boolean>) -> Unit) {
        noteReference.document(note.noteId!!).set(note).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                result(Results.Success(true))
                Log.d(TAG, "createNote: added")
            } else result(Results.Error("Note addition was unsuccessful"))
        }.addOnFailureListener { e ->
            result(Results.Error(e.localizedMessage!!))
        }
    }

    fun getNoteId():String{
        return noteReference.document().id
    }
}