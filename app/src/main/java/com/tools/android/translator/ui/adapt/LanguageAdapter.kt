package com.tools.android.translator.ui.adapt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tools.android.translator.App
import com.tools.android.translator.R
import com.tools.android.translator.translate.Language

/**
 * Created on 2022/4/24
 * Describe:
 */
class LanguageAdapter(
    var isSource: Boolean,
    var list: ArrayList<Language>,
    private var iLangChoice: ILangChoice?,
    private val isRecently: Boolean = false
) : RecyclerView.Adapter<LanguageAdapter.VH>() {

    companion object {
        var sourceLa = Language(App.ins.sourceLa)
        var targetLa = Language(App.ins.targetLa)
    }

    interface ILangChoice {
        fun onChoice(language: Language)
    }

    inner class VH(item: View): RecyclerView.ViewHolder(item) {
        val tvName: TextView = item.findViewById(R.id.tv_la)
        val ivStatus: ImageView = item.findViewById(R.id.iv_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val language = list[position]
        holder.tvName.text = language.displayName
        holder.ivStatus.setOnClickListener {
            iLangChoice?.onChoice(language)
        }
        if (isRecently) {
            holder.ivStatus.visibility = View.GONE
            return
        }
        holder.ivStatus.visibility = View.VISIBLE
        if (isSource && sourceLa == language) {
            holder.ivStatus.setImageResource(R.mipmap.status_select)
        } else if (!isSource && targetLa == language) {
            holder.ivStatus.setImageResource(R.mipmap.status_select)
        } else {
            holder.ivStatus.setImageResource(when(language.available) {
                -1 -> R.mipmap.status_download
                0 -> R.mipmap.status_downloading
                else -> R.mipmap.status_delete
            })
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}