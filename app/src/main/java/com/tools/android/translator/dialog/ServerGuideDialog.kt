package com.tools.android.translator.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import com.tools.android.translator.R
import com.tools.android.translator.support.RemoteConfig
import com.tools.android.translator.support.setPoint
import com.tools.android.translator.ui.server.ConnectServerActivity

class ServerGuideDialog: DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_server_guide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCancelable(false)
        setPoint.point("tan_itranslator")
        view.findViewById<AppCompatTextView>(R.id.tv_connect).setOnClickListener {
            setPoint.point("itr_noti_conn_click")
            RemoteConfig.ins.isShowingGuideDialog=false
            startActivity(Intent(requireContext(),ConnectServerActivity::class.java).apply {
                putExtra("auto",true)
            })
            dismiss()
        }
        view.findViewById<AppCompatImageView>(R.id.iv_cancel).setOnClickListener {
            RemoteConfig.ins.isShowingGuideDialog=false

            setPoint.point("itr_noti_close_click")
            dismiss()
        }
    }
}