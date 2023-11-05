package com.example.kotlindormify

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindormify.databinding.ActivitySignUpBinding
import com.example.kotlindormify.possible.SignUpTenant1Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedImageUri: Uri
    private var isImageSelected = false

    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

//next btn
        binding.rbTenant.setOnClickListener {
            if (binding.rbTenant.isChecked) {
                // Tenant is selected, so deselect Landlord
                binding.rbLandlord.isChecked = false
            }
        }

        binding.rbLandlord.setOnClickListener {
            if (binding.rbLandlord.isChecked) {
                // Landlord is selected, so deselect Tenant
                binding.rbTenant.isChecked = false
            }
        }

        binding.cvtent.setOnClickListener {
            binding.rbTenant.isChecked = true

            if (binding.rbTenant.isChecked) {
                // Tenant is selected, so deselect Landlord
                binding.rbLandlord.isChecked = false
            }
        }
        binding.cvland.setOnClickListener {
            binding.rbLandlord.isChecked = true

            if (binding.rbLandlord.isChecked) {
                // Landlord is selected, so deselect Tenant
                binding.rbTenant.isChecked = false
            }
        }

        binding.selectbtn.setOnClickListener {


            if (binding.rbTenant.isChecked) {
                // Launch the activity for Tenant
                val intent = Intent(this, SignUpTenantActivity::class.java)
                startActivity(intent)


            } else if (binding.rbLandlord.isChecked) {
                // Landlord layout
                val intent = Intent(this, SignUpLandlordActivity::class.java)
                startActivity(intent)


            } else {
                // Handle the case when neither RadioButton is selected
                // You can show a toast or perform any other action here.

                Toast.makeText(this, "You need to select first", Toast.LENGTH_SHORT).show()
            }

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


    }


}
