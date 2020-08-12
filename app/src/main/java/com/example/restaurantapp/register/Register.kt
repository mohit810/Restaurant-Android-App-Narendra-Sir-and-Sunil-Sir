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
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.twitter.sdk.android.core.*
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_register.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.regex.Pattern

class Register : AppCompatActivity() {
    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]
    private val GALLERY_REQUEST_CODE = 1234
    private lateinit var callbackManager: CallbackManager
    val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    lateinit var imageuri:Uri
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    //This code must be entering before the setContentView to make the twitter login work...
        val mTwitterAuthConfig = TwitterAuthConfig("9OvUwQM7Dj0KNwkUKyTOhnni8",
            "9A4ishtJxFoVcGilCvA8FeV4lK0zhgxwSFbnwVl5fgiALO7cE8")
        val twitterConfig: TwitterConfig = TwitterConfig.Builder(this)
            .twitterAuthConfig(mTwitterAuthConfig)
            .build()
        Twitter.initialize(twitterConfig)
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
        setContentView(R.layout.activity_register)
        configureGoogleSignIn()
        setupUI()
        registerErrorMsg.visibility = View.GONE
        firebaseAuth = FirebaseAuth.getInstance()
        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // [END initialize_auth]

        // [START initialize_fblogin]
        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()

        fblogin()
        twitterlogin()
         imageuri = saveImageToInternalStorage(R.drawable.user)
        userpic.setOnClickListener(){
            pickFromGallery()
        }
        add_dp_btn.setOnClickListener (){
            pickFromGallery()
        }
        registerSignupbtn.setOnClickListener(){
            submissionRegisterData()
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
            .into(userpic)
        Picasso.get()
            .load(uri)
            .into(object : com.squareup.picasso.Target{
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                override fun onBitmapLoaded(bitmap: Bitmap?, from: LoadedFrom?) {
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
    fun submissionRegisterData(){
        if (displaynameinput.text.toString() == "" || emailinput.text.toString() == "" /*|| pswdinput.text.toString() == ""*/ || mobilenumberinput.text.toString() == "" || addressinput.text.toString() == "") {
            registerErrorMsg.setText("Please don't leave any field empty")
            registerErrorMsg.visibility = View.VISIBLE
            vibratePhone()

        }else  if (!isEmailValid(emailinput.text.toString())){
            registerErrorMsg.setText("Please Enter correct Email ID")
            registerErrorMsg.visibility = View.VISIBLE
            vibratePhone()
        }else if(!isPhoneNumber(mobilenumberinput.text.toString().replace("+91", ""))){
            registerErrorMsg.setText("Please Enter correct Mobile")
            registerErrorMsg.visibility = View.VISIBLE
            vibratePhone()
        }
        else {
            val userName = displaynameinput.text.toString()
            val emailId = emailinput.text.toString()
           val pwd = pswdinput.text.toString()
            val customerAddress = addressinput.text.toString()
            val mobileNumber =  mobilenumberinput.text.toString().replace("+91", "")
            sendDataToServer(userName, emailId ,pwd,customerAddress,mobileNumber)
        }
    }

    private fun sendDataToServer(username:String,email:String,pwd:String,addrss:String,mobile:String) {
        val userName :RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"),username)
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
                Toast.makeText(this@Register," Sorry but Our Server is Down.",Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<List<Details>>, response: Response<List<Details>>) {
                if (response.code() == 201) {
                    Toast.makeText(this@Register,"Created Successfully.",Toast.LENGTH_LONG).show()
                    Prefs.customerEmailId = email
                    Prefs.customerPwd = pwd
                    Prefs.customerAddress = addrss
                    Prefs.customerMobileNumber =  mobile.toLong()
                    Prefs.customerName = username
                    //lets get the uri for our prefs
                    Prefs.profilePicUri = file.toString()
                    Prefs.checker = "on"
                    val i = Intent(this@Register, HomeActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Will clear out your activity history stack till now
                    startActivity(i)
                } else {
                    Toast.makeText(this@Register,"You already have a Account with this Phone Number!",Toast.LENGTH_LONG).show()
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

    fun onClickGoogleButton(view: View) {
        if (view == google_btn) {
            signIn()
        }
    }
    fun onClickTwitterButton(view: View) {
        if (view == twitter_btn) {
        login_button.performClick();
         }
    }

    fun onClickFacebookButton(view: View) {
        if (view == fb_btn) {
//            buttonFacebookLogin.performClick();
        }
    }
    fun twitterlogin(){
        login_button.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                Toast.makeText(baseContext, "twitterLogin:success ",
                    Toast.LENGTH_SHORT).show()

                val session = TwitterCore.getInstance().sessionManager.activeSession
                val authToken = session.authToken
                val intent = Intent(this@Register, TwitterRegister::class.java)
                startActivity(intent)
            }
            override fun failure(exception: TwitterException) {
                Toast.makeText(baseContext, "twitterLogin:failure",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun fblogin(){
        buttonFacebookLogin.setPermissions(Arrays.asList("public_profile", "email"))
        buttonFacebookLogin.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Toast.makeText(baseContext, "facebook:onCancel",
                    Toast.LENGTH_SHORT).show()
                // ...
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(baseContext, "facebook:onError $error" ,
                    Toast.LENGTH_SHORT).show()
                // ...
            }
        })
    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        //showProgressBar()

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    var intent = Intent(this, FbRegister::class.java)
                    intent.putExtra("uids",token.userId)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
                //          hideProgressBar()
            }
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
    }

    private fun setupUI() {
        google_button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
        login_button.onActivityResult(requestCode,resultCode,data)
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

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {

                startActivity(Intent(this,GoogleRegister::class.java))
            } else {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

}
