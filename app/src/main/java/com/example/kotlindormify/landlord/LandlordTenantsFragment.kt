package com.example.kotlindormify.landlord

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kotlindormify.R
import com.example.kotlindormify.databinding.LandlordTenantsFragmentBinding  // Import the generated ViewBinding class

class LandlordTenantsFragment : Fragment() {

    // Declare a variable to hold the ViewBinding instance
    private lateinit var binding: LandlordTenantsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using ViewBinding
        binding = LandlordTenantsFragmentBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Now you can access views in your layout using binding, e.g., binding.textViewId.text = "Hello, World!"

    }
}
