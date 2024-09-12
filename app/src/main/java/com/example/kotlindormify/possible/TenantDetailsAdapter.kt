package com.example.kotlindormify.possible

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.R
import com.example.kotlindormify.TenantsDetails


class TenantDetailsAdapter(private val tenantsdetailList: List<TenantsDetails>) :
    RecyclerView.Adapter<TenantDetailsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        val etFullName: TextView = itemView.findViewById(R.id.etFullNametenant)
        val etAge: TextView = itemView.findViewById(R.id.etAgetenant)
        val etAddress2tenant: TextView = itemView.findViewById(R.id.etAddress2tenant)
        val etPhoneNumbertenant: TextView = itemView.findViewById(R.id.etPhoneNumbertenant)
        val etEmailtenant: TextView = itemView.findViewById(R.id.etEmailtenant)
        val passETtenant: TextView = itemView.findViewById(R.id.passETtenant)




       // val tvDistance: TextView = itemView.findViewById(R.id.ivSelectedImagetenant)
       // val tvDistance: ImageView = imageView.findViewById(R.id.ivSelectedImagetenant)
     //   val uploadbtn : ImageView = itemView.findViewById(R.id.btnAddImage2tenant)
        // Add other views as needed based on your layout

        init {
            // Set a click listener for the item
            itemView.setOnClickListener {
                // Get the clicked dormitory item
                val clickedtenants = tenantsdetailList[adapterPosition]


                // Create a bundle to pass tenants data to the detail fragment
                val bundle = Bundle()
                bundle.putString("username", clickedtenants.username)
                bundle.putString("age", clickedtenants.age)
                bundle.putString("sex", clickedtenants.sex)
                bundle.putString("address", clickedtenants.address)
                bundle.putString("phoneNumber", clickedtenants.phoneNumber)
                bundle.putString("email", clickedtenants.email)
                bundle.putString("password", clickedtenants.password)
              //  bundle.putString("profile", clickedtenants.profile)
            //    bundle.putString("identification", clickedtenants.identification)


            }
        }


    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_tenant_details, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tenantsDetails = tenantsdetailList[position]
        holder.etFullName.text = tenantsDetails.username
        holder.etAge.text = tenantsDetails.age
       // holder.etAddress2tenant.text = tenantsDetails.sex
        holder.etAddress2tenant.text = tenantsDetails.address
        holder.etPhoneNumbertenant.text = tenantsDetails.phoneNumber
        holder.passETtenant.text = tenantsDetails.password
        holder.etEmailtenant.text = tenantsDetails.email





    }



    override fun getItemCount(): Int {
        return tenantsdetailList.size

    }

}
