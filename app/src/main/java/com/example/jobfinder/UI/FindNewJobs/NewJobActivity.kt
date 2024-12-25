package com.example.jobfinder.UI.FindNewJobs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.R
import com.example.jobfinder.UI.JobDetails.SeekerJobDetailActivity
import com.example.jobfinder.databinding.ActivityNewJobBinding
import com.example.jobfinder.databinding.CustomFilterLayoutBinding
import com.google.android.material.slider.RangeSlider
import java.text.NumberFormat
import java.util.Currency




class NewJobActivity : AppCompatActivity() {
    lateinit var binding: ActivityNewJobBinding
    lateinit var cusBindingFilter: CustomFilterLayoutBinding
    private val viewModel: FindNewJobViewModel by viewModels()
    private lateinit var adapter: NewJobsAdapter
    private var isJobDetailActivityOpen = false
    private var isFirstApplyFilter = true

    // Biến cho filter
    private lateinit var jtButtons: List<Button>
    private lateinit var recNameButtons: List<Button>
    private lateinit var postedTimeButtons: List<Button>
    private var ftJobTitle = 0
    private var ftRecTitle = 0
    private var ftPostTime = 1
    private var ftShift = 0
    private var ftMinSalary = 0.0f
    private var ftMaxSalary = 50000.0f
    private var ftStartHr = 0
    private var ftEndHr = 24
    private var jobTypeId = 0

    private var isLoadingData: Boolean = true

    @SuppressLint("DefaultLocale")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewJobBinding.inflate(layoutInflater)
        cusBindingFilter = CustomFilterLayoutBinding.bind(binding.slideMenu.root)
        setContentView(binding.root)

        // nút back về home trên màn hình
        binding.backButton.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        // chạy hàm lấy data các công việc
        viewModel.fetchJobs()

        // gán data vào adapter sau khi fetch
        adapter = NewJobsAdapter(listOf())
        binding.newJobHomeRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.newJobHomeRecyclerView.adapter = adapter

        adapter.setDataChangeListener(object : NewJobsAdapter.DataChangeListener {
            override fun onDataChanged(filteredList: List<JobModel>) {
                if (!isLoadingData) {
                    checkEmptyAdapter(filteredList)
                }
            }
        })

