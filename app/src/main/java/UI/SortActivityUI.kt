package UI

import android.support.v7.widget.Toolbar
import com.example.filemanager.activities.SortActivity
import com.example.filemanager.R
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView

/**
 * Created by 11046 on 2017/9/22.
 */
class SortActivityUI:AnkoComponent<SortActivity>{
    override fun createView(ui: AnkoContext<SortActivity>)= with(ui) {
        verticalLayout{
            appBarLayout {
                include<Toolbar>(R.layout.toolbar_layout)
                recyclerView {
                    id = R.id.filelist
                }.lparams(width = matchParent, height = matchParent)
            }.lparams(width= matchParent,height = wrapContent)
        }
    }

}