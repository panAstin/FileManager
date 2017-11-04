package com.example.filemanager.adapters

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.example.filemanager.FileBean
import com.example.filemanager.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.io.File
import kotlin.collections.ArrayList

internal class fmAdapter//参数初始化
(context: Context) : RecyclerView.Adapter<fmAdapter.myViewHolder>() {
    private val inflater: LayoutInflater
    private var fileBeans: ArrayList<FileBean> = ArrayList()    //存储文件信息
    companion object {
        var isSelectd: ArrayMap<Int,Boolean>? =null   // 存储CheckBox选择信息
        var selectFlag = 0          //选择模式标识
    }
    private val animation: Animation

    init {
        isSelectd = ArrayMap()
        inflater = LayoutInflater.from(context)
        animation = AnimationUtils.loadAnimation(context, R.anim.list_anim)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= myViewHolder(inflater.inflate(R.layout.file, parent, false))

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val filebean = fileBeans[position]
        inititem(holder,filebean)
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() =  fileBeans.size

    class myViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal val name: TextView = view.findViewById(R.id.textView) as TextView
        internal val size: TextView = view.findViewById(R.id.sizeView) as TextView
        val image: ImageView = view.findViewById(R.id.imageView) as ImageView
        internal val date: TextView = view.findViewById(R.id.filedate) as TextView
        val cb : CheckBox = view.findViewById(R.id.checkBox) as CheckBox
    }

    private fun inititem(holder: myViewHolder,fileBean: FileBean) {
        val i = fileBeans.indexOf(fileBean)
        holder.name.text = fileBean.getFile().name
        holder.size.text = fileBean.getSize()
        holder.date.text = fileBean.getDate()
        if(fileBean.getIcon()==null){
            Picasso.with(inflater.context)          //使用Picasso生成图片缩略图
                    .load(File(fileBean.getFile().path))
                    .config(Bitmap.Config.RGB_565)
                    .transform(getTransformation(holder.image))
                    .into(holder.image)
        }else{
            holder.image.setImageBitmap(fileBean.getIcon())
        }
        holder.cb.tag = i
        if (selectFlag >0){
            holder.cb.visibility = View.VISIBLE
            holder.cb.isChecked = isSelectd!![i]!!
        }else{
            holder.cb.visibility = View.GONE
        }
    }

    fun setListData(fileBeans: ArrayList<FileBean>){
        this.fileBeans = fileBeans
        this.notifyDataSetChanged()
    }

    fun changeSelecFlag(){
        selectFlag = when{
            selectFlag > 0 -> 0
            else ->{
                initIsSelectd()
                1
            }
        }
        notifyDataSetChanged()
    }

    fun initIsSelectd(){
        isSelectd!!.clear()
        var i = 0
        do {
            isSelectd!!.put(i,false)
            i++
        }while (i<fileBeans.size)
    }

    fun getSelectCount():Int{
        var count = 0
        for (isselect in isSelectd!!){
            if (isselect.value){
                count++
            }
        }
        return count
    }

    fun addItem(fileBean: FileBean) {
        fileBeans.add(fileBean)
        val position = fileBeans.indexOf(fileBean)
        notifyItemInserted(position)
    }

    fun removeItem(position: Int) {
        fileBeans.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun getTransformation(view: ImageView): Transformation {
        return object : Transformation {
            override fun key(): String ="transformation" + " desiredWidth"

            override fun transform(source: Bitmap): Bitmap {
                val targetWidth = view.width

                //返回原图
                if (source.width == 0 || source.width < targetWidth) {
                    return source
                }

                //如果图片大小大于等于设置的宽度，则按照设置的宽度比例来缩放
                val aspectRatio = source.height.toDouble() / source.width.toDouble()
                val targetHeight = (targetWidth * aspectRatio).toInt()
                if (targetHeight == 0 || targetWidth == 0) {
                    return source
                }
                val result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
                if (result != source) {
                    // Same bitmap is returned if sizes are the same
                    source.recycle()
                }
                return result
            }
        }
    }

}
