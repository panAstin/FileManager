package com.example.filemanager.utils

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import com.example.filemanager.R

import com.example.filemanager.fragments.CommonDialogFragment
import java.util.Calendar

/**
 * Created by 11046 on 2018/2/4.
 * dialogfragment辅助类
 */

object DialogFragmentHelper {

    private const val DIALOG_POSITIVE = "确定"
    private const val DIALOG_NEGATIVE = "取消"

    private val TAG_HEAD = DialogFragmentHelper::class.java.simpleName
    private const val THEMES = R.style.Base_AlertDialog

    /**
     * 加载中的弹出窗
     */
    private val PROGRESS_TAG = TAG_HEAD + ":progress"

    @JvmOverloads
    fun showProgress(fragmentManager: FragmentManager, message: String, cancelable: Boolean = true,
                     cancelListener: CommonDialogFragment.OnDialogCancelListener? = null): CommonDialogFragment {

        val dialogFragment = CommonDialogFragment.newInstance(object : CommonDialogFragment.OnCallDialog {
            override fun getDialog(context: Context): Dialog {
                val progressDialog = ProgressDialog(context, THEMES)
                progressDialog.setMessage(message)
                return progressDialog
            }
        }, cancelable, cancelListener)
        dialogFragment.show(fragmentManager, PROGRESS_TAG)
        return dialogFragment
    }

    /**
     * 简单提示弹出窗
     */
    private val TIPS_TAG = TAG_HEAD + ":tips"

    @JvmOverloads
    fun showTips(fragmentManager: FragmentManager, message: String, cancelable: Boolean = true,
                 cancelListener: CommonDialogFragment.OnDialogCancelListener? = null) {

        val dialogFragment = CommonDialogFragment.newInstance(object : CommonDialogFragment.OnCallDialog {
            override fun getDialog(context: Context): Dialog {
                val builder = AlertDialog.Builder(context, THEMES)
                builder.setMessage(message)
                return builder.create()
            }
        }, cancelable, cancelListener)
        dialogFragment.show(fragmentManager, TIPS_TAG)
    }

    /**
     * 确定取消框
     */
    private val CONfIRM_TAG = TAG_HEAD + ":confirm"

    fun showConfirmDialog(fragmentManager: FragmentManager, message: String,
                          listener: IDialogResultListener<Int>?, cancelable: Boolean,
                          cancelListener: CommonDialogFragment.OnDialogCancelListener) {
        val dialogFragment = CommonDialogFragment.newInstance(object : CommonDialogFragment.OnCallDialog {
            override fun getDialog(context: Context): Dialog {
                val builder = AlertDialog.Builder(context, THEMES)
                builder.setMessage(message)
                builder.setPositiveButton(DIALOG_POSITIVE) { _, which ->
                    listener?.onDataResult(which)
                }
                builder.setNegativeButton(DIALOG_NEGATIVE) { _, which ->
                    listener?.onDataResult(which)
                }
                return builder.create()
            }
        }, cancelable, cancelListener)
        dialogFragment.show(fragmentManager, CONfIRM_TAG)

    }

    /**
     * 带列表的弹出窗
     */
    private val LIST_TAG = TAG_HEAD + ":list"

    fun showListDialog(fragmentManager: FragmentManager, title: String, items: Array<String>,
                       resultListener: IDialogResultListener<Int>?, cancelable: Boolean): DialogFragment? {
        val dialogFragment = CommonDialogFragment.newInstance(object : CommonDialogFragment.OnCallDialog {
            override fun getDialog(context: Context): Dialog {
                val builder = AlertDialog.Builder(context, THEMES)
                builder.setTitle(title)
                builder.setItems(items) { _, which ->
                    resultListener?.onDataResult(which)
                }
                return builder.create()
            }
        }, cancelable, null)
        dialogFragment.show(fragmentManager, LIST_TAG)
        return null
    }

    /**
     * 选择日期
     */
    private val DATE_TAG = TAG_HEAD + ":date"

