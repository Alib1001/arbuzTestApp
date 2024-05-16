package com.app.arbuztestapp.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart")
    fun getAllCartItems(): LiveData<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity)

    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItemEntity)

    @Query("DELETE FROM cart")
    fun deleteAllCartItems()

    @Query("SELECT * FROM cart WHERE productId = :productId")
    suspend fun getCartItem(productId: Int): CartItemEntity?

    @Query("UPDATE cart SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Int, quantity: Int)

    @Query("UPDATE cart SET totalPrice = :totalPrice WHERE id = :id")
    suspend fun updateTotalPrice(id: Int, totalPrice: Double)
}

