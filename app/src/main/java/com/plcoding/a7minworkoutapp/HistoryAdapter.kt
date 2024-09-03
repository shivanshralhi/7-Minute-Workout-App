package com.plcoding.a7minworkoutapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.plcoding.a7minworkoutapp.databinding.ItemHistoryLayoutBinding

class HistoryAdapter(private val item : ArrayList<String>):RecyclerView.Adapter<HistoryAdapter.ViewHolder>(){

    class ViewHolder(binding : ItemHistoryLayoutBinding):RecyclerView.ViewHolder(binding.root){
        val llHistoryItemMain = binding.llHistoryItemMain
        val tvItem = binding.tvItem
        val tvPosition = binding.tvPosition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHistoryLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    }

    override fun getItemCount(): Int {
        return item.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date :String = item.get(position)
        holder.tvPosition.text = (position+1).toString()
        holder.tvItem.text = date
        if(position%2 == 0) {
            holder.llHistoryItemMain.setBackgroundColor(Color.parseColor("#EBEBEB"))
        }else{
            holder.llHistoryItemMain.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }
    }
}