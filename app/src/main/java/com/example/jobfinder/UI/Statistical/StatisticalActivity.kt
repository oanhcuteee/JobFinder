package com.example.jobfinder.UI.Statistical

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.IncomeHandle
import com.example.jobfinder.databinding.ActivityStatisticalBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class StatisticalActivity : AppCompatActivity() {
    lateinit var binding: ActivityStatisticalBinding
    private val viewModel: IncomeViewModel by viewModels()
    private val workHourViewModel: WorkHoursViewModel by viewModels()
    private val uid = GetData.getCurrentUserId()
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
        val incomeList = viewModel.incomeList.value
        incomeList?.let {
            val weeklyTotals = IncomeHandle.calculateWeeklyIncome(incomeList, year, month)
            drawBarChart(weeklyTotals, mapOf())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLineChartNuser(year: Int) {
        val workHourList = workHourViewModel.workHourList.value
        val legend = getString(R.string.Sta_workedHourPMonth_legend)
        val lineChart = binding.NuserlineChart
        workHourList?.let {
            val workHourMap = IncomeHandle.calculateWorkHoursByMonth(workHourList,year)
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
        val thuNhapDataSet = BarDataSet(ColumnThuNhap.ifEmpty { defaultValues }, getString(R.string.Sta_labelBarchart_income)).apply {
            color = ContextCompat.getColor(this@StatisticalActivity, R.color.income_color)
        }
        val chiTieuDataSet = BarDataSet(ColumnChiTieu.ifEmpty { defaultValues }, getString(R.string.Sta_labelBarchart_expenditure)).apply {
            color = ContextCompat.getColor(this@StatisticalActivity, R.color.red)
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



    private fun drawPieChart(totalIncomeByJobType: Map<Int,Double>) {
        val pieEntriesMutableList = mutableListOf<PieEntry>()

        for ((jobType, totalIncome) in totalIncomeByJobType) {
            pieEntriesMutableList.add(PieEntry(totalIncome.toFloat(), GetData.getStringFromJobTypeInt( binding.root.context,jobType)))
        }
        val pieEntries: List<PieEntry> = pieEntriesMutableList.toList()

        // Màu của các trường
        val colors = listOf(
                ContextCompat.getColor(this@StatisticalActivity, R.color.primary_color2),
                ContextCompat.getColor(this@StatisticalActivity, R.color.blue),
                ContextCompat.getColor(this@StatisticalActivity, R.color.green),
                ContextCompat.getColor(this@StatisticalActivity, R.color.ratingStar),
                ContextCompat.getColor(this@StatisticalActivity, R.color.cyan),
                ContextCompat.getColor(this@StatisticalActivity, R.color.red),
                ContextCompat.getColor(this@StatisticalActivity, R.color.purple),
                ContextCompat.getColor(this@StatisticalActivity, R.color.pink),
                ContextCompat.getColor(this@StatisticalActivity, R.color.teal)
        )

        val pieDataSet = PieDataSet(pieEntries, "").apply {
            this.colors = colors
        }

        val pieData = PieData(pieDataSet)

        for ((index, entry) in pieEntries.withIndex()) {
            val legendText = entry.label
            val legendColor = colors[index]

            val textView = TextView(this).apply {
                text = legendText
                setTextColor(Color.BLACK)
                textSize = 15f
                setPadding(8, 8, 8, 0)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val colorView = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    40, // width
                    40 // height
                ).apply {
                    setMargins(0, 0, 8, 0) // Optional: add margins between colorView and textView
                }
                setBackgroundColor(legendColor)
            }

            val legendItemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(colorView)
                addView(textView)
                setPadding(0, 8, 0, 8)
                gravity = Gravity.CENTER_VERTICAL // Center vertically within the LinearLayout
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            binding.legendContainer.addView(legendItemLayout)
        }
        setupAndApplyDataToPieChart(binding.pieChart, pieData)
    }

    private fun setupAndApplyDataToPieChart(chart: PieChart, data: PieData) {
        chart.apply {
            this.data = data
            description.isEnabled = false                       // Ẩn mô tả của biểu đồ
            isDrawHoleEnabled = true                            // Cho phép vẽ lỗ ở giữa biểu đồ
            holeRadius = 40f                                    // Đặt bán kính của lỗ ở giữa
            setEntryLabelColor(getColor(R.color.white))         // Đặt màu sắc cho nhãn của các phần
            animateY(1000)                          // Thêm animation khi hiển thị biểu đồ
            setDrawEntryLabels(false)

            // Chỉnh chú thích (legend)
            legend.apply {
                isEnabled = false                                                // Hiển thị chú thích
                textColor = getColor(R.color.black)
                textSize = 15f
                setXEntrySpace(15f)                                             // Khoảng cách giữa các legend, trục X
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM       // Căn chú thích theo chiều dọc
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER   // Căn chú thích theo chiều ngang
                form = Legend.LegendForm.CIRCLE // Đổi hình dạng của chú thích thành chấm tròn
            }

            setupValueFormatter(data, getColor(R.color.white), 14f) // Verify định dạng value
            invalidate()                                        // Cập nhật cài đặt ở trên và áp dụng để vẽ chart
        }
        // chỉnh cỡ chữ và màu của data
        data.apply {
            setValueTextSize(14f)
            setValueTextColor(Color.WHITE)
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
            color = ContextCompat.getColor(this@StatisticalActivity, R.color.primary_color2)
            valueTextColor = ContextCompat.getColor(this@StatisticalActivity, R.color.black)
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(this@StatisticalActivity, R.color.filter_color)
        }

        val data = LineData(lineDataSet)
        setupAndApplyDataToLineChart(chart, data)
    }

    private fun setupAndApplyDataToLineChart(chart: LineChart, data: LineData) {
        chart.apply {
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
                textColor = ContextCompat.getColor(this@StatisticalActivity, R.color.black)
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
                textColor = ContextCompat.getColor(this@StatisticalActivity, R.color.black)
                setDrawGridLines(true)
            }
            // Chú thích
            legend.apply {
                setExtraOffsets(0f,0f,0f,15f)
                xEntrySpace = 30f
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM       // Căn chiều dọc
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT    // Căn chiều ngang
                textColor = ContextCompat.getColor(this@StatisticalActivity, R.color.black)
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
        val lineChartTitle = getString(R.string.Sta_workingHourPMonth)
        val legend = getString(R.string.Sta_workedHourPMonth_legend)
        val lineChart = binding.NuserlineChart

        val todayString = GetData.getDateFromString(today)

        val todayDate = LocalDate.parse(todayString, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        showDateDialog(todayDate)

        binding.NuserLineChartWrap.visibility = View.VISIBLE
        binding.NuserlineChartTitle.text = lineChartTitle

        viewModel.fetchIncome(uid.toString())
        defaltChart(todayDate, legend, lineChart)

        viewModel.incomeList.observe(this){ newIncomeList->
            val weeklyTotals = IncomeHandle.calculateWeeklyIncome(
                newIncomeList,
                todayDate.year,
                todayDate.monthValue
            )
            drawBarChart(weeklyTotals, mapOf())
        }

        viewModel.fetchIncomeByJobTypeId(uid.toString())

        viewModel.incomeByJobTypeList.observe(this){ newIncomeListByJobType->
            val totalIncomeByJobType = IncomeHandle.calculateIncomeByJobType(newIncomeListByJobType)
            drawPieChart(totalIncomeByJobType)
        }

        workHourViewModel.fetchWorkHour(uid.toString())

        workHourViewModel.workHourList.observe(this){newWorHourList ->
            val workHourMap = IncomeHandle.calculateWorkHoursByMonth(newWorHourList, todayDate.year)
            drawLineChart(legend, lineChart, workHourMap)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDateDialog(todayDate : LocalDate ){
        val monthYearText = "Tháng ${todayDate.monthValue}/${todayDate.year}"
        val yearText = "${todayDate.year}"

        binding.selectMonthYearBtn.text = monthYearText
        binding.NuserselectYearBtn.text = yearText
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun defaltChart(todayDate:LocalDate, legend:String, lineChart:LineChart){
        val weeklyTotals = IncomeHandle.calculateWeeklyIncome(
            mutableListOf(),
            todayDate.year,
            todayDate.monthValue
        )
        drawBarChart(weeklyTotals, mapOf())


        val totalIncomeByJobType = IncomeHandle.calculateIncomeByJobType(mutableListOf())
        drawPieChart(totalIncomeByJobType)

        val workHourMap = IncomeHandle.calculateWorkHoursByMonth(mutableListOf(), todayDate.year)
        drawLineChart(legend, lineChart, workHourMap)

    }

}