package pl.gov.mf.etoll.front.shared

import android.content.Context
import android.view.View
import android.view.ViewGroup

abstract class BaseCard<MODEL>(val viewModel: MODEL) {

    abstract fun bind(context: Context, root: ViewGroup): View

    open fun refresh() {
        // custom implementation if required
    }
}