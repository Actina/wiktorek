package pl.gov.mf.mobile.ui.components.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

// https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
@Deprecated("This is just helper for demo view to avoid checking all threading - it should not be used in other views")
class WrapContentLinearLayoutManager @JvmOverloads constructor(
    context: Context
) : LinearLayoutManager(context) {
    //... constructor
    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
        }
    }
}