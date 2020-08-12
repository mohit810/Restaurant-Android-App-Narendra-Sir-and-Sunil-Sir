package com.example.restaurantapp.login

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.restaurantapp.HomeActivity
import com.example.restaurantapp.R
import com.example.restaurantapp.dataprovider.dataclass.Details
import com.example.restaurantapp.dataprovider.sharedpref.Prefs
import com.example.restaurantapp.register.Register
import com.example.retrofit.ApiClient
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //status bar
        val window = this.window
// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
// finally change the color
        window.statusBarColor = ContextCompat.getColor(this, R.color.login_notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE)
        }
        setContentView(R.layout.activity_login)
        login_id_pwd_error_msg.visibility = View.GONE
        login_error_img.visibility = View.GONE
        login_cardview_signup_btn.setOnClickListener(){
            startActivity(Intent(this, Register::class.java))
        }
        login_cardview_login_btn.setOnClickListener(){
           var emailinput:String = login_cardview_email_input.text.toString() //"iamohit810@gmail.com"
           var passwordinput:String = login_cardview_pwd_input.text.toString() //"vvvcc"
           loginProcess(emailinput,passwordinput)
        }

    }

    public fun loginProcess(emailinput: String, passwordinput: String){
        val emailIdd = RequestBody.create(MediaType.parse("multipart/form-data"),emailinput)
        val pswd = RequestBody.create(MediaType.parse("multipart/form-data"),passwordinput)
        val call: Call<List<Details>> = ApiClient.getClient.getUser(emailIdd,pswd)
        call.enqueue(object : Callback<List<Details>> {
            override fun onFailure(call: Call<List<Details>>, t: Throwable) {
                var d= t.stackTrace
                Toast.makeText(this@Login, " Sorry but Our Server is Down.",Toast.LENGTH_LONG).show()

            }

            override fun onResponse(call: Call<List<Details>>, response: Response<List<Details>>) {
                //     val apiResponse = ModelAPIResponse(response.body() as LinkedTreeMap<String, String>)
                if(response.body()== null){
                    login_id_pwd_error_msg.visibility = View.VISIBLE
                    login_error_img.visibility = View.VISIBLE
                    vibratePhone()
                }else {
                    if (response.code() == 200) {
                        var userDetailsApi = response.body()
                        store(
                            userDetailsApi!!.first().name,
                            userDetailsApi.first().email,
                            userDetailsApi.first().password,
                            userDetailsApi.first().address,
                            userDetailsApi.first().mobile,
                            userDetailsApi.first().image
                        )
                    } else {
                        login_id_pwd_error_msg.visibility = View.VISIBLE
                        login_error_img.visibility = View.VISIBLE
                        vibratePhone()
                    }
                }
            }
        })
    }
    fun store(
        name: String,
        email: String,
        passwd: String,
        fullAddress: String,
        mobile: String,
        image: String
    ) {
        Prefs.customerName = name
        Prefs.customerEmailId = email
        Prefs.customerPwd=passwd
        Prefs.customerAddress = fullAddress
        Prefs.customerMobileNumber = mobile.toLong()
        storeImage(image)
        Prefs.checker = "on"
        val i = Intent(this, HomeActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// Will clear out your activity history stack till now
        startActivity(i)
    }

    private fun storeImage(image: String) {
        Picasso.get()
            .load(image)
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
}
