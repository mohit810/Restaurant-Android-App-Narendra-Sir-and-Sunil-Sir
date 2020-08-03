package com.example.restaurantapp.bottomnav.menu

import android.content.Intent
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.restaurantapp.R
import com.example.restaurantapp.bottomnav.menu.tabs.MainCourse
import com.example.restaurantapp.bottomnav.menu.tabs.Starters
import com.example.restaurantapp.bottomnav.menu.tabs.SweetDish
import com.example.restaurantapp.bottomnav.menu.view.ViewPagerAdapter
import com.example.restaurantapp.cartdatabase.CartDataSource
import com.example.restaurantapp.cartdatabase.CartDatabase
import com.example.restaurantapp.cartdatabase.LocalCartDataSource
import com.example.restaurantapp.dataprovider.sharedpref.Prefs
import com.example.restaurantapp.eventbus.CountCartEvent
import com.example.restaurantapp.register.Register
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_menu.*
import kotlinx.android.synthetic.main.fragment_menu.view.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Suppress("DEPRECATION")
class Menu : Fragment() {
    private var tickToCross: AnimatedVectorDrawable? =null
    private  var crossToTick:AnimatedVectorDrawable? = null
    private var isTick = true
    var tabLayout: TabLayout? = null
   lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       /* var getaddress= getActivity()!!.getIntent().getExtras()!!.getString("Data")*/
        if (Prefs.gpsAddress.equals(null)){
            gpsaddresstxt.setText("Please give us premission to show data")
        }
        else{
            var getaddress = Prefs.gpsAddress
            gpsaddresstxt.setText(getaddress)
        }

        // bottom_navigation_view.setItemIconTintList(null)//enabling color in bottom nav
        val mToolbar = menutoolbar
     //   setSupportActionBar(mToolbar)
        nest_scrollview.isFillViewport = true //enables nested scroll view
        tabLayout = view.tabLayoutmenu
        viewPager = view.viewpager
        viewPager.offscreenPageLimit = 3
        setupViewPager(viewPager!!)
        tabLayout!!.setupWithViewPager(viewPager)
        val mAppBarLayout: AppBarLayout = app_bar as AppBarLayout
        mAppBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var isShow = false
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange()
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    // showOption(R.id.action_info)
                } else if (isShow) {
                    isShow = false
                    // hideOption(R.id.action_info)
                }
            }
        })
        //animation for the fab action button commented as not req.
/*       val fabTickCross: FloatingActionButton = fabTickCross
        tickToCross = getDrawable(activity!!.baseContext,R.drawable.avd_tick2cross) as AnimatedVectorDrawable
        crossToTick = getDrawable(activity!!.baseContext,R.drawable.avd_cross2tick) as AnimatedVectorDrawable
        fabTickCross.setOnClickListener(){
            when (it.id) {
                R.id.fabTickCross -> {
                    val drawable = (if (isTick) tickToCross else crossToTick)!!
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        drawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                            override fun onAnimationStart(drawable: Drawable) {
                                super.onAnimationStart(drawable)
                            }
                            override fun onAnimationEnd(drawable: Drawable) {
                                super.onAnimationEnd(drawable)
                            }
                        })
                    }
                    val fab = it as FloatingActionButton
                    fab.setImageDrawable(drawable)
                    drawable.start()
                    if (isTick){
                        Toast.makeText(activity!!.baseContext,"yo",Toast.LENGTH_LONG).show()
                    }
                    else if(!isTick){
                        Toast.makeText(activity!!.baseContext,"bye",Toast.LENGTH_LONG).show()
                    }
                    isTick = !isTick
                }
            }
        }*/
        registerbtn.setOnClickListener(){
            startActivity(Intent(activity!!.baseContext, Register::class.java))
        }
        //notification area color
        val window = activity!!.window
// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
// finally change the color
        window.statusBarColor = ContextCompat.getColor(activity!!, R.color.colorSoothingRed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE)
        }
    }


    // Add Fragments to Tabs
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(getChildFragmentManager())
        adapter.addFragment(Starters(), "Starters")
        adapter.addFragment(MainCourse(), "Main Course")
        adapter.addFragment(SweetDish(), "Sweet Dish")
        viewPager.adapter = adapter
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }



/*
    private fun startLocationUpdate() {
        locationViewModel.getLocationData().observe(viewLifecycleOwner, Observer {
            var geocoder = Geocoder(activity!!.baseContext, Locale.getDefault())

            var addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
           // var address = addresses[0].getAddressLine(0)
            var city = addresses[0].locality
            var local = addresses[0].subLocality
            gpsaddresstxt.text = local + ", " + city
        })
    }
*/
}