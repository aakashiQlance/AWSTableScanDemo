package com.aakash.awstablescandemo.model

import com.google.gson.annotations.SerializedName

data class CommonResponseModel(
        @SerializedName("status")
        var status: Int,
        @SerializedName("message")
        var message: String? = null,
        @SerializedName("data")
        var data: ArrayList<TableData>
)
