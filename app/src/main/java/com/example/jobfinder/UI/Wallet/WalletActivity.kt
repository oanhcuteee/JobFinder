package com.example.jobfinder.UI.Wallet

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.example.jobfinder.R
import com.example.jobfinder.Utils.FragmentHelper
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.databinding.ActivityWalletBinding

class WalletActivity : AppCompatActivity() , WalletFragment.DataLoadListener {
    private lateinit var binding: ActivityWalletBinding
    private var isExpanded = true
    private lateinit var fadeInAnimation: Animation
    private lateinit var fadeOutAnimation: Animation
    private val PAYMENT_REQUEST_CODE = 1001
    private var backCheck = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fadeInAnimation= AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeInAnimation.duration = 500
        fadeOutAnimation= AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
        fadeOutAnimation.duration = 500

        binding.mainFtBtn.setOnClickListener {
            // Đảo ngược trạng thái mở rộng và cập nhật giao diện
            isExpanded = !isExpanded
            updateFABVisibility()
        }

        binding.walletSwipe.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.inputNum.setText("")
                binding.inputNum.clearFocus()
                FragmentHelper.replaceFragment(supportFragmentManager, binding.walletActivityFramelayout, WalletFragment(binding.ZalopaySection))
                binding.walletSwipe.isRefreshing = false
            }, 1000)
        }

        // add fragment mặc định khi mới mở
        FragmentHelper.replaceFragment(supportFragmentManager, binding.walletActivityFramelayout, WalletFragment(binding.ZalopaySection))

        binding.addWalletFtBtn.setOnClickListener {
            // Thay thế com.example.jobfinder.UI.Wallet.WalletFragment bằng AddWalletFragment
            FragmentHelper.replaceFragment(supportFragmentManager, binding.walletActivityFramelayout, AddWalletFragment(binding.ZalopaySection))
            binding.walletTitle.setText(R.string.add_wallet)
            backCheck = true
        }


        binding.addWalletFtTxt.setOnClickListener {
            // Thay thế com.example.jobfinder.UI.Wallet.WalletFragment bằng AddWalletFragment
            FragmentHelper.replaceFragment(supportFragmentManager, binding.walletActivityFramelayout, AddWalletFragment(binding.ZalopaySection))
            binding.walletTitle.setText(R.string.add_wallet)
            backCheck = true
        }

        binding.walletHistoryFtBtn.setOnClickListener{
            startActivity(Intent(this, WalletHistoryActivity::class.java))
        }

        binding.walletHistoryFtTxt.setOnClickListener{
            startActivity(Intent(this, WalletHistoryActivity::class.java))
        }

        // back bằng nút trên màn hình
        binding.backButton.setOnClickListener{
            if(backCheck){
                FragmentHelper.replaceFragment(supportFragmentManager, binding.walletActivityFramelayout, WalletFragment(binding.ZalopaySection))
                binding.walletTitle.setText(R.string.wallet_title)
                backCheck= false
            }else {
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        binding.confirmNum.setOnClickListener {
            val inputNum = binding.inputNum.text.toString()
            val inputNumDouble = inputNum.toDoubleOrNull()

            if (inputNum.isEmpty() || inputNum.toInt() <= 0) {
                Toast.makeText(this, getString(R.string.wallet_deposit_amount), Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, ZaloPaymentOrderActivity::class.java)
                intent.putExtra("amount", inputNumDouble)
                startActivityForResult(intent, 1000)
            }
        }
    }

    // Xử lý khi dữ liệu đã tải xong bên com.example.jobfinder.UI.Wallet.WalletFragment (nếu adapter không rỗng)
    override fun onDataLoaded() {
        binding.animationView.visibility = View.GONE
    }

    // Kiểm tra nếu adapter rỗng, ẩn animationView, hiện noWalletCard
    override fun onDataLoadedEmpty(isListEmpty: Boolean) {
        val walletFragment = supportFragmentManager.findFragmentById(R.id.wallet_activity_framelayout) as? WalletFragment
        if (isListEmpty) {
            walletFragment?.updateNoWalletCardVisibility(View.VISIBLE)
        } else {
            walletFragment?.updateNoWalletCardVisibility(View.GONE)
        }
        binding.animationView.visibility = View.GONE
    }

    private fun updateFABVisibility() {
        if (isExpanded) {
            binding.walletHistoryFtTxt.startAnimation(fadeOutAnimation)
            binding.walletHistoryFtBtn.startAnimation(fadeOutAnimation)
            binding.addWalletFtTxt.startAnimation(fadeOutAnimation)
            binding.addWalletFtBtn.startAnimation(fadeOutAnimation)
            binding.walletHistoryFtTxt.visibility = View.GONE
            binding.walletHistoryFtBtn.visibility = View.GONE
            binding.addWalletFtTxt.visibility = View.GONE
            binding.addWalletFtBtn.visibility = View.GONE
            binding.walletHistoryFtBtn.isClickable = false
            binding.addWalletFtBtn.isClickable = false
            binding.walletHistoryFtTxt.isClickable = false
            binding.addWalletFtTxt.isClickable = false
        } else {
            binding.walletHistoryFtTxt.startAnimation(fadeInAnimation)
            binding.walletHistoryFtBtn.startAnimation(fadeInAnimation)
            binding.addWalletFtTxt.startAnimation(fadeInAnimation)
            binding.addWalletFtBtn.startAnimation(fadeInAnimation)
            binding.walletHistoryFtTxt.visibility = View.VISIBLE
            binding.walletHistoryFtBtn.visibility = View.VISIBLE
            binding.addWalletFtTxt.visibility = View.VISIBLE
            binding.addWalletFtBtn.visibility = View.VISIBLE
            binding.walletHistoryFtBtn.isClickable = true
            binding.addWalletFtBtn.isClickable = true
            binding.walletHistoryFtTxt.isClickable = true
            binding.addWalletFtTxt.isClickable = true
        }
    }

    private fun showPaymentResultDialog(message: String, imageResId: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.zalo_dialog_payment_result)
        dialog.setCanceledOnTouchOutside(true)

        val resultImage = dialog.findViewById<ImageView>(R.id.result_image)
        val resultText = dialog.findViewById<TextView>(R.id.result_text)
        val closeButton = dialog.findViewById<TextView>(R.id.close_button)

        resultImage.setImageResource(imageResId)
        resultText.text = message

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            FragmentHelper.replaceFragment(supportFragmentManager, binding.walletActivityFramelayout, WalletFragment(binding.ZalopaySection))
        }

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            val var1 = data?.getStringExtra("var1")
            val var2 = data?.getStringExtra("var2")
            val var3 = data?.getStringExtra("var3")
            val err = data?.getStringExtra("error")

            if(var1!= null && var2!= null && var3!= null && err == "none") {
                showPaymentResultDialog(getString(R.string.wallet_deposit_success), R.drawable.ic_payment_success)
            }
            if(var3 == null &&var1!= null && var2!= null && err == "none"){
                showPaymentResultDialog(getString(R.string.wallet_deposit_failed), R.drawable.ic_payment_failed)
            }
            if(var1!= null && var2!= null && var3!= null && err != "none") {
                showPaymentResultDialog(getString(R.string.wallet_deposit_error), R.drawable.ic_payment_failed)
            }
            binding.inputNum.setText("")
            binding.inputNum.clearFocus()
        }
    }


}