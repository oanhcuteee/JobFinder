package com.example.jobfinder.UI.Home

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.jobfinder.R
import com.example.jobfinder.databinding.FragmentForgotPassBinding
import com.example.jobfinder.databinding.FragmentTestBinding

class TestFragment : Fragment() {
    private lateinit var binding: FragmentTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTestBinding.inflate(inflater, container, false)

//        binding.webView.settings.javaScriptEnabled = true
//        binding.webView.webViewClient = object : WebViewClient() {
//            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//                super.onPageStarted(view, url, favicon)
//                binding.animationView.visibility = View.VISIBLE
//            }
//
//            override fun onPageFinished(view: WebView?, url: String?) {
//                super.onPageFinished(view, url)
//                binding.animationView.visibility = View.GONE
//            }
//        }
//        binding.webView.loadUrl("https://www.youtube.com/")
//        val handler = Handler()
//        handler.postDelayed({
//            binding.webView.loadUrl("https://www.youtube.com/")
//        }, 2000) // Đợi 2000ms (2 giây) trước khi load URL
        return binding.root
    }

}