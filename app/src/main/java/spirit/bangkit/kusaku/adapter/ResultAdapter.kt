package spirit.bangkit.kusaku.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import spirit.bangkit.kusaku.databinding.ItemListviewBinding

class ResultAdapter(private val list: Map<String?, Int>) : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {

    inner class ResultViewHolder(val binding: ItemListviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = ItemListviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, pos: Int) {
        with(holder.binding) {
            tvLabel.text = list.keys.elementAt(pos)
            tvCount.text = list.values.elementAt(pos).toString()
        }
    }

    override fun getItemCount(): Int = list.size

}