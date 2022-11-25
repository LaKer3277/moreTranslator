package com.tools.android.translator.ui.adapt

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.tools.android.translator.R
import com.tools.android.translator.server.ConnectServerManager
import com.tools.android.translator.server.ServerBean
import com.tools.android.translator.server.ServerManager
import com.tools.android.translator.support.getServerLogo

class ServerListAdapter(
    private val context: Context,
    private val item:(bean:ServerBean)->Unit
):RecyclerView.Adapter<ServerListAdapter.MyView>() {
    private val list= arrayListOf<ServerBean>()
    init {
        list.add(ServerBean())
        list.addAll(ServerManager.getAllServerList())
    }

    inner class MyView(view:View):RecyclerView.ViewHolder(view){
        val ivLogo=view.findViewById<AppCompatImageView>(R.id.iv_server_logo)
        val tvName=view.findViewById<AppCompatTextView>(R.id.tv_server_name)
        val ivSelector=view.findViewById<AppCompatImageView>(R.id.iv_selector)
        init {
            view.setOnClickListener {
                item.invoke(list[layoutPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyView {
        return MyView(LayoutInflater.from(context).inflate(R.layout.item_server,parent,false))
    }

    override fun onBindViewHolder(holder: MyView, position: Int) {
        val serverBean = list[position]
        holder.ivSelector.isSelected=serverBean.guo==ConnectServerManager.serverBean.guo
        holder.ivLogo.setImageResource(getServerLogo(serverBean))
        holder.tvName.text=if(serverBean.isFast()) serverBean.guo else "${serverBean.guo} - ${serverBean.cheng}"
    }

    override fun getItemCount(): Int = list.size
}