        //refresh activity
        binding.findJobSwipe.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.refreshFetchJobs()
                //reset filter
                defaultSelectionUIFilter()
                saveButtonUIState()
                ftJobTitle =0
                ftRecTitle =0
                ftPostTime =1
                ftShift =0
                ftMinSalary = 0.0f
                ftMaxSalary = 50000.0f
                ftStartHr = 0
                ftEndHr = 24
                jobTypeId = 0
                //reset search
                binding.searchView.clearFocus()
                binding.searchView.setQuery("", false)
                binding.findJobSwipe.isRefreshing = false
            }, 1000)
        }


        isLoadingData = true
        // Cập nhật adapter khi có dữ liệu mới từ ViewModel
        viewModel.jobsListLiveData.observe(this) { newItem ->
//                val sortedByPostDate = newItem.toMutableList().sortedByDescending { GetData.convertStringToDATE(it.postDate.toString()) }
            adapter.updateData(newItem)
            isLoadingData = false
            checkEmptyAdapter(newItem)
        }

        // Click vào từng item trong recycler
        adapter.setOnItemClickListener(object : NewJobsAdapter.onItemClickListener {
            override fun onItemClicked(Job: JobModel) {
                if (!isJobDetailActivityOpen) {
                    isJobDetailActivityOpen = true
                    val intent = Intent(this@NewJobActivity, SeekerJobDetailActivity::class.java)
                    intent.putExtra("job", Job)
                    startActivityForResult(intent, 1003)
                }
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

        binding.rootNewJob.setOnClickListener {
            binding.searchView.clearFocus()
        }







        // PHẦN CUSTOM FILTER--------------------------------------------------------------------
        jtButtons = listOf(cusBindingFilter.JTAll, cusBindingFilter.JTAtoZ)
        recNameButtons = listOf(cusBindingFilter.recAll, cusBindingFilter.recAtoZ)
        postedTimeButtons = listOf(cusBindingFilter.PTAnytime, cusBindingFilter.PTNewest, cusBindingFilter.PTThisMonth)

        // Khởi tạo luôn UI mặc định cho filter trước khi mở bằng drawer
        defaultSelectionUIFilter()

        // nút mở filter drawer
        binding.filterIcon.setOnClickListener {
            binding.rootNewJob.openDrawer(GravityCompat.END)
            binding.searchView.clearFocus()
        }
        binding.rootNewJob.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerOpened(drawerView: View) {
                // Nếu đã nhấn apply ít nhất một lần, khôi phục trạng thái từ SharedPreferences
                if (!isFirstApplyFilter) {
                    restoreButtonUIState()
                }
            }
            // Các hàm khác khi được đóng hoặc mở, dùng nếu cần thiết
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })


        // Jobtitle btn list
        jtButtons.forEach { button ->
            button.setOnClickListener {
                handleButtonSelection(jtButtons, button, "job")
                // Nếu đang chọn JTAtoZ không cho chọn recNameAtoZ và postime newest
                adjustButtonUIState(button,
                    cusBindingFilter.JTAtoZ,
                    cusBindingFilter.recAtoZ,
                    cusBindingFilter.PTNewest,
                    cusBindingFilter.PTAnytime,
                    cusBindingFilter.PTThisMonth)
            }
        }

        // Recruiter btn list
        recNameButtons.forEach { button ->
            button.setOnClickListener {
                handleButtonSelection(recNameButtons, button,"rec")
                // Nếu đang chọn recNameAtoZ không cho chọn JTAtoZ và postime newest
                adjustButtonUIState(button,
                    cusBindingFilter.recAtoZ,
                    cusBindingFilter.JTAtoZ,
                    cusBindingFilter.PTNewest,
                    cusBindingFilter.PTAnytime,
                    cusBindingFilter.PTThisMonth)
            }
        }

        // Posttime btn list
        postedTimeButtons.forEach { button ->
            button.setOnClickListener {
                handleButtonSelection(postedTimeButtons, button,"time")
            }
        }

        // Setup Spinner
        val jobTypes = listOf(getString(R.string.sort_defaultSelectJobType)) + resources.getStringArray(R.array.job_types_array)
        val spinnerAdapter = ArrayAdapter(this, R.layout.simple_spinner_item_filter, jobTypes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cusBindingFilter.workTypeSpinner.adapter = spinnerAdapter

        cusBindingFilter.workTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                jobTypeId = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Xử lý khi không có mục nào được chọn
            }
        }

        // Seekbar lương slider
        cusBindingFilter.rangeslider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            // Responds to when slider's touch event is being started
            override fun onStartTrackingTouch(slider: RangeSlider) {}
            // Responds to when slider's touch event is being stopped
            override fun onStopTrackingTouch(slider: RangeSlider) {}
        })
        // Responds to when slider's value is changed
        cusBindingFilter.rangeslider.addOnChangeListener { rangeSlider, values, fromUser ->
            // Cập nhật giá trị của ftMinSalary và ftMaxSalary khi người dùng thay đổi giá trị của thanh trượt
            ftMinSalary = rangeSlider.values[0]
            ftMaxSalary = rangeSlider.values[1]
        }
        // Format định dạng hiển thị của tiền việt trên label
        cusBindingFilter.rangeslider.setLabelFormatter { value: Float ->
            val format = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 0
            format.currency = Currency.getInstance("VND")
            format.format(value.toDouble())
        }

        // Seekbar giờ làm slider
        cusBindingFilter.workshiftSlider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            // Responds to when slider's touch event is being started
            override fun onStartTrackingTouch(slider: RangeSlider) {
            }
            // Responds to when slider's touch event is being stopped
            override fun onStopTrackingTouch(slider: RangeSlider) {
            }
        })
        // Responds to when slider's value is changed
        cusBindingFilter.workshiftSlider.addOnChangeListener { rangeSlider, value, fromUser ->
            ftStartHr = rangeSlider.values[0].toInt()
            ftEndHr = rangeSlider.values[1].toInt()
        }
        // Format định dạng hiển thị giờ trên label
        cusBindingFilter.workshiftSlider.setLabelFormatter { value: Float ->
            // Ví dụ convert 1.5 sẽ trở thành "01:30"
            val hours = value.toInt()
            val minutes = ((value - hours) * 60).toInt()
            String.format("%02d:%02d", hours, minutes)
        }


        // reset filter btn
        cusBindingFilter.resetBtn.setOnClickListener {
            defaultSelectionUIFilter()
            saveButtonUIState()
            binding.searchView.clearFocus()
            binding.searchView.setQuery("", false)
            ftJobTitle =0
            ftRecTitle =0
            ftPostTime =1
            ftShift =0
            ftMinSalary = 0.0f
            ftMaxSalary = 50000.0f
            ftStartHr = 0
            ftEndHr = 24
            jobTypeId = 0
            viewModel.sortFilter(ftJobTitle, ftRecTitle, ftPostTime, ftMinSalary, ftMaxSalary, ftStartHr, ftEndHr, jobTypeId)
        }

        // apply filter btn
        cusBindingFilter.applyBtn.setOnClickListener {
            Toast.makeText(this, getString(R.string.sort_applyFilterToast), Toast.LENGTH_SHORT).show()
            isFirstApplyFilter = false
            saveButtonUIState()

            viewModel.sortFilter(ftJobTitle, ftRecTitle, ftPostTime, ftMinSalary, ftMaxSalary, ftStartHr, ftEndHr, jobTypeId)

            binding.rootNewJob.closeDrawer(GravityCompat.END)
        }



    } // end of onCreate()


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1003 && resultCode == Activity.RESULT_OK) {
            isJobDetailActivityOpen = false
        }
    }


    private fun handleButtonSelection(buttonList: List<Button>, selectedButton: Button, Type:String) {
        // Cập nhật background của Button được chọn
        selectedButton.setBackgroundResource(R.drawable.custom_filter_btn_selected)
        for (btn in buttonList) {
            if (btn != selectedButton) {
                btn.setBackgroundResource(R.drawable.custom_filter_btn_default)
            }
        }

        val selectedIndex = buttonList.indexOf(selectedButton)
        if(Type == "job") ftJobTitle = selectedIndex
        if(Type == "rec") ftRecTitle = selectedIndex
        if(Type == "time") ftPostTime = selectedIndex
        if(Type == "shift") ftShift = selectedIndex

    }

    private fun defaultSelectionUIFilter() {
        // Các btn muốn đặt lại UI về default
        val selectedButtons = listOf(cusBindingFilter.JTAll, cusBindingFilter.recAll, cusBindingFilter.PTNewest)
        // Giá trị mặc định của RangeSlider
        val defaultValuesSalary = resources.getStringArray(R.array.initial_slider_values).map { it.toFloat() }
        val defaultValueWS = resources.getStringArray(R.array.initial_WShift_Slider_values).map { it.toFloat() }

        val allButtons = listOf(
            // Job title buttons
            cusBindingFilter.JTAll, cusBindingFilter.JTAtoZ,
            // Recruiter name buttons
            cusBindingFilter.recAll, cusBindingFilter.recAtoZ,
            // Posted time buttons
            cusBindingFilter.PTAnytime, cusBindingFilter.PTNewest, cusBindingFilter.PTThisMonth,
        )

        for (btn in allButtons) {
            btn.setBackgroundResource(  // Nếu ngoài các btn mặc định thì đổi tất cả về background default
                if (btn in selectedButtons) R.drawable.custom_filter_btn_selected
                else R.drawable.custom_filter_btn_default
            )
            // Đặt alpha và isEnabled về mặc định
            btn.alpha = 1.0F
            btn.isEnabled = true
        }

        // Đặt lại giá trị của RangeSlider về mặc định
        cusBindingFilter.rangeslider.setValues(defaultValuesSalary)
        cusBindingFilter.workshiftSlider.setValues(defaultValueWS)


        val jobTypes = listOf(getString(R.string.sort_defaultSelectJobType)) + resources.getStringArray(R.array.job_types_array)
        // Đặt mặc định spinner là "Tất cả công việc"
        val defaultPosition = jobTypes.indexOf(getString(R.string.sort_defaultSelectJobType))
        cusBindingFilter.workTypeSpinner.setSelection(defaultPosition)
    }

    // Hàm để điều chỉnh trạng thái của targetButton dựa trên trạng thái của selectedButton
    private fun adjustButtonUIState(
        curSelectedButton: Button,
        buttonToCheck: Button,
        targetButton: Button,
        PTnewest: Button,
        PTanytime: Button,
        PTthismonth: Button) {

        val isButtonToCheckSelected = (curSelectedButton == buttonToCheck)

        targetButton.apply {
            alpha = if (isButtonToCheckSelected) 0.4F else 1.0F
            isEnabled = !isButtonToCheckSelected
        }
        PTnewest.apply {
            alpha = if (isButtonToCheckSelected) 0.4F else 1.0F
            isEnabled = !isButtonToCheckSelected
            setBackgroundResource(
                if (isButtonToCheckSelected) R.drawable.custom_filter_btn_default
                else R.drawable.custom_filter_btn_selected
            )
        }
        PTanytime.apply {
            setBackgroundResource(
                if (isButtonToCheckSelected) R.drawable.custom_filter_btn_selected
                else R.drawable.custom_filter_btn_default
            )
        }
        PTthismonth.apply {
            setBackgroundResource(R.drawable.custom_filter_btn_default)
        }
    }

    // Lưu trạng thái các nút đã dc chọn trong drawerable vào sharedPreferences
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun saveButtonUIState() {
        val sharedPreferences = getSharedPreferences("filter_preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        // Lưu trạng thái của các nút
        for (button in jtButtons + recNameButtons + postedTimeButtons) {
            val isSelected = button.background.constantState == resources.getDrawable(R.drawable.custom_filter_btn_selected, null).constantState
            editor.putBoolean(button.id.toString(), isSelected)
        }
        // Lưu giá trị của slider
        val sliderValuesSalary = cusBindingFilter.rangeslider.values
        val sliderValuesWS = cusBindingFilter.workshiftSlider.values
        editor.putFloat("slider_start_value_Salary", sliderValuesSalary[0])
        editor.putFloat("slider_end_value_Salary", sliderValuesSalary[1])
        editor.putFloat("slider_start_value_WS", sliderValuesWS[0])
        editor.putFloat("slider_end_value_WS", sliderValuesWS[1])

        // Lưu trạng thái của Spinner
        val spinnerPosition = cusBindingFilter.workTypeSpinner.selectedItemPosition
        editor.putInt("spinner_position", spinnerPosition)

        editor.apply()
    }

    // Khôi phục trạng thái btn từ sharedPreferences
    private fun restoreButtonUIState() {
        val sharedPreferences = getSharedPreferences("filter_preferences", MODE_PRIVATE)

        // Khôi phục trạng thái của các nút
        for (button in jtButtons + recNameButtons + postedTimeButtons) {
            val isSelected = sharedPreferences.getBoolean(button.id.toString(), false)
            button.apply {
                setBackgroundResource(if (isSelected) R.drawable.custom_filter_btn_selected else R.drawable.custom_filter_btn_default)
                if (isSelected) {
                    alpha = 1.0F
                    isEnabled = true
                }
            }
        }
        // Điều chỉnh trạng thái nếu một trong hai AtoZ của list jtButton và recNameButton
        val isJTAtoZSelected = sharedPreferences.getBoolean(cusBindingFilter.JTAtoZ.id.toString(), false)
        val isRecAtoZSelected = sharedPreferences.getBoolean(cusBindingFilter.recAtoZ.id.toString(), false)
        val isPTnewestSelected = sharedPreferences.getBoolean(cusBindingFilter.PTNewest.id.toString(), false)

        if (isJTAtoZSelected && !isRecAtoZSelected && !isPTnewestSelected) {
            cusBindingFilter.recAtoZ.apply {
                setBackgroundResource(R.drawable.custom_filter_btn_default)
                alpha = 0.4F
                isEnabled = false
            }
            cusBindingFilter.PTNewest.apply {
                setBackgroundResource(R.drawable.custom_filter_btn_default)
                alpha = 0.4F
                isEnabled = false
            }
            cusBindingFilter.recAll.apply {
                setBackgroundResource(R.drawable.custom_filter_btn_selected)
                alpha = 1.0F
                isEnabled = true
            }
        } else if (!isJTAtoZSelected && isRecAtoZSelected && !isPTnewestSelected) {
            cusBindingFilter.JTAtoZ.apply {
                setBackgroundResource(R.drawable.custom_filter_btn_default)
                alpha = 0.4F
                isEnabled = false
            }
            cusBindingFilter.PTNewest.apply {
                setBackgroundResource(R.drawable.custom_filter_btn_default)
                alpha = 0.4F
                isEnabled = false
            }
            cusBindingFilter.JTAll.apply {
                setBackgroundResource(R.drawable.custom_filter_btn_selected)
                alpha = 1.0F
                isEnabled = true
            }
        } else if (!isJTAtoZSelected && !isRecAtoZSelected && isPTnewestSelected) {
            cusBindingFilter.JTAtoZ.apply {
                setBackgroundResource(R.drawable.custom_filter_btn_default)
                alpha = 1.0F
                isEnabled = true
            }
            cusBindingFilter.PTNewest.apply {
                setBackgroundResource(R.drawable.custom_filter_btn_selected)
                alpha = 1.0F
                isEnabled = true
            }
            cusBindingFilter.JTAll.apply {
                setBackgroundResource(R.drawable.custom_filter_btn_selected)
                alpha = 1.0F
                isEnabled = true
            }
        }

        // Khôi phục giá trị của slider
        val startValueSalary = sharedPreferences.getFloat("slider_start_value_Salary", cusBindingFilter.rangeslider.values[0])
        val endValueSalary = sharedPreferences.getFloat("slider_end_value_Salary", cusBindingFilter.rangeslider.values[1])
        cusBindingFilter.rangeslider.values = listOf(startValueSalary, endValueSalary)

        val startValueWS = sharedPreferences.getFloat("slider_start_value_WS", cusBindingFilter.workshiftSlider.values[0])
        val endValueWS = sharedPreferences.getFloat("slider_end_value_WS", cusBindingFilter.workshiftSlider.values[1])
        cusBindingFilter.workshiftSlider.values = listOf(startValueWS, endValueWS)


        // Khôi phục spinner
        val spinnerPosition = sharedPreferences.getInt("spinner_position", 0)
        cusBindingFilter.workTypeSpinner.setSelection(spinnerPosition)
    }

    // Khi activity ở trạng thái dừng cũng lưu trạng thái filter lại
    override fun onPause() {
        super.onPause()
        saveButtonUIState()
    }

    private fun checkEmptyAdapter(list: List<JobModel>) {
        if (list.isEmpty()) {
            binding.noDataImage.visibility = View.VISIBLE
            binding.animationView.visibility = View.GONE
        } else {
            binding.noDataImage.visibility = View.GONE
            binding.animationView.visibility = View.GONE
        }
    }


}