package com.example.jobfinder.UI.Admin.UserManagement

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinder.Datas.Model.AdminModel.BasicInfoAndRole
import com.example.jobfinder.UI.UserDetailInfo.BUserDetailInfoActivity
import com.example.jobfinder.databinding.ActivityAdminUserManagBinding


class AdminUserManagActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminUserManagBinding
    private val viewModel: AdminUserManagementViewModel by viewModels()
    lateinit var adapter: AdminUserManagementAdapter
    private var isActivityOpened = false
    private val REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserManagBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // nút back về
        binding.backButton.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.UMSwipe.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.fetchUserList()
                binding.searchView.clearFocus()
                binding.searchView.setQuery("", false)
                binding.UMSwipe.isRefreshing = false
            }, 1000)
        }

        adapter = AdminUserManagementAdapter(mutableListOf())
        binding.recyclerUserList.adapter = adapter
        binding.recyclerUserList.layoutManager = LinearLayoutManager(this)

        viewModel.fetchUserList()

        viewModel.userList.observe(this){userList->
            adapter.updateData(userList)
            checkEmptyAdapter(userList)
        }

        adapter.setOnItemClickListener(object : AdminUserManagementAdapter.OnItemClickListener {
            override fun onItemClick(userInfo: BasicInfoAndRole) {
                if (!isActivityOpened) {
                    val intent = when (userInfo.userRole) {
                        "NUser" -> Intent(this@AdminUserManagActivity, AdminUMNUserDetail::class.java)
                        "BUser" -> Intent(this@AdminUserManagActivity, BUserDetailInfoActivity::class.java)
                        else -> null // Handle other roles if needed
                    }

                    if (intent != null) {
                        intent.putExtra("uid", userInfo.userBasicInfo.user_id.toString())
                        intent.putExtra("accStatus", userInfo.accountStatus.toString())
                        startActivityForResult(intent, REQUEST_CODE)
                    }
                    isActivityOpened = true
                }
            }
        })

        adapter.setDataChangeListener(object : AdminUserManagementAdapter.DataChangeListener {
            override fun onDataChanged(filteredList: List<BasicInfoAndRole>) {
                checkEmptyAdapter(filteredList)
            }
        })

        // mục search
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(submitInput: String?): Boolean {
                // Ẩn bàn phím
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
                // bỏ focus
                binding.searchView.clearFocus()
                // logic search
                adapter.filter.filter(submitInput)
                return true
            }
            override fun onQueryTextChange(dataInput: String): Boolean {
                // Nếu không nhập text vào
                return if (dataInput.isEmpty()) {
                    adapter.resetOriginalList()
                    false
                } else { // có nhập text
                    adapter.filter.filter(dataInput)
                    true
                }
            }
        })

        // nút close của searchView
        binding.searchView.setOnCloseListener {
            adapter.resetOriginalList()
            false
        }

        binding.main.setOnClickListener {
            binding.searchView.clearFocus()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            isActivityOpened = false
            viewModel.fetchUserList()
            binding.searchView.clearFocus()
            binding.searchView.setQuery("", false)
        }
    }

    private fun checkEmptyAdapter(list: List<BasicInfoAndRole>) {
        if (list.isEmpty()) {
            binding.noUser.visibility = View.VISIBLE
            binding.animationView.visibility = View.GONE
        } else {
            binding.noUser.visibility = View.GONE
            binding.animationView.visibility = View.GONE
        }
    }

}