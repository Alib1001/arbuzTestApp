package com.app.arbuztestapp.ui.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.app.arbuztestapp.Database.CartItemEntity
import com.app.arbuztestapp.Database.ProductDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val productDatabase: ProductDatabase = ProductDatabase.getDatabase(application)

    val allCartItems: LiveData<List<CartItemEntity>> = productDatabase.cartItemDao().getAllCartItems()

    fun removeCartItem(cartItem: CartItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            productDatabase.cartItemDao().deleteCartItem(cartItem)
        }
    }

    fun updateCartItem(cartItem: CartItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            productDatabase.cartItemDao().updateCartItem(cartItem)
        }
    }
}

