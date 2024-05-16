package com.app.arbuztestapp.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import com.app.arbuztestapp.Database.CartItemEntity
import com.app.arbuztestapp.Database.ProductDatabase
import com.app.arbuztestapp.Database.ProductEntity
import com.app.arbuztestapp.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ProductAdapter(private val context: Context, private var productList: List<ProductEntity>) :
    BaseAdapter(), Filterable {

    private var filteredProductList: List<ProductEntity> = productList
    private val API_KEY = "43914006-87ed0f8ac6975440722032c8f"

    fun setProductList(products: List<ProductEntity>) {
        productList = products
        filteredProductList = products
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return filteredProductList.size
    }

    override fun getItem(position: Int): Any {
        return filteredProductList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false)
            holder = ViewHolder()
            holder.imageView = view.findViewById(R.id.imageView)
            holder.nameTextView = view.findViewById(R.id.nameTextView)
            holder.priceTextView = view.findViewById(R.id.priceTextView)
            holder.addBtn = view.findViewById(R.id.addButton)
            holder.removeBtn = view.findViewById(R.id.removeButton)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val product = filteredProductList[position]

        if (product.imageUri != null && product.imageUri.isNotBlank()) {
            Log.d("ImageUrl", product.imageUri)
            Picasso.get().load(product.imageUri).into(holder.imageView)
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val imageUrl = getImageUrl(API_KEY, product.name)
                withContext(Dispatchers.Main) {
                    imageUrl?.let {
                        product.imageUri = it
                        ProductDatabase.getDatabase(context).productDao().updateProduct(product)
                        Picasso.get().load(it).into(holder.imageView)
                    }
                }
            }
        }

        holder.nameTextView.text = product.name

        val cartItemDao = ProductDatabase.getDatabase(context).cartItemDao()
        CoroutineScope(Dispatchers.IO).launch {
            val cartItem = cartItemDao.getCartItem(product.id)
            withContext(Dispatchers.Main) {
                if (cartItem != null) {
                    holder.priceTextView.text = cartItem.quantity.toString()
                } else {
                    holder.priceTextView.text = product.price.toString()
                }
            }
        }

        holder.addBtn.setOnClickListener {
            val product = filteredProductList[position]
            val cartItemDao = ProductDatabase.getDatabase(context).cartItemDao()

            CoroutineScope(Dispatchers.IO).launch {
                var cartItem = cartItemDao.getCartItem(product.id)

                if (cartItem != null) {
                    cartItem.quantity++
                    cartItemDao.updateCartItem(cartItem)
                } else {
                    cartItem = CartItemEntity(productId = product.id, quantity = 1)
                    cartItemDao.insertCartItem(cartItem)
                }

                withContext(Dispatchers.Main) {
                    notifyDataSetChanged()
                }
            }
        }

        holder.removeBtn.setOnClickListener {
            val product = filteredProductList[position]
            val cartItemDao = ProductDatabase.getDatabase(context).cartItemDao()

            CoroutineScope(Dispatchers.IO).launch {
                val cartItem = cartItemDao.getCartItem(product.id)

                if (cartItem != null) {
                    if (cartItem.quantity > 1) {
                        cartItem.quantity--
                        cartItemDao.updateCartItem(cartItem)
                    } else {
                        cartItemDao.deleteCartItem(cartItem)
                    }

                    withContext(Dispatchers.Main) {
                        notifyDataSetChanged()
                    }
                }
            }
        }

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()
                filteredProductList = if (charString.isEmpty()) {
                    productList
                } else {
                    val filteredList = mutableListOf<ProductEntity>()
                    for (product in productList) {
                        if (product.name.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(product)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredProductList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                filteredProductList = results?.values as List<ProductEntity>
                notifyDataSetChanged()
            }
        }
    }

    private class ViewHolder {
        lateinit var imageView: ImageView
        lateinit var nameTextView: TextView
        lateinit var priceTextView: TextView
        lateinit var addBtn: Button;
        lateinit var removeBtn: Button;
    }

    private suspend fun getImageUrl(apiKey: String, query: String): String? {
        val url = URL("https://pixabay.com/api/?key=$apiKey&q=$query&image_type=photo")
        var imageUrl: String? = null

        try {
            val connection: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            connection.connect()

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(response)

            if (jsonObject.getInt("totalHits") > 0) {
                val hits = jsonObject.getJSONArray("hits")
                val randomIndex = (0 until hits.length()).random()
                val hitObject = hits.getJSONObject(randomIndex)
                imageUrl = hitObject.getString("webformatURL")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return imageUrl
    }
}
