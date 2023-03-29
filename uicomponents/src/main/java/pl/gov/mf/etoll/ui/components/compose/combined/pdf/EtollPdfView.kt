package pl.gov.mf.etoll.ui.components.compose.combined.pdf

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.util.FitPolicy
import pl.gov.mf.mobile.utils.getAppMode
import java.io.File

@Composable
fun EbiletPdfView(
    modifier: Modifier = Modifier,
    pdfAssetName: String? = null,
    pdfFile: File? = null,
    error: () -> Unit,
) {
    AndroidView(modifier = modifier
        .fillMaxHeight()
        .fillMaxWidth(),
        factory = {
            PdfViewWrapper(it, appMode = getAppMode(it)).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = {
            it.getPdfView().run {
                pdfAssetName?.let {
                    fromAsset(pdfAssetName)
                } ?: pdfFile?.takeIf { it.exists() }?.let {
                    fromFile(pdfFile)
                }
            }?.run {
                enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .onError {
                        error()
                    }
                    .enableAntialiasing(true)
                    .spacing(0)
                    .autoSpacing(false)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .load()
            }
        })
}