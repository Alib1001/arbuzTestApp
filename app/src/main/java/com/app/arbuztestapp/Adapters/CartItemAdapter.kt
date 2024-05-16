package com.app.arbuztestapp.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.app.arbuztestapp.Database.CartItemEntity
import com.app.arbuztestapp.Database.ProductDatabase
import com.app.arbuztestapp.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking


class CartItemAdapter(
    private val context: Context,
    private var cartItemList: List<CartItemEntity>,
    private val listener: OnCartItemChangeListener
) : BaseAdapter() {

    interface OnCartItemChangeListener {
        fun onCartItemChanged(totalCost: Double)
    }

    private val productDatabase: ProductDatabase = ProductDatabase.getDatabase(context)

    override fun getCount(): Int {
        return cartItemList.size
    }

    override fun getItem(position: Int): Any {
        return cartItemList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false)
            holder = ViewHolder()
            holder.cartItemImageView = view.findViewById(R.id.cartItemImageView)
            holder.cartItemNameTextView = view.findViewById(R.id.cartItemNameTextView)
            holder.cartItemPriceTextView = view.findViewById(R.id.cartItemPriceTextView)
            holder.cartItemTotalPriceTextView = view.findViewById(R.id.cartItemTotalPriceTextView)
            holder.cartItemQuantityTextView = view.findViewById(R.id.cartItemQuantityTextView)
            holder.cartItemRemoveButton = view.findViewById(R.id.cartItemRemoveButton)
            holder.cartItemIncreaseButton = view.findViewById(R.id.cartItemIncreaseButton)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val cartItem = cartItemList[position]

        CoroutineScope(Dispatchers.IO).launch {
            val product = productDatabase.productDao().getProduct(cartItem.productId)
            withContext(Dispatchers.Main) {
                product?.let {
                    holder.cartItemNameTextView.text = it.name
                    holder.cartItemPriceTextView.text = it.price.toString()
                    holder.cartItemTotalPriceTextView.text = (it.price * cartItem.quantity).toString()
                    holder.cartItemQuantityTextView.text = cartItem.quantity.toString()
                    Picasso.get().load(it.imageUri).into(holder.cartItemImageView)
                }
            }
        }

        holder.cartItemRemoveButton.setOnClickListener {
            if (cartItem.quantity > 0) {
                cartItem.quantity--
                CoroutineScope(Dispatchers.IO).launch {
                    productDatabase.cartItemDao().updateCartItem(cartItem)
                    listener.onCartItemChanged(calculateTotalCost())
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    productDatabase.cartItemDao().deleteCartItem(cartItem)
                    listener.onCartItemChanged(calculateTotalCost())
                }
            }
        }

        holder.cartItemIncreaseButton.setOnClickListener {
            cartItem.quantity++
            CoroutineScope(Dispatchers.IO).launch {
                productDatabase.cartItemDao().updateCartItem(cartItem)
                listener.onCartItemChanged(calculateTotalCost())
            }
        }

        return view
    }

    fun calculateTotalCost(): Double {
        var totalCost = 0.0
        cartItemList.forEach { cartItem ->
            val product = runBlocking {
                productDatabase.productDao().getProduct(cartItem.productId)
            }
            product?.let {
                totalCost += it.price * cartItem.quantity
            }
        }
        return totalCost
    }

    fun setCartItemList(cartItemList: List<CartItemEntity>) {
        this.cartItemList = cartItemList
        notifyDataSetChanged()
    }

    private class ViewHolder {
        lateinit var cartItemImageView: ImageView
        lateinit var cartItemNameTextView: TextView
        lateinit var cartItemPriceTextView: TextView
        lateinit var cartItemTotalPriceTextView: TextView
        lateinit var cartItemQuantityTextView: TextView
        lateinit var cartItemRemoveButton: Button
        lateinit var cartItemIncreaseButton: Button
    }
}
