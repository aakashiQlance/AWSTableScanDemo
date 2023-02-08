package com.aakash.awstablescandemo.retrofit


import com.aakash.awstablescandemo.model.CommonResponseModel
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface ApiEndPoint {

    @POST("api/testing_purpose_data")
    fun getTableData(
        @Body jsonObject: JsonObject
    ):Call<CommonResponseModel>

}