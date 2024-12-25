package com.example.jobfinder.UI.Home
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.jobfinder.Datas.Model.idAndRole
import com.example.jobfinder.R
import com.example.jobfinder.UI.Admin.Home.AdminHomeActivity
import com.example.jobfinder.UI.Notifications.NotificationsFragment
import com.example.jobfinder.UI.SplashScreen.SelectRoleActivity
import com.example.jobfinder.UI.UsersProfile.UserDetailActivity
import com.example.jobfinder.UI.WorkingJob.NUserWorkingJobActivity
import com.example.jobfinder.UI.WorkingJob.WorkingJobActivity
import com.example.jobfinder.Utils.FragmentHelper
import com.example.jobfinder.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityHomeBinding
    private var backPressedCount = 0
    private var addingFragmentInProgress = false
    private lateinit var viewModel: HomeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        // Khởi tạo viewmodel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.fetchJobs()

        // Chỉ add fragment vào home khi trạng thái hiện tại là null (tránh xoay màn hình lại add lại gây lỗi)
        if (savedInstanceState == null) {
            addingFragmentInProgress = true

            FirebaseDatabase.getInstance().getReference("UserRole").child(uid.toString()).get()
                .addOnSuccessListener { snapshot ->
                    val data: idAndRole? = snapshot.getValue(idAndRole::class.java)
                    data?.let {
                        val userRole = data.role
                        if (data.accountStatus == "active") {
                            viewModel.userRole = userRole.toString()
                            addFragmentDefault(viewModel.userRole)
                            updateNavigationBar()
                            addingFragmentInProgress = false
                        }else{
                            // trả về result về login để đóng activity
                            binding.animationView.visibility = View.GONE
                            auth.signOut()
                            val intent = Intent(this, SelectRoleActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, R.string.disabled_account, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
        } else {
            // Khôi phục trạng thái của activity từ bundle
            viewModel.userRole = savedInstanceState.getString("userRole", "")
            binding.animationView.visibility = View.GONE
        }


        // trả về result về login để đóng activity
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)


        // thanh Navigation
        binding.bottomNavView.setOnItemSelectedListener { menuItem ->
            if (!addingFragmentInProgress) { // Kiểm tra xem quá trình thêm fragment có đang diễn ra không
                handleNavigation(menuItem.itemId)
            }
            true
        }

    }

    override fun onResume() {
        super.onResume()
        backPressedCount = 0 // Reset lại backPressedCount khi activity resume
        updateNavigationBar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Lưu trạng thái của activity vào bundle
        outState.putString("userRole", viewModel.userRole)
    }


    // Bấm 1 lần để hỏi, lần thứ 2 sẽ thoát ứng dụng
    override fun onBackPressed() {
        if (backPressedCount >= 1) {
            setResult(Activity.RESULT_CANCELED)
            super.onBackPressed() // đóng activity
            finish()
        } else {
            backPressedCount++
            Toast.makeText(this, getString(R.string.backpress_ask), Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                backPressedCount = 0
            }, 2000) // Reset backPressedCount sau 2 giây
        }
    }

    // Xử lí các bottom Navigation
    private fun handleNavigation(itemId: Int): Boolean {
        if (isCurrentFragment(itemId)) {
            return true // Không cần reload lại fragment nếu đã ở trong fragment mục tiêu
        }
        when(itemId) {
            R.id.home -> {
                if (viewModel.userRole == "BUser") {
                    FragmentHelper.replaceFragment(supportFragmentManager, binding.HomeFrameLayout, HomeFragmentBuser())
                } else {
                    FragmentHelper.replaceFragment(supportFragmentManager, binding.HomeFrameLayout, HomeFragmentNuser())
                }
                return true
            }
            R.id.managermentJob  -> {
                if (viewModel.userRole == "BUser") {
                    startActivity(Intent(this, WorkingJobActivity::class.java))
                } else {
                    startActivity(Intent(this, NUserWorkingJobActivity::class.java))
                }
                return true
            }
            R.id.notify -> {
                FragmentHelper.replaceFragment(supportFragmentManager, binding.HomeFrameLayout, NotificationsFragment())
                return true
            }
            R.id.profile -> {
                startActivity(Intent(this, UserDetailActivity::class.java))
                return true
            }
            else -> {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                return false
            }
        }
    }


    // Hàm này thực hiện việc thêm fragment default dựa trên giá trị của userRole
    private fun addFragmentDefault(curRole: String) {
        when (curRole) {
            "BUser" -> {
                FragmentHelper.replaceFragment(supportFragmentManager, binding.HomeFrameLayout, HomeFragmentBuser())
                binding.animationView.visibility = View.GONE
                binding.bottomNavView.visibility= View.VISIBLE
            }
            "NUser" -> {
                FragmentHelper.replaceFragment(supportFragmentManager, binding.HomeFrameLayout, HomeFragmentNuser())
                binding.animationView.visibility = View.GONE
                binding.bottomNavView.visibility= View.VISIBLE
            }
            "Admin" ->{
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                startActivity(Intent(this, AdminHomeActivity::class.java))
                finish()
                binding.animationView.visibility = View.GONE
            }
            else -> {
                Toast.makeText(this, "Có lỗi xảy ra khi thêm fragment default", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Kiểm tra fragment hiện tại và cập nhật icon trên thanh điều hướng dưới cùng (navbar) tương ứng
    private fun updateNavigationBar() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.HomeFrameLayout)
        when {
            currentFragment is HomeFragmentNuser && viewModel.userRole == "NUser" -> {
                binding.bottomNavView.selectedItemId = R.id.home
                binding.animationView.visibility = View.GONE
            }
            currentFragment is HomeFragmentBuser && viewModel.userRole == "BUser" -> {
                binding.bottomNavView.selectedItemId = R.id.home
                binding.animationView.visibility = View.GONE
            }
            currentFragment is NotificationsFragment -> {
                binding.bottomNavView.selectedItemId = R.id.notify
                binding.animationView.visibility = View.GONE
            }
        }
    }

    // tránh reload lại khi đang hiển thị fragment mà nhấn nav lần nữa trùng fragment đó
    private fun isCurrentFragment(itemId: Int): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.HomeFrameLayout)
        return when (itemId) {
            R.id.home -> {
                if (viewModel.userRole == "BUser") {
                    currentFragment is HomeFragmentBuser
                } else {
                    currentFragment is HomeFragmentNuser
                }
            }
            R.id.notify -> currentFragment is NotificationsFragment
            else -> false
        }
    }
    

}