package com.magda.noteapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class User (
    var userId: String?=null,
    var firstName :String? = null,
    var lastName :String? = null,
    var emailAddress: String?=null,
):Parcelable
