package pl.gov.mf.etoll.front.ridedetailsmap

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
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
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.databinding.FragmentRideDetailsMapBinding
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.ridedetailsmap.RideDetailsMapFragmentViewModel.NavigationTargets.CURRENT
import pl.gov.mf.etoll.front.ridedetailsmap.RideDetailsMapFragmentViewModel.NavigationTargets.LOCATION_DATA_NOT_REPORTED
import java.util.*

class RideDetailsMapFragment :
    BaseDatabindingFragment<RideDetailsMapFragmentViewModel, RideDetailsMapFragmentComponent>(),
    OnMapReadyCallback {

    private lateinit var binding: FragmentRideDetailsMapBinding

    private var map: GoogleMap? = null
    private var lastLocalAppLatLng: LatLng? = null
    private val markersMap: EnumMap<MarkerKey, Marker> = EnumMap(MarkerKey::class.java)

    override fun createComponent(): RideDetailsMapFragmentComponent =
        (context as MainActivity).component.plus(
            RideDetailsMapFragmentModule(this, lifecycle)
        )

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)

        viewModel.initDefaults()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_ride_details_map,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialToolbar>(R.id.ride_details_map_toolbar)
            .setNavigationOnClickListener { findNavController().popBackStack() }

        viewModel.navigate.observe(viewLifecycleOwner) { navigation ->
            when (navigation) {
                CURRENT -> {
                }
                LOCATION_DATA_NOT_REPORTED -> {
                    removeRemoteMarkers()
                    focusMap()
                    viewModel.resetNavigation()
                }
                else -> {
                }
            }
        }

        val mapFragment: SupportMapFragment = childFragmentManager
            .findFragmentById(R.id.ride_details_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

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
        }

        viewModel.startPositionViewData.observe(viewLifecycleOwner) { startLocationViewData ->
            if (startLocationViewData.startLatLngVisible) {
                removeOldMarker(MarkerKey.START)
                addNewMarkerToPos(
                    startLocationViewData.starLatLng,
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_start_loc, null),
                    MarkerKey.START
                )
                focusMap()
            }
        }

        viewModel.localPositionsViewData.observe(viewLifecycleOwner) { localViewData ->
            if (localViewData.localAppLatLngVisible) {
                if (lastLocalAppLatLng != localViewData.appLatLng) {
                    lastLocalAppLatLng = localViewData.appLatLng
                    removeOldMarker(MarkerKey.APP)
                    addNewMarkerToPos(
                        localViewData.appLatLng,
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_app, null),
                        MarkerKey.APP
                    )
                    focusMap()
                }
            }
        }

        viewModel.remotePositionsViewData.observe(viewLifecycleOwner) { remoteViewData ->
            //Pin with last app postion received by backend
            if (remoteViewData.appLogVisible) {
                val remoteAppLatLng = remoteViewData.appLatLng

                removeOldMarker(MarkerKey.LOG)
                addNewMarkerToPos(
                    remoteAppLatLng,
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_log, null),
                    MarkerKey.LOG
                )
                focusMap()
            }

            //Pin with last zsl position received by backend
            if (remoteViewData.zslVisible) {
                val remoteZslLatLng = remoteViewData.zslLatLng

                removeOldMarker(MarkerKey.ZSL)
                addNewMarkerToPos(
                    remoteZslLatLng,
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_zsl, null),
                    MarkerKey.ZSL
                )
                focusMap()
            }
        }
    }

    private fun focusMap() {
        val latLngBuilder = LatLngBounds.Builder()
        val areaPadding = resources.getDimensionPixelSize(R.dimen.map_padding)

        if (markersMap.entries.isNotEmpty())
            markersMap.entries.forEach { entry -> latLngBuilder.include(entry.value.position) }
        else
            PolandEdgePoints.values().forEach { latLngBuilder.include(it.latLng) }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                latLngBuilder.build(), areaPadding
            )
        )
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

    private fun removeRemoteMarkers() {
        removeOldMarker(MarkerKey.LOG)
        removeOldMarker(MarkerKey.ZSL)
    }

    private fun removeOldMarker(markerKey: MarkerKey) {
        markersMap[markerKey]?.remove()
        markersMap.remove(markerKey)
    }

    enum class MarkerKey {
        APP, LOG, ZSL, START
    }

    enum class PolandEdgePoints(val latLng: LatLng) {
        @Keep
        NORTH(LatLng(54.835715, 18.302786)),

        @Keep
        SOUTH(LatLng(54.835560, 18.309974)),

        @Keep
        WEST(LatLng(52.838814, 14.125326)),

        @Keep
        EAST(LatLng(50.869060, 24.145557))
    }

}