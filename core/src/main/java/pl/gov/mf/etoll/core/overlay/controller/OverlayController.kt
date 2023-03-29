package pl.gov.mf.etoll.core.overlay.controller

import android.content.Context

interface OverlayController {
    /**
     * Try to enable overlay settings - failure means no permissions
     */
    fun tryToEnableOverlay(context: Context): Boolean

    /**
     * Disable overlay function
     */
    fun disableOverlay()

    /**
     * Get current status
     */
    fun overlayIsEnabled(context: Context): Boolean

    fun askForPermissions(context: Context)
}