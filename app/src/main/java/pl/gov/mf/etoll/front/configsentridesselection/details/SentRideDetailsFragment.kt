package pl.gov.mf.etoll.front.configsentridesselection.details

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.databinding.FragmentConfigSentRideDetailsBinding
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.configsentridesselection.details.SentRideDetailsFragmentViewModel.SentRideDetailsNavigationLocation
import pl.gov.mf.mobile.ui.components.dialogs.DialogsHelper
import java.util.*
import javax.inject.Inject


class SentRideDetailsFragment :
    BaseDatabindingFragment<SentRideDetailsFragmentViewModel, SentRideDetailsFragmentComponent>(),
    OnMapReadyCallback {
    private var map: GoogleMap? = null
    private val markersMap: EnumMap<MarkerKey, Marker> = EnumMap(MarkerKey::class.java)
    private var lastLocalAppLatLng: LatLng? = null
    private lateinit var binding: FragmentConfigSentRideDetailsBinding

    @Inject
    lateinit var dialogsHelper: DialogsHelper

    companion object {
        const val ON_SENT_RIDE_INFO_RESULT = "ON_SENT_RIDE_INFO_RESULT"
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        // invalidate map
        if (viewModel.appMode.value?.appMode == AppMode.DARK_MODE) {
            map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_dark_mode_style
                )
            )
        } else {
            map?.setMapStyle(null)
        }
    }

    override fun createComponent(): SentRideDetailsFragmentComponent =
        (context as MainActivity).component.plus(
            SentRideDetailsFragmentModule(this, lifecycle)
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)

        viewModel.updateSent(requireArguments())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_config_sent_ride_details,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialToolbar>(R.id.config_sent_ride_details_toolbar)
            .setNavigationOnClickListener { popBackStack() }
        view.findViewById<MaterialButton>(R.id.config_sent_ride_details_back)
            .setOnClickListener { popBackStack() }

        view.findViewById<MaterialButton>(R.id.config_sent_ride_details_select).setOnClickListener {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                ON_SENT_RIDE_INFO_RESULT,
                viewModel.sent.value!!.toJSON()
            )
            findNavController().popBackStack()
        }

        viewModel.navigate.observe(viewLifecycleOwner) { navigation ->
            when (navigation) {
                SentRideDetailsNavigationLocation.ERROR -> {
                    viewModel.resetNavigation()
                    // todo - dialog or something
                }
                else -> {
                }
            }
        }

        val mapFragment: SupportMapFragment = childFragmentManager
            .findFragmentById(R.id.sent_ride_details_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun popBackStack() {
        findNavController().previousBackStackEntry
            ?.savedStateHandle
            ?.remove<String>(ON_SENT_RIDE_INFO_RESULT)
        findNavController().popBackStack()
    }

    // suppressing MissingPermission for isMyLocationEnabled because app is restarted when location permission is disabled
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.isMyLocationEnabled = false

        if (viewModel.appMode.value?.appMode == AppMode.DARK_MODE) {
            map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_dark_mode_style
                )
            )
        } else {
            map?.setMapStyle(null)
        }

        viewModel.sent.observe(viewLifecycleOwner) { sent ->
            val start =
                LatLng(sent.item.loadingAddress.latitude, sent.item.loadingAddress.longitude)
            markersMap[MarkerKey.START_DEST]?.run {
                position = start
            } ?: addNewMarkerToPos(
                start,
                ResourcesCompat.getDrawable(resources, R.drawable.ic_map_marker_green_light, null),
                MarkerKey.START_DEST
            )

            val destination =
                LatLng(sent.item.deliveryAddress.latitude, sent.item.deliveryAddress.longitude)
            markersMap[MarkerKey.END_DEST]?.run {
                position = destination
            } ?: addNewMarkerToPos(
                destination,
                ResourcesCompat.getDrawable(resources, R.drawable.ic_map_marker_red_light, null),
                MarkerKey.END_DEST
            )

            focusMap()
        }

        viewModel.lastLocalAppLocation.observe(viewLifecycleOwner) { localGpsViewData ->
            if (localGpsViewData.localAppLatLngVisible) {
                val newAppLatLng = localGpsViewData.appLatLng
                if (newAppLatLng != lastLocalAppLatLng) {
                    lastLocalAppLatLng = newAppLatLng
                    markersMap[MarkerKey.APP]?.run {
                        position = newAppLatLng
                    } ?: addNewMarkerToPos(
                        newAppLatLng,
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_map_marker_blue_light,
                            null
                        ),
                        MarkerKey.APP
                    )
                    focusMap()
                }
            } else {
                markersMap[MarkerKey.APP]?.remove()
            }
        }
    }

    private fun addNewMarkerToPos(pos: LatLng, pin: Drawable?, markerKey: MarkerKey) {
        pin?.let {
            markersMap.put(
                markerKey, map?.addMarker(
                    MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(it.toBitmap()))
                        .position(pos)
                )
            )
        }
    }

    private fun focusMap() {
        val latLngBuilder = LatLngBounds.Builder()
        val areaPadding = resources.getDimensionPixelSize(R.dimen.map_padding)

        markersMap.entries.forEach { entry ->
            latLngBuilder.include(entry.value.position)
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                latLngBuilder.build(), areaPadding
            )
        )
    }

    enum class MarkerKey {
        APP, START_DEST, END_DEST
    }
}