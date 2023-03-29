package pl.gov.mf.mobile.ui.components.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.view.menu.MenuItemImpl
import com.google.android.material.bottomnavigation.BottomNavigationView
import pl.gov.mf.mobile.utils.translate
import java.lang.Boolean
import java.lang.reflect.InvocationTargetException

class CustomBottomNavigationView : BottomNavigationView {

    private val translationMenuStrings = mutableListOf<String>()

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    )

    fun translate() {
        if (translationMenuStrings.size == 0) {
            for (i in 0 until menu.size()) {
                translationMenuStrings.add(menu.getItem(i).title.toString())
            }
        }
        for (i in 0 until menu.size()) {
            menu.getItem(i).title = translationMenuStrings[i].translate(context)
        }
    }

    fun setSelected(index: Int) {
        try {
            // as BottomNavigationView does not yet support custom selecting of active item, we needed to use reflections
            val method =
                MenuItemImpl::class.java.getDeclaredMethod("setExclusiveCheckable", Boolean.TYPE)
            method.isAccessible = true
            for (i in 0 until menu.size()) {
                method.invoke(menu.getItem(i) as MenuItemImpl, false)
                menu.getItem(i).isChecked = false
            }
            method.invoke(menu.getItem(index) as MenuItemImpl, true)
            menu.getItem(index).isChecked = true
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}