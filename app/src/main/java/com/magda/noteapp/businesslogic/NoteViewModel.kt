package com.magda.noteapp.businesslogic

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magda.noteapp.model.Note
import com.magda.noteapp.repository.NotesRepository
import com.magda.noteapp.utils.NetworkResponse
import com.magda.noteapp.utils.Results
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel: ViewModel() {
    private val TAG = "NoteViewModel"
    /**
     * View model
     * coroutine scopes -> UI /worker thread
     * lifecycle observer
     * Communicate with the repo and view model
     * livedata
     * Communicate with the view model and view
     */

    private val notesRepository = NotesRepository()
    private val createNoteLiveData :MutableLiveData<NetworkResponse<Boolean>> = MutableLiveData<NetworkResponse<Boolean>>()
    val publicCreateNoteLiveData:MutableLiveData<NetworkResponse<Boolean>> get()= createNoteLiveData

   fun createNote(note: Note) {
    viewModelScope.launch(Dispatchers.IO){
        notesRepository.createNote(note){
            Log.d(TAG, "createNote: $note")
            createNoteLiveData.postValue(NetworkResponse.loading())
            when(it){
                is Results.Success ->{
                    createNoteLiveData.postValue(NetworkResponse.success(true,null))
                    Log.d(TAG, "createNote: Success")
                }
                is Results.Error ->{
                    createNoteLiveData.postValue(NetworkResponse.error(it.error))
                }
            }
        }
    }
    }

    fun getNoteId():String{
        return notesRepository.getNoteId()
    }

}