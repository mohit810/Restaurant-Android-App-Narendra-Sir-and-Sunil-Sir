package com.example.restaurantapp

import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.restaurantapp.bottomnav.Account
import com.example.restaurantapp.bottomnav.Cart
import com.example.restaurantapp.bottomnav.Search
import com.example.restaurantapp.bottomnav.menu.Menu
import com.example.restaurantapp.cartdatabase.CartDataSource
import com.example.restaurantapp.cartdatabase.CartDatabase
import com.example.restaurantapp.cartdatabase.LocalCartDataSource
import com.example.restaurantapp.eventbus.CountCartEvent
import com.example.restaurantapp.eventbus.HideCartCount
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_base.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class Base : AppCompatActivity() {

    private lateinit var cartDataSource: CartDataSource
    lateinit var  bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        cartDataSource = LocalCartDataSource(CartDatabase.getDbInstance(this).cartDAO())
        bottom_navigation_view.setItemIconTintList(null)//enabling color in bottom nav
        //bottom nav
         bottomNavigation = bottom_navigation_view
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        if (savedInstanceState == null) {
            val fragment = Menu()
            supportFragmentManager.beginTransaction().replace(R.id.frag_container, fragment, fragment.javaClass.getSimpleName())
                .commit()
            countCartItem()
        }
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token
                val eheh = "hjvjhvhj"

            })
        /*countCartItem()*/
    }

    override fun onResume() {
        super.onResume()
        countCartItem()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.home_png -> {
                val fragment = Menu()
                supportFragmentManager.beginTransaction().replace(R.id.frag_container, fragment, fragment.javaClass.getSimpleName())
                    .commit()
                countCartItem()//added this so that if user deletes the cart items on navigation number gets updated
                return@OnNavigationItemSelectedListener true
            }
            R.id.search_png -> {
                val fragment = Search()
                supportFragmentManager.beginTransaction().replace(R.id.frag_container, fragment, fragment.javaClass.getSimpleName())
                    .commit()
                countCartItem()
                return@OnNavigationItemSelectedListener true
            }
            R.id.cart_png -> {
                val fragment = Cart()
                supportFragmentManager.beginTransaction().replace(R.id.frag_container, fragment, fragment.javaClass.getSimpleName())
                    .commit()
                bottomNavigation.removeBadge(R.id.cart_png)
                return@OnNavigationItemSelectedListener true
            }
            R.id.account_png -> {
                val fragment = Account()
                supportFragmentManager.beginTransaction().replace(R.id.frag_container, fragment, fragment.javaClass.getSimpleName())
                    .commit()
                countCartItem()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onHideCartCountEvent(event: HideCartCount){
        if (event.isHidden)
        {
            bottomNavigation.removeBadge(R.id.cart_png)
        }
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onCartCountEvent(event: CountCartEvent){
        if (event.isSuccess)
        {
            countCartItem()
        }
    }

   private  fun countCartItem() {
        cartDataSource.countItemInCart("")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: SingleObserver<Int> {
                override fun onSuccess(t: Int) {
                    val badge = bottomNavigation.getOrCreateBadge(R.id.cart_png)
                    if (t >0){
                       badge.isVisible = true
                       badge.backgroundColor = resources.getColor(R.color.colorWhite, this@Base.getTheme())
                       badge.badgeTextColor = resources.getColor(R.color.colorSoothingRed, this@Base.getTheme())
// An icon only badge will be displayed unless a number is set:
                       badge.number = t
                   } else{
                        badge.isVisible = false
                   }

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                //    Toast.makeText(this@Base,e.message,Toast.LENGTH_LONG).show()
                }

            })
    }

}
