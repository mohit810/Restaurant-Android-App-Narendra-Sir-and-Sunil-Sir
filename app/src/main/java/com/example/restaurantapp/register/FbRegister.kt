package com.example.restaurantapp.register

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.restaurantapp.HomeActivity
import com.example.restaurantapp.LoadingDialog
import com.example.restaurantapp.R
import com.example.restaurantapp.dataprovider.dataclass.Details
import com.example.restaurantapp.dataprovider.sharedpref.Prefs
import com.example.retrofit.ApiClient
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_fb_register.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.regex.Pattern


class FbRegister : AppCompatActivity() {
    lateinit var uids:String
    lateinit var imageuri: Uri
    private val GALLERY_REQUEST_CODE = 1234
    val progressBar = LoadingDialog()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //status bar
        val window = this.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        // finally change the color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorWhite)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
        setContentView(R.layout.activity_fb_register)
        fbRegisterErrorMsg.visibility = View.GONE
        uids = intent.extras!!.getString("uids")!!
        progressBar.show(this,"Please Wait...")

        Handler().postDelayed({

//Dismiss progress bar after 4 seconds

            progressBar.dialog.dismiss()

        }, 2000)
        loadingApiData()
        imageuri = saveImageToInternalStorage(R.drawable.user)
        fbRegisterProfilePic.setOnClickListener(){
            pickFromGallery()
        }
        fbRegister_Add_dp_btn.setOnClickListener (){
            pickFromGallery()
        }
        fbRegisterSubmitbtn.setOnClickListener(){
            SubmissionFbRegister()
        }

    }
    // Method to save an image to internal storage
    private fun saveImageToInternalStorage(drawableId:Int):Uri{
        // Get the image from drawable resource as drawable object
        val drawable = ContextCompat.getDrawable(applicationContext,drawableId)

        // Get the bitmap from drawable object
        val bitmap = (drawable as BitmapDrawable).bitmap
        // Get the context wrapper instance
        val wrapper = ContextWrapper(applicationContext)
        // Initializing a new file
        // The bellow line return a directory in internal storage
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
        // Create a file to save the image
        file = File(file, "profilepic.png")
        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)
            // Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            // Flush the stream
            stream.flush()
            // Close stream
            stream.close()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }
        // Return the saved image uri
        return Uri.parse(file.absolutePath)
    }
    private fun setImage(uri: Uri){
        Picasso.get()
            .load(uri)
            .transform(CropCircleTransformation())
            .resize(400, 400)
            .into(fbRegisterProfilePic)
        Picasso.get()
            .load(uri)
            .into(object : com.squareup.picasso.Target{
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    try {
                        val wrapper = ContextWrapper(applicationContext)
                        val mydir = wrapper.getDir("images", Context.MODE_PRIVATE)
                        if (!mydir.exists()) {
                            mydir.mkdirs()
                        }
                        val fileUri = File(mydir, "profilepic.png")
                        val outputStream = FileOutputStream(fileUri)
                        bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.flush()
                        outputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    Toast.makeText(applicationContext, "Image Downloaded", Toast.LENGTH_LONG)
                        .show()
                }
            })
    }
    private fun launchImageCrop(uri: Uri){
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(400, 400)
            .setCropShape(CropImageView.CropShape.RECTANGLE) // default is rectangle
            .start(this)
    }
    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }
    fun SubmissionFbRegister(){
        if (fbRegisterDisplayNameInput.text.toString() == "" || fbRegisterEmailInput.text.toString() == "" || fbRegisterPswdInput.text.toString() == "" || fbRegisterMobileNumberInput.text.toString() == "" || fbRegisterAddressInput.text.toString() == "") {
            fbRegisterErrorMsg.setText("Please don't leave any field empty")
            fbRegisterErrorMsg.visibility = View.VISIBLE
            vibratePhone()

        }else  if (!isEmailValid(fbRegisterEmailInput.text.toString())){
            fbRegisterErrorMsg.setText("Please Enter correct Email ID")
            fbRegisterErrorMsg.visibility = View.VISIBLE
            vibratePhone()
        }else if(!isPhoneNumber(fbRegisterMobileNumberInput.text.toString().replace("+91", ""))){
            fbRegisterErrorMsg.setText("Please Enter correct Mobile")
            fbRegisterErrorMsg.visibility = View.VISIBLE
            vibratePhone()
        }
        else {
           sendDataToServer(fbRegisterDisplayNameInput.text.toString(),fbRegisterEmailInput.text.toString(),fbRegisterPswdInput.text.toString(),fbRegisterAddressInput.text.toString(),fbRegisterMobileNumberInput.text.toString().replace("+91", ""))
        }
    }
    private fun sendDataToServer(username:String,email:String,pwd:String,addrss:String,mobile:String) {
        val userName : RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"),username)
        val emailIdd = RequestBody.create(MediaType.parse("multipart/form-data"),email)
        val pswd =RequestBody.create(MediaType.parse("multipart/form-data"),pwd)
        val userInputAddress =RequestBody.create(MediaType.parse("multipart/form-data"),addrss)
        val numb =RequestBody.create(MediaType.parse("multipart/form-data"),mobile)

        val wrapper = ContextWrapper(applicationContext)
        val mydir = wrapper.getDir("images", Context.MODE_PRIVATE)
        //get file
        val file = File(mydir, "profilepic.png")
        val requestFile: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
// MultipartBody.Part is used to send also the actual file name
        val imgg: MultipartBody.Part = MultipartBody.Part.createFormData("profilePic", file.name, requestFile)
        val call: Call<List<Details>> = ApiClient.getClient.uploadData(userName,emailIdd,pswd,numb,userInputAddress,imgg)
        call.enqueue(object : retrofit2.Callback<List<Details>>{
            override fun onFailure(call: Call<List<Details>>, t: Throwable) {
                var d= t.stackTrace
                Toast.makeText(this@FbRegister,d.toString() + " Sorry but Our Server is Down.",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<List<Details>>, response: Response<List<Details>>) {
                if (response.code() == 201) {
                    Toast.makeText(this@FbRegister,"Created Successfully.",Toast.LENGTH_LONG).show()
                    Prefs.customerEmailId = email
                    Prefs.customerPwd = pwd
                    Prefs.customerAddress = addrss
                    Prefs.customerMobileNumber =  mobile.toLong()
                    Prefs.customerName = username
                    //lets get the uri for our prefs
                    Prefs.profilePicUri = file.toString()
                    Prefs.checker = "on"
                    val i = Intent(this@FbRegister, HomeActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Will clear out your activity history stack till now
                    startActivity(i)
                } else {
                    Toast.makeText(this@FbRegister,"You already have a account",Toast.LENGTH_LONG).show()
                }

            }

        })
    }
    fun isPhoneNumber(mobile:String) : Boolean{
        val REG = "^([\\-\\s]?)?[0]?(91)?[789]\\d{9}\$"
        val PATTERN: Pattern = Pattern.compile(REG)
        return PATTERN.matcher(mobile).find()}
    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    fun vibratePhone(){
        val v: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //deprecated in API 26
            @Suppress("DEPRECATION")
            v.vibrate(100)
        }
    }
    private fun loadingApiData() {
        // Checking the Access Token.
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphLoginRequest(AccessToken.getCurrentAccessToken())
            var token = AccessToken.getCurrentAccessToken()
            // If already login in then show the Toast.
            Toast.makeText(this, "Already logged in", Toast.LENGTH_SHORT).show()

            Picasso.get()
                .load(
                    "https://graph.facebook.com/v7.0/" + uids
                        .toString() + "/picture?height=500&type=normal"
                ) //extract as User instance method
                .transform(CropCircleTransformation())
                .resize(400, 400)
                .into(fbRegisterProfilePic)
            /* val profilePic:ProfilePictureView = fbimageView fb's api
             profilePic.setProfileId(uids)*/
            Picasso.get()
                .load(
                    "https://graph.facebook.com/v7.0/" + uids
                        .toString() + "/picture?height=500&type=normal"
                ) //extract as User instance method
                .transform(CropCircleTransformation())
                .resize(400, 400)
                .into(object : com.squareup.picasso.Target{
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        try {
                            val wrapper = ContextWrapper(applicationContext)
                            val mydir = wrapper.getDir("images", Context.MODE_PRIVATE)
                            if (!mydir.exists()) {
                                mydir.mkdirs()
                            }
                            val fileUri = File(mydir, "profilepic.png")
                            Prefs.profilePicUri = fileUri.toString()
                            val outputStream = FileOutputStream(fileUri)
                            bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.flush()
                            outputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        Toast.makeText(applicationContext, "Image Downloaded", Toast.LENGTH_LONG)
                            .show()
                    }
                })
            GraphLoginRequest(token)
        } else {

            // If not login in then show the Toast.
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to access Facebook User Data.
    fun GraphLoginRequest(accessToken: AccessToken?) {
        val graphRequest = GraphRequest.newMeRequest(
            accessToken
        ) { jsonObject, graphResponse ->
            try {
                fbRegisterDisplayNameInput.setText( jsonObject.getString("name"))
                fbRegisterEmailInput.setText( jsonObject.getString("email"))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val bundle = Bundle()
        bundle.putString(
            "fields",
            "id,name,link,email,gender,last_name,first_name,locale,timezone,updated_time,verified"
        )
        graphRequest.parameters = bundle
        graphRequest.executeAsync()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //choosing and Cropping profile pic
        when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    }
                }
                else{
                    Toast.makeText(this,"Image selection error: Couldn't select that image from memory.",Toast.LENGTH_SHORT).show()
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    setImage(result.uri)
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(this,result.getError().toString(),Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}
