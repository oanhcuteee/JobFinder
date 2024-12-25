package com.example.jobfinder.UI.Admin.Statistical

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.jobfinder.R
import com.example.jobfinder.UI.Statistical.MonthYearPickerDialog
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.IncomeHandle
import com.example.jobfinder.databinding.ActivityStatisticalBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class AdminStatisticalActivity : AppCompatActivity() {
    lateinit var binding: ActivityStatisticalBinding
    private val userCountViewModel: AdminUserCountViewModel by viewModels()
    private val incomeVM:AdminIncomeViewModel by viewModels()
    private val today = GetData.getCurrentDateTime()

    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticalBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // nút back về
        binding.backbtn.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.pieChartWrap.visibility = View.GONE

        drawChartNuser()

        // Dialog Barchart
        binding.selectMonthYearBtn.setOnClickListener {
            val monthYearPickerDialog = MonthYearPickerDialog(selectedMonth, selectedYear)
            monthYearPickerDialog.setListener { month, year ->
                selectedMonth = month
                selectedYear = year
                val monthYearText = "Tháng $month /$year"
                binding.selectMonthYearBtn.text = monthYearText
                updateBarChartMonYea(month, year)
            }
            monthYearPickerDialog.show(supportFragmentManager, "MonthYearPickerDialog")
        }

        // Dialog Line chart
        binding.NuserselectYearBtn.setOnClickListener {
            val monthYearPickerDialog = MonthYearPickerDialog(selectedMonth, selectedYear, isYearOnly = true)
            monthYearPickerDialog.setListener { _, year ->
                selectedYear = year
                val yearText = "$year"
                binding.NuserselectYearBtn.text = yearText
                updateLineChartNuser(year)
            }
            monthYearPickerDialog.show(supportFragmentManager, "MonthYearPickerDialog")
        }

    }
    private fun updateBarChartMonYea(month: Int, year: Int) {
        val registeredUserLists = userCountViewModel.registeredUserLists.value
        registeredUserLists?.let {
            val bUserList = it.bUserList
            val nUserList = it.nUserList

            val weeklyBUserTotals = IncomeHandle.calculateWeeklyRegisteredUser(
                bUserList,
                year,
                month
            )

            val weeklyNUserTotals = IncomeHandle.calculateWeeklyRegisteredUser(
                nUserList,
                year,
                month
            )

            drawBarChart(weeklyBUserTotals, weeklyNUserTotals)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLineChartNuser(year: Int) {
        val incomeList = incomeVM.incomeList.value
        val legend = getString(R.string.Sta_workedHourPMonth_legend)
        val lineChart = binding.NuserlineChart
        incomeList?.let {
            val workHourMap = IncomeHandle.calculateAdminIncomeByMonth(incomeList,year)
            drawLineChart(legend, lineChart, workHourMap)
//            }
        }
    }


    private fun drawBarChart(weekAndTotalIncome: Map<Int,Double>, weekAndTotalExpense: Map<Int, Double>) {
        val mutableColumnThuNhap = mutableListOf<BarEntry>()
        val mutableColumnChiTieu = mutableListOf<BarEntry>()

        var positionX = 0.5f

        // Duyệt qua weekAndTotal và thêm BarEntry vào mutableColumnThuNhap
        weekAndTotalIncome.forEach { (_, total) ->
            mutableColumnThuNhap.add(BarEntry(positionX, total.toFloat()))
            positionX += 1.0f // Tăng vị trí x thêm 1.0 sau mỗi tuần
        }

        // Chuyển đổi MutableList thành List
        val ColumnThuNhap: List<BarEntry> = mutableColumnThuNhap.toList()

        var positionXExpense = 0.5f

        weekAndTotalExpense.forEach { (_, total) ->
            mutableColumnChiTieu.add(BarEntry(positionXExpense, total.toFloat()))
            positionXExpense += 1.0f // Increase x position by 1.0 after each week
        }

        // Chuyển đổi MutableList thành List
        val ColumnChiTieu: List<BarEntry> = mutableColumnChiTieu.toList()


        val weeks = (1..(weekAndTotalIncome.keys + weekAndTotalExpense.keys).maxOrNull()!!).map { "${getText(R.string.week)} $it" }.toTypedArray()

        // Không có data thì đây là giá trị mặc định
        val defaultValues = weeks.indices.map { BarEntry(it + 0.5f, 0f) }

        // Gán giá trị default và đổi màu cột
        val thuNhapDataSet = BarDataSet(ColumnThuNhap.ifEmpty { defaultValues }, getString(R.string.buser)).apply {
            color = ContextCompat.getColor(this@AdminStatisticalActivity, R.color.primary_color3)
        }
        val chiTieuDataSet = BarDataSet(ColumnChiTieu.ifEmpty { defaultValues }, getString(R.string.nuser)).apply {
            color = ContextCompat.getColor(this@AdminStatisticalActivity, R.color.primary_color4)
        }

        val data = BarData(thuNhapDataSet, chiTieuDataSet)
        SetupAndApplyDataToBarChart(binding.barChart, data, weeks)
    }

    private fun SetupAndApplyDataToBarChart(chart: BarChart, data: ChartData<*>, weeks: Array<String>) {
        chart.apply {
            this.data = data as BarData?

            setupValueFormatter(data, getColor(R.color.black), 10f) // Đặt format value hiển thị ở mỗi cột trên biểu đồ
            val groupSpace = 0.2f                              // Khoảng cách giữa các nhóm (tuần 1, tuần 2,...)
            val barSpace = 0.1f                                // Khoảng cách giữa các cột trong 1 nhóm (chi tiêu, thu nhập)
            val firstPosition = 0.5f                           // Vị trí đặt nhóm đầu tiên (tuần 1)
            data.barWidth = 0.3f                               // độ rộng của 1 cột trong 1 nhóm

            groupBars(firstPosition, groupSpace, barSpace)
            description.isEnabled = false     // Không cần tiêu đề
            setDrawGridBackground(true)       // Vẽ nền lưới sau cột
            setPinchZoom(true)                // Cho phép zoom chart
            animateY(1000)        // Độ trễ cho animation
            axisRight.isEnabled = false       // Ẩn trục phải của chart

            // Chú thích
            legend.apply {
                setExtraOffsets(0f,0f,0f,15f)
                form = Legend.LegendForm.LINE
                xEntrySpace = 30f       // Khoảng cách của legend trục X
                textSize = 14f
            }
            // Trục X
            xAxis.apply {
                granularity = 1f                               // Khoảng cách giữa các điểm trên trục
                setDrawGridLines(false)                        // không kẻ lưới cho trục X
                textColor = getColor(R.color.black)
                position = XAxis.XAxisPosition.BOTTOM           // Vị trí đặt trục X
                axisMinimum = 0.5f                              // Giá trị tối thiểu trục X
                axisMaximum = weeks.size.toFloat() + 0.5f       // Giá trị tối đa trục X
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        // Chỉ định vị trí của các nhãn trục X
                        return if (value >= 0.5 && value < weeks.size + 0.5) weeks[(value - 0.5).toInt()] else ""
                    }
                }
            }
            // Trục Y
            axisLeft.apply {
                setDrawGridLines(true)
                textColor = getColor(R.color.black)
            }

            invalidate() // Cập nhật cài đặt ở trên và áp dụng để vẽ chart
        }
    }

    private fun drawLineChart(label: String, chart: LineChart, workHourMap:Map<Int,Double>) {
        // Dữ liệu mẫu cho LineChart
        val LineEntries = mutableListOf<Entry>()

        for ((dayOfMonth, hours) in workHourMap) {
            val entry = Entry(dayOfMonth.toFloat(), hours.toFloat())
            LineEntries.add(entry)
        }
        val lineEntries: List<Entry> =  LineEntries.toList()

        val lineDataSet = LineDataSet(lineEntries, label).apply {
            color = ContextCompat.getColor(this@AdminStatisticalActivity, R.color.primary_color2)
            valueTextColor = ContextCompat.getColor(this@AdminStatisticalActivity, R.color.black)
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(this@AdminStatisticalActivity, R.color.filter_color)
        }

        val data = LineData(lineDataSet)
        setupAndApplyDataToLineChart(chart, data)
    }

    private fun setupAndApplyDataToLineChart(chart: LineChart, data: LineData) {
        chart.apply {
            setupValueFormatter(data, getColor(R.color.black), 10f)
            this.data = data
            description.isEnabled = false
            setDrawGridBackground(true)
            setPinchZoom(true)
            animateY(1000)
            axisRight.isEnabled = false

            // Trục x
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                labelCount = 12
                textColor = ContextCompat.getColor(this@AdminStatisticalActivity, R.color.black)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val monthsArray = context.resources.getStringArray(R.array.months_array)
                        return when(value.toInt()){
                            1 -> monthsArray[0]
                            2 -> monthsArray[1]
                            3 -> monthsArray[2]
                            4 -> monthsArray[3]
                            5 -> monthsArray[4]
                            6 -> monthsArray[5]
                            7 -> monthsArray[6]
                            8 -> monthsArray[7]
                            9 -> monthsArray[8]
                            10 -> monthsArray[9]
                            11 -> monthsArray[10]
                            12 -> monthsArray[11]
                            else -> monthsArray[11]
                        }
                    }
                }
            }
            // Trục y
            axisLeft.apply {
                textColor = ContextCompat.getColor(this@AdminStatisticalActivity, R.color.black)
                setDrawGridLines(true)
            }
            // Chú thích
            legend.apply {
                setExtraOffsets(0f,0f,0f,15f)
                xEntrySpace = 30f
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM       // Căn chiều dọc
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT    // Căn chiều ngang
                textColor = ContextCompat.getColor(this@AdminStatisticalActivity, R.color.black)
                textSize = 14f
            }

            invalidate()
        }
    }


    private fun setupValueFormatter(data: ChartData<*>, valueTextColor: Int, valueTextSize: Float) {
        val formatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when {
                    value >= 1000000 -> "${"%.1f".format(value / 1000000)}M"
                    value >= 1000 -> "${"%.0f".format(value / 1000)}K"
                    else -> "%.0f".format(value)
                }
            }
        }
        for (set in data.dataSets) {
            set.valueFormatter = formatter
            set.valueTextColor = valueTextColor
            set.valueTextSize = valueTextSize
        }
    }



    @SuppressLint("NewApi")
    private fun drawChartNuser() {
        binding.BarchatTitle.text = getString(R.string.registered_amount)
        val lineChartTitle = getString(R.string.admin_income)
        val legend = getString(R.string.unit)
        val lineChart = binding.NuserlineChart

        val todayString = GetData.getDateFromString(today)

        val todayDate = LocalDate.parse(todayString, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        showDateDialog(todayDate)

        binding.NuserLineChartWrap.visibility = View.VISIBLE
        binding.NuserlineChartTitle.text = lineChartTitle

        userCountViewModel.fetchRegisteredBUser()
        userCountViewModel.fetchRegisteredNUser()

        userCountViewModel.registeredUserLists.observe(this) { registeredUserLists ->
            registeredUserLists?.let {
                val bUserList = it.bUserList
                val nUserList = it.nUserList

                val weeklyBUserTotals = IncomeHandle.calculateWeeklyRegisteredUser(
                    bUserList,
                    todayDate.year,
                    todayDate.monthValue
                )

                val weeklyNUserTotals = IncomeHandle.calculateWeeklyRegisteredUser(
                    nUserList,
                    todayDate.year,
                    todayDate.monthValue
                )

                drawBarChart(weeklyBUserTotals, weeklyNUserTotals)

            }
        }

        incomeVM.fetchIncome()

        incomeVM.incomeList.observe(this){ incomeList->

            val workHourMap = IncomeHandle.calculateAdminIncomeByMonth(incomeList, todayDate.year)
            drawLineChart(legend, lineChart, workHourMap)
        }

        defaltChart(todayDate, legend, lineChart)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDateDialog(todayDate : LocalDate){
        val monthYearText = "Tháng ${todayDate.monthValue}/${todayDate.year}"
        val yearText = "${todayDate.year}"

        binding.selectMonthYearBtn.text = monthYearText
        binding.NuserselectYearBtn.text = yearText
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun defaltChart(todayDate: LocalDate, legend:String, lineChart: LineChart){
        val weeklyTotals = IncomeHandle.calculateWeeklyIncome(
            mutableListOf(),
            todayDate.year,
            todayDate.monthValue
        )
        drawBarChart(weeklyTotals, mapOf())

        val workHourMap = IncomeHandle.calculateWorkHoursByMonth(mutableListOf(), todayDate.year)
        drawLineChart(legend, lineChart, workHourMap)

    }

}