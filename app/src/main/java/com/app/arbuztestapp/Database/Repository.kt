package com.app.arbuztestapp.Database

import androidx.lifecycle.LiveData

class Repository(private val productDao: ProductDao) {

    val allProducts: LiveData<List<ProductEntity>> = productDao.getAllProducts()

    suspend fun insertProduct(product: ProductEntity) {
        productDao.insertProduct(product)
    }
    suspend fun getAllProducts(): LiveData<List<ProductEntity>> {
        return allProducts
    }

}

