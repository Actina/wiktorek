package pl.gov.mf.mobile.ui.components.views.bubblewidget

enum class ConnectionState {
    OK,
    NOT_OK;

    fun ConnectionState.toBoolean(): Boolean = this == OK
}