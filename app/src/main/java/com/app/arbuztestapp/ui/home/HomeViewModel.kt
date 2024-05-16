package com.app.arbuztestapp.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.app.arbuztestapp.Database.ProductDao
import com.app.arbuztestapp.Database.ProductDatabase
import com.app.arbuztestapp.Database.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val productDao: ProductDao
    val productList: LiveData<List<ProductEntity>>

    init {
        val database = ProductDatabase.getDatabase(application)
        productDao = database.productDao()
        productList = productDao.getAllProducts()
        if (!getProductsAddedStatus()) {
            insertProductList()
            saveProductsAddedStatus(true)
        }
    }

    fun refreshProductList() {
        if (!getProductsAddedStatus()) {
            insertProductList()
            saveProductsAddedStatus(true)
        }
    }



    private fun insertProductList() {
        viewModelScope.launch(Dispatchers.IO) {
            val products = mutableListOf<ProductEntity>()
            products.add(ProductEntity(name = "Potatoes", price = 10.toDouble(), description =
            "Sweet potatoes", storageConditions = "From 0°C to 4°C ", country = "Kazakhstan", imageUri = ""))
            products.add(ProductEntity(name = "Apples", price = 800.toDouble(), description =
            "Red delicious apples", storageConditions = "From 0°C to 4°C", country = "Kazakhstan", imageUri = ""))
            products.add(ProductEntity(name = "Carrots", price = 600.toDouble(), description =
            "Fresh carrots", storageConditions = "From 0°C to 4°C", country = "Kazakhstan", imageUri = ""))
            products.add(ProductEntity(name = "Tomatoes", price = 1500.toDouble(), description =
            "Cherry tomatoes", storageConditions = "From 0°C to 4°C", country = "Kazakhstan", imageUri = ""))
            products.add(ProductEntity(name = "Chicken", price = 2500.toDouble(), description =
            "Fresh chicken breast", storageConditions = "From 0°C to 4°C", country = "Kazakhstan", imageUri = ""))
            products.add(ProductEntity(name = "Salmon", price = 3500.toDouble(), description =
            "Atlantic salmon fillet", storageConditions = "From -20°C", country = "Kazakhstan", imageUri = ""))
            products.add(ProductEntity(name = "Milk", price = 400.toDouble(), description =
            "Fresh cow's milk", storageConditions = "From 0°C to 4°C", country = "Kazakhstan", imageUri = ""))
            products.add(ProductEntity(name = "Spinach", price = 900.toDouble(), description =
            "Fresh spinach leaves", storageConditions = "From 0°C to 4°C", country = "Kazakhstan", imageUri = ""))
            products.add(ProductEntity(name = "Eggs", price = 500.toDouble(), description =
            "Free-range chicken eggs", storageConditions = "From 0°C to 4°C", country = "Kazakhstan", imageUri = ""))
            products.add(ProductEntity(name = "Bananas", price = 1100.toDouble(), description =
            "Ripe bananas", storageConditions = "From 13°C to 15°C", country = "Kazakhstan", imageUri = ""))


            productDao.insertProducts(products)
        }
    }

    private fun getProductsAddedStatus(): Boolean {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            "PRODUCTS_ADDED_STATUS",
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getBoolean("PRODUCTS_ADDED", false)
    }

    private fun saveProductsAddedStatus(status: Boolean) {
        val sharedPreferencesEditor = getApplication<Application>().getSharedPreferences(
            "PRODUCTS_ADDED_STATUS",
            Context.MODE_PRIVATE
        ).edit()
        sharedPreferencesEditor.putBoolean("PRODUCTS_ADDED", status)
        sharedPreferencesEditor.apply()
    }
}
