package com.app.arbuztestapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.arbuztestapp.Adapters.ProductAdapter
import com.app.arbuztestapp.R

class HomeFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: ProductAdapter
    private lateinit var searchView: SearchView
    private lateinit var gridView: GridView
    private var lastQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        searchView = root.findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(this)
        searchView.setQuery(lastQuery, false)

        gridView = root.findViewById(R.id.gridView)

        homeViewModel.refreshProductList()

        homeViewModel.productList.observe(viewLifecycleOwner, { productList ->
            if (::adapter.isInitialized) {
                adapter.setProductList(productList)
                if (lastQuery.isNotEmpty()) {
                    adapter.filter.filter(lastQuery)
                }
            }
        })

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductAdapter(requireContext(), ArrayList())
        gridView.adapter = adapter
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        lastQuery = newText ?: ""
        if (::adapter.isInitialized) {
            adapter.filter.filter(newText)
        }
        return true
    }
}
