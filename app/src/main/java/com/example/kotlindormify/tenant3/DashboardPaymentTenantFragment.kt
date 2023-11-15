package com.example.kotlindormify.tenant3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.kotlindormify.R

class DashboardPaymentTenantFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard_payment_tenant, container, false)

        val btnpay: CardView = view.findViewById(R.id.ivpay)

        // Now you can use btnpay as needed.


        return view
    }
}



