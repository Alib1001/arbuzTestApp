package com.app.arbuztestapp.ui.cart
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.app.arbuztestapp.Adapters.CartItemAdapter
import com.app.arbuztestapp.Database.ProductDatabase
import com.app.arbuztestapp.databinding.FragmentCartBinding

class CartFragment : Fragment(), CartItemAdapter.OnCartItemChangeListener {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartViewModel: CartViewModel
    private lateinit var productDatabase: ProductDatabase
    private lateinit var cartItemAdapter: CartItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val root: View = binding.root

        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        productDatabase = ProductDatabase.getDatabase(requireContext())

        cartItemAdapter = CartItemAdapter(requireContext(), listOf(), this) // Pass listener

        val cartListView: ListView = binding.cartListView
        cartListView.adapter = cartItemAdapter

        cartViewModel.allCartItems.observe(viewLifecycleOwner, Observer { cartItems ->
            cartItems?.let {
                cartItemAdapter.setCartItemList(it)
                val totalCost = cartItemAdapter.calculateTotalCost()
                onCartItemChanged(totalCost)
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCartItemChanged(totalCost: Double) {
        binding.checkoutButton.text = "Checkout ($totalCost)"
    }
}

