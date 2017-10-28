package UI

import android.graphics.Color
import android.support.v7.widget.Toolbar
import com.example.filemanager.activities.MainActivity
import com.example.filemanager.R
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.support.v4.viewPager

/**
 * Created by 11046 on 2017/4/16.
 */
class MainActivityUI:AnkoComponent<MainActivity> {
    override fun createView(ui: AnkoContext<MainActivity>)=with(ui) {
        verticalLayout{
                appBarLayout {
                    include<Toolbar>(R.layout.toolbar_layout)
                    tabLayout {
                        id = R.id.tablayout
                        backgroundColor = Color.rgb(48,70,155)
                        setSelectedTabIndicatorColor(Color.WHITE)
                        setTabTextColors(Color.GRAY,Color.WHITE)
                    }.lparams(width = matchParent, height = wrapContent)
                    viewPager {
                        id = R.id.viewpager
                    }.lparams(width = matchParent, height = matchParent)
                }.lparams(width= matchParent,height = wrapContent)
        }
    }
}