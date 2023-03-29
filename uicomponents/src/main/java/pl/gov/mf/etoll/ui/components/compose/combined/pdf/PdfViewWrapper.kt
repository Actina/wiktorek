package pl.gov.mf.etoll.ui.components.compose.combined.pdf

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.github.barteksc.pdfviewer.PDFView
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.ui.components.R

class PdfViewWrapper @JvmOverloads constructor(
    context: Context, appMode: AppMode, attrs: AttributeSet? = null,
) : RelativeLayout(context, attrs) {
    init {
        inflate(context,
            when (appMode) {
                AppMode.DARK_MODE -> R.layout.pdf_wrapper_dark
                AppMode.LIGHT_MODE -> R.layout.pdf_wrapper_light
            }, this)
    }

    fun getPdfView(): PDFView = findViewById(R.id.pdfView)
}