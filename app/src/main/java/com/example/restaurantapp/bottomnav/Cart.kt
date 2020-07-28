package com.example.restaurantapp.bottomnav


import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.example.restaurantapp.Base
import com.example.restaurantapp.R
import com.example.restaurantapp.bottomnav.adapters.CartAdapter
import com.example.restaurantapp.bottomnav.viewmodel.CartViewModel
import com.example.restaurantapp.callbacks.IMyButtonCallback
import com.example.restaurantapp.cartdatabase.CartDataSource
import com.example.restaurantapp.cartdatabase.CartDatabase
import com.example.restaurantapp.cartdatabase.CartItemEntity
import com.example.restaurantapp.cartdatabase.LocalCartDataSource
import com.example.restaurantapp.common.MySwipeHelper
import com.example.restaurantapp.dataprovider.dataclass.DishesDetails
import com.example.restaurantapp.dataprovider.sharedpref.Prefs
import com.example.restaurantapp.eventbus.CountCartEvent
import com.example.restaurantapp.eventbus.HideCartCount
import com.example.restaurantapp.eventbus.UpdateItemInCart
import com.example.restaurantapp.login.Login
import com.example.restaurantapp.register.Register
import com.example.retrofit.ApiClient
import com.facebook.appevents.codeless.internal.EventBinding
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_cart.*
import kotlinx.android.synthetic.main.fragment_cart.view.*
import kotlinx.android.synthetic.main.fragment_cart.view.empty_cart_animation
import kotlinx.android.synthetic.main.not_logged_in.*
import kotlinx.android.synthetic.main.not_logged_in.view.*
import kotlinx.android.synthetic.main.not_logged_in.view.not_loged_in_ErrorMsg
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern


/**
 * A simple [Fragment] subclass.
 */
class Cart : Fragment() {
    private var cartDataSourceCartFragment:CartDataSource?=null
    private var compositeDisposableCartFragment=CompositeDisposable()
    private var recyclerViewState: Parcelable?= null
private lateinit var cartViewModel:CartViewModel
    var txt_empty_cart:TextView? = null
    var txt_total_price:TextView? = null
    var groupplaceholder:CardView? = null
    var recycler_cart:RecyclerView? = null
    var adapter:CartAdapter?= null
    var helperList:List<CartItemEntity>? = null
    private var one:Int = 0
    private var two:Int? = null
    private var three:Int? = null
    private var four:Int? = null
    private var onequant:Int = 0
    private var twoquant:Int?= null
    private var threequant:Int?= null
    private var fourquant:Int? = null
    override fun onResume() {
        super.onResume()
        calculateTotalPrice()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EventBus.getDefault().postSticky(HideCartCount(true))
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        //now init the initcartdatasource of the view model
        cartViewModel.initcartDataSourceCartViewModel(context!!)
        // Inflate the layout for this fragment
        val root= inflater.inflate(R.layout.fragment_cart, container, false)
        initViews(root)
        cartViewModel.getmutableLiveDataCartItem().observe(this, Observer {
            if (it == null || it.isEmpty())
            {
                recycler_cart!!.visibility =View.GONE
                groupplaceholder!!.visibility = View.GONE
                txt_empty_cart!!.visibility =View.VISIBLE
                empty_cart_animation!!.visibility =View.VISIBLE
            }else{
                recycler_cart!!.visibility =View.VISIBLE
                groupplaceholder!!.visibility = View.VISIBLE
                txt_empty_cart!!.visibility =View.GONE
                empty_cart_animation!!.visibility =View.GONE
                adapter = CartAdapter(context!!,it)
                recycler_cart!!.adapter = adapter
                helperList = it
            }
        })


        return root
    }

