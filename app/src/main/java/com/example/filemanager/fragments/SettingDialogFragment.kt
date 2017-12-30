package com.example.filemanager.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import com.example.filemanager.R
import org.jetbrains.anko.find

/**
 * Created by 11046 on 2017/12/11.
 * 设置弹窗
 */
class SettingDialogFragment:DialogFragment(){
    private var portedit:EditText? = null
    private var synswitch:Switch? = null
    private var modespinner:Spinner? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.serverset_layout,null)
        initSet(view)
        builder.setView(view)
                .setTitle(R.string.action_settings)
                .setPositiveButton("应用", { _, _ ->
                    saveSet()
                }).setNegativeButton("关闭",null)
        return builder.create()
    }

    private fun initSet(view: View){
        modespinner = view.find(R.id.modespinner)
        synswitch = view.find(R.id.synswitch)
        portedit = view.find(R.id.porttxt)

        val preferences = context.getSharedPreferences("ServerSetting",Context.MODE_PRIVATE)
        modespinner?.setSelection(preferences.getInt("mode",0),true)
        synswitch?.isChecked = preferences.getBoolean("synflag",false)
        portedit?.hint = preferences.getInt("port",9090).toString()
    }

    private fun saveSet(){
        val preferences = context.getSharedPreferences("ServerSetting",Context.MODE_PRIVATE)
        val port = portedit!!.text.toString()
        val editor = preferences.edit()
        editor.putBoolean("synflag",synswitch!!.isChecked)
        editor.putInt("mode",modespinner!!.selectedItemPosition)
        if(port!=""){
            editor.putInt("port", port.toInt())
        }
        editor.apply()
    }
}