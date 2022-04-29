package com.magda.noteapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Note(
    var noteId: String? = null,
    var uuid: String? = null,
    var title: String? = null,
    var timeStamp: String? = null,
    var body: String? = null,
    var imagePath:String?=null,
): Parcelable