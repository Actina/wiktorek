package pl.gov.mf.etoll.front.shared.adapter

import androidx.appcompat.widget.LinearLayoutCompat
import pl.gov.mf.etoll.front.shared.BaseCard

class CardsAdapter {

    val cards: List<BaseCard<*>>
        get() = _cards

    private val _cards = mutableListOf<BaseCard<*>>()

    fun addCard(card: BaseCard<*>) {
        _cards.add(card)
    }

    fun generateView(root: LinearLayoutCompat) {
        // clear
        root.removeAllViews()
        // add each view
        cards.forEach {
            root.addView(it.bind(root.context, root))
        }
    }

    fun clear() {
        _cards.clear()
    }
}