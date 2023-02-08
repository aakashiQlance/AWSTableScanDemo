package com.aakash.awstablescandemo

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.NetworkOnMainThreadException
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.aakash.awstablescandemo.model.CommonResponseModel
import com.aakash.awstablescandemo.retrofit.ApiEndPoint
import com.aakash.playstoresubscriptionwithautorenewal.retrofit.RetrofitBuilder
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.options.StorageUploadFileOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.util.FileUriUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mayank.simplecropview.callback.CropCallback
import com.mayank.simplecropview.callback.LoadCallback
import com.mayank.simplecropview.callback.SaveCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    var currentImageName = ""
    private var mFrameRect: RectF? = null
    private var mSourceUri: Uri? = null
    private val KEY_FRAME_RECT = "FrameRect"
    private val KEY_SOURCE_URI = "SourceUri"

    private val mLoadCallback: LoadCallback = object : LoadCallback {
        override fun onSuccess() {
//            selectImageCrop.setVisibility(View.VISIBLE);
//            mCropView.setVisibility(GONE);
            val values = IntArray(2)
            cropImageView.getLocationOnScreen(values)
            Log.d("X & Y", cropImageView.width.toString() + " " + cropImageView.height)
            btnGenerateTable.visibility = View.VISIBLE

            //  mCropView.crop(mSourceUri).execute(mCropCallback);
        }

        override fun onError(e: Throwable) {}
    }


    private val mSaveCallback: SaveCallback = object : SaveCallback {
        override fun onSuccess(outputUri: Uri) {
            //startResultActivity(outputUri);
            cropImageView.visibility = View.GONE
            btnGenerateTable.visibility = View.GONE
            try {
                val filePath: String? = FileUriUtils.getRealPath(this@MainActivity, outputUri)

                val selectedImage = File(filePath!!).toString()

                uploadFile_AWS(selectedImage) {
                    if (it) {
                        CoroutineScope(IO).launch {
                            delay(10000)

                            getTableData { str ->
                                CoroutineScope(Main).launch {
                                    progressBar.visibility = View.GONE
                                }
                                if (str != null) {
                                    val jsonObject = JSONArray(str)
                                    val rowArray = ArrayList<ArrayList<String>>()


                                    for (i in 0 until jsonObject.length()) {
                                        val value = jsonObject.get(i).toString()
                                        val eachRowArray = JSONArray(value)
                                        val eachArrayElement = ArrayList<String>()
                                        for (j in 0 until eachRowArray.length()) {
                                            eachArrayElement.add(eachRowArray.get(j).toString())
                                        }
                                        rowArray.add(eachArrayElement)
                                    }


                                    Log.i("RowData", rowArray.toString())

                                    val intent = Intent(this@MainActivity,
                                        TableDataActivity::class.java)
                                    val gson = Gson()
                                    val modal = ArrayModal()
                                    modal.array = rowArray
                                    val obj = gson.toJson(modal)
                                    intent.putExtra("TABLEDATA", obj)
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun onError(e: Throwable) {}
    }

    private val mCompressFormat = CompressFormat.JPEG
    private val mCropCallback: CropCallback = object : CropCallback {
        override fun onSuccess(cropped: Bitmap) {
            cropImageView.save(cropped)
                .compressFormat(mCompressFormat)
                .execute(createSaveUri(), mSaveCallback)
        }

        override fun onError(e: Throwable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        if (savedInstanceState != null) {
            // restore data
            mFrameRect =
                savedInstanceState.getParcelable(KEY_FRAME_RECT)
            mSourceUri =
                savedInstanceState.getParcelable(KEY_SOURCE_URI)
        }




        btnScanImage.setOnClickListener {

            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            ) {

                ImagePicker.with(this@MainActivity)
                    .compress(1024)
                    .start()
                progressBar.visibility = View.VISIBLE

            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    100
                )
            }

        }


        btnGenerateTable.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            cropImageView.crop(mSourceUri).execute(mCropCallback)
        }

    }

    private fun createNewUri(context: Context, format: CompressFormat?): Uri? {
        val currentTimeMillis = System.currentTimeMillis()
        val today = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        val title = dateFormat.format(today)
        val dirPath = getDirPath()
        val fileName = "scv" + title + "." + getMimeType(format)
        val path = "$dirPath/$fileName"
        val file = File(path)
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, title)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + getMimeType(format))
        values.put(MediaStore.Images.Media.DATA, path)
        val time = currentTimeMillis / 1000
        values.put(MediaStore.MediaColumns.DATE_ADDED, time)
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time)
        if (file.exists()) {
            values.put(MediaStore.Images.Media.SIZE, file.length())
        }
        val resolver = context.contentResolver
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    private fun getMimeType(format: CompressFormat?): String {
        return when (format) {
            CompressFormat.JPEG -> "jpeg"
            CompressFormat.PNG -> "png"
            else -> {
                "jpg"
            }
        }
    }

    private fun getDirPath(): String {
        var dirPath = ""
        var imageDir: File? = null
        val extStorageDir = Environment.getExternalStorageDirectory()
        if (extStorageDir.canWrite()) {
            imageDir = File(extStorageDir.path + "/simplecropview")
        }
        if (imageDir != null) {
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }
            if (imageDir.canWrite()) {
                dirPath = imageDir.path
            }
        }
        return dirPath
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {

            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
            //getImages(true, 1)

        }
    }


    private fun getTableData(
        function: (
            String?,
        ) -> Unit,
    ) {
        val retrofit = RetrofitBuilder.getRetrofitClient(this, "https://devapi.studelicious.com/")
        val apiService = retrofit.create(ApiEndPoint::class.java)

        val jsonObject = JsonObject()
        jsonObject.addProperty("table_name", currentImageName)

        val refreshTokenAPI: Call<CommonResponseModel> = apiService.getTableData(jsonObject)

        refreshTokenAPI.enqueue(object : Callback<CommonResponseModel> {
            override fun onResponse(
                call: Call<CommonResponseModel>,
                response: Response<CommonResponseModel>,
            ) {
                if (response.code() == 200 && response.body() != null) {

                    function(response.body()!!.data[0].tableValue)


                } else {
                    CoroutineScope(IO).launch {
                        delay(5000)
                        getTableData { str ->
                            CoroutineScope(Main).launch {
                                progressBar.visibility = View.GONE
                            }
                            val jsonObject = JSONArray(str)
                            val rowArray = ArrayList<ArrayList<String>>()


                            for (i in 0 until jsonObject.length()) {
                                val value = jsonObject.get(i).toString()
                                val eachRowArray = JSONArray(value)
                                val eachArrayElement = ArrayList<String>()
                                for (j in 0 until eachRowArray.length()) {
                                    eachArrayElement.add(eachRowArray.get(j).toString())
                                }
                                rowArray.add(eachArrayElement)
                            }


                            Log.i("RowData", rowArray.toString())

                            val intent = Intent(this@MainActivity,
                                TableDataActivity::class.java)
                            val gson = Gson()
                            val modal = ArrayModal()
                            modal.array = rowArray
                            val obj = gson.toJson(modal)
                            intent.putExtra("TABLEDATA", obj)
                            startActivity(intent)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<CommonResponseModel>, t: Throwable) {
                CoroutineScope(Main).launch {
                    progressBar.visibility = View.GONE
                }
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            progressBar.visibility = View.GONE
            val imageUri = data.data
            cropImageView.visibility = View.VISIBLE
            cropImageView.load(imageUri)
                .initialFrameRect(mFrameRect)
                .useThumbnail(true)
                .execute(mLoadCallback)
        }
    }


    private fun uploadFile_AWS(path: String?, function: (Boolean) -> Unit) {
        var attachmentUrl = ""
        val selectedFile = File(path.toString())
        try {
            val options = StorageUploadFileOptions.defaultInstance()

            currentImageName = System.currentTimeMillis().toString()
            Amplify.Storage.uploadFile(
                "$currentImageName.jpg", selectedFile, options, {
                    if (it.fractionCompleted == 1.0) {
                        function(true)
                    }
                    Log.e("MyAmplifyApp", "Fraction completed: ${it.fractionCompleted}")
                },
                {

                },
                {
                    Log.e("MyAmplifyApp", "Upload failed", it)
                }
            )

        } catch (exception: Exception) {
            Log.e("MyAmplifyApp", "Upload failed", exception)
        } catch (e: NetworkOnMainThreadException) {
            Log.e("MyAmplifyApp", "Upload failed$e.message")
        }
    }

    fun createSaveUri(): Uri? {
        return createNewUri(this@MainActivity,
            mCompressFormat)
    }
}