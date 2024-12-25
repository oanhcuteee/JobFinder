import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.walletHistoryModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData
import java.text.NumberFormat
import java.util.Currency

class WalletHistoryAdapter(private val historyList: List<walletHistoryModel>) :
    RecyclerView.Adapter<WalletHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amountTextView: TextView = itemView.findViewById(R.id.wallet_history_amount)
        val bankNameTextView: TextView = itemView.findViewById(R.id.wallet_history_card_bank)
        val cardNumTextView: TextView= itemView.findViewById(R.id.wallet_history_card_num)
        val dateTextView: TextView = itemView.findViewById(R.id.wallet_history_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_wallet_history, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = historyList[position]

        // Chuyển sang vnd (chỉ hiển thị còn tính toán như bth)
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 0
        format.currency = Currency.getInstance("VND")
        holder.amountTextView.setText(format.format(currentItem.amount?.toDouble()))

        holder.bankNameTextView.text = currentItem.bankName
        holder.cardNumTextView.text= currentItem.cardNum
        holder.dateTextView.text = GetData.getDateFromString(currentItem.date.toString()).toString()
        holder.amountTextView.setTextColor(getHistoryTxtColor(holder.itemView.context, currentItem.type))
    }

    override fun getItemCount() = historyList.size

    private fun getHistoryTxtColor(context: Context, type: String?): Int {
        return when (type) {
            "income" -> ContextCompat.getColor(context, R.color.income_color)
            else -> ContextCompat.getColor(context, R.color.expense_color)
        }
    }
}
