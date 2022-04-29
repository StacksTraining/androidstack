package com.magda.noteapp.utils

sealed class Results <out T:Any>{
    //2 classes : when it is successful and when it has failed: need to have data
    //same as enum except it can be more than one instance
    data class Success <out T:Any>(val data:T):Results<T>()
    //Results<Notes>(), Results<String>()
    data class Error (val error: String):Results<Nothing>()
}
