package pl.gov.mf.etoll.maps

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import javax.inject.Inject

/**
 * Note. "Renderer requests are not guaranteed, and the latest renderer may not always be returned."
 * See more in https://developers.google.com/maps/documentation/android-sdk/renderer
 */
class MapsRendererConfiguratorImpl @Inject constructor(private val context: Context) :
    MapsRendererConfigurator {

    override fun initializeRenderer() {
        MapsInitializer.initialize(context, MapsInitializer.Renderer.LATEST) {
            when (it) {
                MapsInitializer.Renderer.LEGACY ->
                    Log.d(RENDERER_TAG, "Legacy version of renderer is used")
                MapsInitializer.Renderer.LATEST ->
                    Log.d(RENDERER_TAG, "Latest version of renderer is used")
            }
        }
    }

    companion object {
        const val RENDERER_TAG = "MAPS_RENDERER"
    }

}