    fun showDateDialog(fragmentManager: FragmentManager, title: String, calendar: Calendar,
                       resultListener: IDialogResultListener<Calendar>, cancelable: Boolean): DialogFragment? {
        val dialogFragment = CommonDialogFragment.newInstance(object : CommonDialogFragment.OnCallDialog {
            override fun getDialog(context: Context): Dialog {
                val datePickerDialog = DatePickerDialog(context, THEMES, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    resultListener.onDataResult(calendar)
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

                datePickerDialog.setTitle(title)
                datePickerDialog.setOnShowListener {
                    datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).text = DIALOG_POSITIVE
                    datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).text = DIALOG_NEGATIVE
                }
                return datePickerDialog

            }
        }, cancelable, null)
        dialogFragment.show(fragmentManager, DATE_TAG)
        return null
    }

    /**
     * 选择时间
     */
    private val TIME_TAG = TAG_HEAD + ":time"

    fun showTimeDialog(manager: FragmentManager, title: String, calendar: Calendar,
                       resultListener: IDialogResultListener<Calendar>?, cancelable: Boolean) {
        val dialogFragment = CommonDialogFragment.newInstance(object : CommonDialogFragment.OnCallDialog {
            override fun getDialog(context: Context): Dialog {
                val dateDialog = TimePickerDialog(context, THEMES, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    if (resultListener != null) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        resultListener.onDataResult(calendar)
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

                dateDialog.setTitle(title)
                dateDialog.setOnShowListener {
                    dateDialog.getButton(DialogInterface.BUTTON_POSITIVE).text = DIALOG_POSITIVE
                    dateDialog.getButton(DialogInterface.BUTTON_NEGATIVE).text = DIALOG_NEGATIVE
                }

                return dateDialog
            }
        }, cancelable, null)
        dialogFragment.show(manager, DATE_TAG)
    }

    /**
     * 带输入框的弹出窗
     */
    private val INSERT_TAG = TAG_HEAD + ":insert"

    @JvmOverloads
    fun showInsertDialog(manager: FragmentManager, title: String,text: String = "",
                         resultListener: IDialogResultListener<String>?, cancelable: Boolean) {

        val dialogFragment = CommonDialogFragment.newInstance(object : CommonDialogFragment.OnCallDialog {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            override fun getDialog(context: Context): Dialog {
                val editText = EditText(context)
                editText.background = null
                editText.setPadding(60, 40, 0, 0)
                editText.setText(text)
                val builder = AlertDialog.Builder(context, THEMES)
                builder.setTitle(title)
                builder.setView(editText)
                builder.setPositiveButton(DIALOG_POSITIVE) { _, _ ->
                    resultListener?.onDataResult(editText.text.toString())
                }
                builder.setNegativeButton(DIALOG_NEGATIVE, null)
                return builder.create()

            }
        }, cancelable, null)
        dialogFragment.show(manager, INSERT_TAG)

    }

    /**
     * 带输入密码框的弹出窗
     */
    private val PASSWORD_INSERT_TAG = TAG_HEAD + ":insert"

    fun showPasswordInsertDialog(manager: FragmentManager, title: String,
                                 resultListener: IDialogResultListener<String>?, cancelable: Boolean) {
        val dialogFragment = CommonDialogFragment.newInstance(object : CommonDialogFragment.OnCallDialog {
            override fun getDialog(context: Context): Dialog {
                val editText = EditText(context)
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                editText.isEnabled = true
                val builder = AlertDialog.Builder(context, THEMES)
                builder.setTitle(title)
                builder.setView(editText)
                builder.setPositiveButton(DIALOG_POSITIVE) { _, _ ->
                    resultListener?.onDataResult(editText.text.toString())
                }
                builder.setNegativeButton(DIALOG_NEGATIVE, null)
                return builder.create()
            }
        }, cancelable, null)
        dialogFragment.show(manager, PASSWORD_INSERT_TAG)
    }

    /**
     * 两个输入框的弹出窗
     */
    private val INTERVAL_INSERT_TAG = TAG_HEAD + ":interval_insert"

    fun showIntervalInsertDialog(manager: FragmentManager, title: String,
                                 resultListener: IDialogResultListener<Array<String>>?, cancelable: Boolean) {
        val dialogFragment = CommonDialogFragment.newInstance(object : CommonDialogFragment.OnCallDialog {
            override fun getDialog(context: Context): Dialog {
                val view = LayoutInflater.from(context).inflate(R.layout.dialog_interval_insert, null)
                val minEditText = view.findViewById(R.id.interval_insert_et_min) as EditText
                val maxEditText = view.findViewById(R.id.interval_insert_et_max) as EditText
                val builder = AlertDialog.Builder(context, THEMES)
                return builder.setTitle(title)
                        .setView(view)
                        .setPositiveButton(DIALOG_POSITIVE, { _, _ ->
                            resultListener?.onDataResult(arrayOf(minEditText.text.toString(), maxEditText.text.toString()))
                        }).setNegativeButton(DIALOG_NEGATIVE, null)
                        .create()
            }
        }, cancelable, null)
        dialogFragment.show(manager, INTERVAL_INSERT_TAG)
    }

}

















