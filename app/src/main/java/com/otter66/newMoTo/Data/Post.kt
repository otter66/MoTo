package com.otter66.newMoTo.Data

import android.os.Parcel
//속도는 빠르다고 하는데... 다음에 한 번 써봐야징
import android.os.Parcelable
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class Post(
    var id: String? = null,
    val title: String? = null,
    val twoLineDescription: String? = null,
    var mainImage: String? = null,
    var images: ArrayList<String>? = null,
    val description: String? = null,
    val update: String? = null,
    val improvement: String? = null,
    var linkNames: ArrayList<String>? = null,
    var linkAddresses: ArrayList<String>? = null,
    var publisher: String? = null,
    val createdDate: Date? = null
) : Serializable
