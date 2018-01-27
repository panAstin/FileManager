package UI

import android.graphics.Color
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import com.example.filemanager.activities.MainActivity
import com.example.filemanager.R
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.support.v4.drawerLayout
import org.jetbrains.anko.support.v4.viewPager

/**
 * Created by 11046 on 2017/4/16.
 * MainActivity布局
 */
class MainActivityUI:AnkoComponent<MainActivity> {
    override fun createView(ui: AnkoContext<MainActivity>)=with(ui) {
            appBarLayout {
                include<Toolbar>(R.layout.toolbar_layout)

                drawerLayout {
                    id = R.id.dl_left
                    verticalLayout {
                        id = R.id.fragmentcontent
                        tabLayout {
                            id = R.id.tablayout
                            backgroundColor = Color.rgb(48, 70, 155)
                            setSelectedTabIndicatorColor(Color.WHITE)
                            setTabTextColors(Color.GRAY, Color.WHITE)
                        }.lparams(width = matchParent, height = wrapContent)
                        viewPager {
                            id = R.id.viewpager
                        }.lparams(width = matchParent, height = matchParent)
                    }.lparams(width = matchParent, height = matchParent)
                    relativeLayout {
                        backgroundColor = Color.WHITE
                        isClickable = true
                        include<View>(R.layout.server_layout).lparams(width = matchParent, height = wrapContent){
                               bottomMargin = dip(30)
                            }
                    }.lparams(width = dip(360), height = matchParent, gravity = Gravity.START)
                }.lparams(width = matchParent, height = matchParent)
            }
    }
}