    private fun initViews(root: View) {
        cartDataSourceCartFragment = LocalCartDataSource(CartDatabase.getDbInstance(context!!).cartDAO())
        recycler_cart = root.cart_recyclerView
        recycler_cart!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recycler_cart!!.layoutManager = layoutManager
        recycler_cart!!.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))
        val swipe = object:MySwipeHelper(context!!,recycler_cart!!,200){
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(MyButton(context!!,
                "Delete",
                30,
                0,
                Color.parseColor("#FF3C30"),
                object:IMyButtonCallback{
                    override fun onClick(pos: Int) {
                        val deleteItem = adapter!!.getItemAtPosition(pos)
                        cartDataSourceCartFragment!!.deleteCart(deleteItem)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object: SingleObserver<Int>{
                                override fun onSuccess(t: Int) {
                                    adapter!!.notifyItemRemoved(pos)
                                    sumPrice()
                                    EventBus.getDefault().postSticky(CountCartEvent(true))

                                    Toast.makeText(context,"yay!! Item Deleted",Toast.LENGTH_LONG).show()
                                }
                                override fun onSubscribe(d: Disposable) {

                                }

                                override fun onError(e: Throwable) {
                                    Toast.makeText(context,"eh!!" + e.message,Toast.LENGTH_LONG).show()
                                }

                            })
                    }

                }))
            }

        }
        txt_empty_cart = root.empty_cart_msg
        txt_total_price = root.cart_totalPrice
        groupplaceholder = root.group_placeHolder_cart_fragment

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = activity!!.window
// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
// finally change the color
        window.statusBarColor = ContextCompat.getColor(activity!!, R.color.colorWhite)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
        cart_placeOrder_btn.setOnClickListener() {
            if (Prefs.checker.equals("on")){
                placeOrders()
            }
            else
            {
                val mDialogView = LayoutInflater.from(context).inflate(R.layout.not_logged_in,null)
                val mBuilder = AlertDialog.Builder(context)
                    .setView(mDialogView)

                val mAlertDialog = mBuilder.show()
                mDialogView.not_logged_in_continue_btn.setOnClickListener(){
                    if(mDialogView.not_loged_in_addressInput.text.toString() == "" || mDialogView.not_loged_in_phoneInput.text.toString() == ""){
                        mDialogView.not_loged_in_ErrorMsg.visibility = View.VISIBLE
                    } else if(!isPhoneNumber(mDialogView.not_loged_in_phoneInput.text.toString().replace("+91", ""))) {
                        mDialogView.not_loged_in_ErrorMsg.setText("Please Enter correct Mobile")
                        mDialogView.not_loged_in_ErrorMsg.visibility = View.VISIBLE
                    }
                    else
                    {
                        mAlertDialog.dismiss()
                        Prefs.customerAddress = mDialogView.not_loged_in_addressInput.text.toString()
                        Prefs.customerMobileNumber = mDialogView.not_loged_in_phoneInput.text.toString().replace("+91", "").toLong()
                        placeOrders()
                    }
                }
                mDialogView.not_loged_in_loginbtn.setOnClickListener(){
                    mAlertDialog.dismiss()
                    startActivity(Intent(context,Login::class.java))
                }
                mDialogView.not_loged_in_signupbtn.setOnClickListener(){
                    mAlertDialog.dismiss()
                    startActivity(Intent(context,Register::class.java))
                }
            }
        }
    }

    private fun placeOrders() {
        when(helperList!!.size){
            1 -> {
                one = helperList!!.first().dishId.toInt()
                onequant = helperList!!.first().dishQuantity.toInt()
                 }
            2 -> {
                one = helperList!!.first().dishId.toInt()
                onequant = helperList!!.first().dishQuantity.toInt()
                two = helperList!!.get(1).dishId.toInt()
                twoquant = helperList!!.get(1).dishQuantity.toInt()
                }
            3 -> {
                one = helperList!!.first().dishId.toInt()
                onequant = helperList!!.first().dishQuantity.toInt()
                two = helperList!!.get(1).dishId.toInt()
                twoquant = helperList!!.get(1).dishQuantity.toInt()
                three = helperList!!.get(2).dishId.toInt()
                threequant = helperList!!.get(2).dishQuantity.toInt()
                }
            4 -> {
                one = helperList!!.first().dishId.toInt()
                onequant = helperList!!.first().dishQuantity.toInt()
                two = helperList!!.get(1).dishId.toInt()
                twoquant = helperList!!.get(1).dishQuantity.toInt()
                three = helperList!!.get(2).dishId.toInt()
                threequant = helperList!!.get(2).dishQuantity.toInt()
                four = helperList!!.get(3).dishId.toInt()
                fourquant = helperList!!.get(3).dishQuantity.toInt()
                }
        }
        val call: Call<List<DishesDetails>> = ApiClient.getClient.placingOrder(
            one,
            two,
            three,
            four,
            Prefs.customerMobileNumber.toString(),
            onequant,
            twoquant,
            threequant,
            fourquant
        )
        call.enqueue(object :  Callback<List<DishesDetails>> {
            override fun onFailure(call: Call<List<DishesDetails>>, t: Throwable) {
                var d= t.stackTrace
                Toast.makeText(context!!,d.toString() + " Sorry but Our Server is Down.",Toast.LENGTH_LONG).show()
                cleanCart()

            }

            override fun onResponse(call:  Call<List<DishesDetails>>, response: Response<List<DishesDetails>>) {
                //     val apiResponse = ModelAPIResponse(response.body() as LinkedTreeMap<String, String>)
                if (response.code() == 201) {
                   Toast.makeText(context,"Check Your Order Status in Account Tab",Toast.LENGTH_LONG).show()
                    successCleanCart()
                } else {
                    Toast.makeText(context,"YO",Toast.LENGTH_LONG).show()
                    cleanCart()
                }

            }
        })
    }
    private fun successCleanCart(){
        cartViewModel.deletemutableLiveDataCartItem().observe(this, Observer {
            if (it == null || it.isEmpty())
            {
                recycler_cart!!.visibility =View.GONE
                groupplaceholder!!.visibility = View.GONE
                txt_empty_cart!!.visibility =View.VISIBLE
                txt_empty_cart!!.setText("your order will be delivered soon.")
                empty_cart_animation!!.visibility =View.VISIBLE
                empty_cart_animation!!.setAnimation(R.raw.orderplaced)
                empty_cart_animation!!.setRepeatCount(LottieDrawable.INFINITE)
                empty_cart_animation!!.playAnimation()

            }else{
                recycler_cart!!.visibility =View.VISIBLE
                groupplaceholder!!.visibility = View.VISIBLE
                txt_empty_cart!!.visibility =View.GONE
                empty_cart_animation!!.visibility =View.GONE
                adapter = CartAdapter(context!!,it)
                recycler_cart!!.adapter = adapter
                helperList = it
            }
        })
    }
private fun cleanCart(){
    cartViewModel.deletemutableLiveDataCartItem().observe(this, Observer {
        if (it == null || it.isEmpty())
        {
            recycler_cart!!.visibility =View.GONE
            groupplaceholder!!.visibility = View.GONE
            txt_empty_cart!!.visibility =View.VISIBLE
            empty_cart_animation!!.visibility =View.VISIBLE
        }else{
            recycler_cart!!.visibility =View.VISIBLE
            groupplaceholder!!.visibility = View.VISIBLE
            txt_empty_cart!!.visibility =View.GONE
            empty_cart_animation!!.visibility =View.GONE
            adapter = CartAdapter(context!!,it)
            recycler_cart!!.adapter = adapter
            helperList = it
        }
    })
}
    fun isPhoneNumber(mobile:String) : Boolean{
        val REG = "^([\\-\\s]?)?[0]?(91)?[789]\\d{9}\$"
        val PATTERN: Pattern = Pattern.compile(REG)
        return PATTERN.matcher(mobile).find()}

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        cartViewModel!!.onStop()
        compositeDisposableCartFragment.clear()
        EventBus.getDefault().postSticky(HideCartCount(false))
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onUpdateInCartItems(event: UpdateItemInCart){
        if (event.cartItemEventBus!=null){
            recyclerViewState = recycler_cart!!.layoutManager!!.onSaveInstanceState()
            cartDataSourceCartFragment!!.updateCart(event.cartItemEventBus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: SingleObserver<Int>{
                    override fun onSuccess(t: Int) {
                        calculateTotalPrice()
                        recycler_cart!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onError(e: Throwable) {
                      //  Toast.makeText(context,"[update Cart]"+e.message,Toast.LENGTH_LONG).show()
                    }

                })
        }
    }

    private fun calculateTotalPrice() {
        cartDataSourceCartFragment!!.sumPrice("")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: SingleObserver<Double>{
                override fun onSuccess(tprice: Double) {
                    txt_total_price!!.text = "Total: " + "₹ " + tprice.toString()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                  //  Toast.makeText(context,"[Sum Cart]"+e.message,Toast.LENGTH_LONG).show()
                }

            })
    }
    private fun sumPrice(){
        cartDataSourceCartFragment!!.sumPrice("")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: SingleObserver<Double>{
                override fun onSuccess(tprice: Double) {
                    txt_total_price!!.text = "Total: " + "₹ " + tprice.toString()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                 //   Toast.makeText(context,"[Sum Cart]"+e.message,Toast.LENGTH_LONG).show()
                }

            })
    }
}
