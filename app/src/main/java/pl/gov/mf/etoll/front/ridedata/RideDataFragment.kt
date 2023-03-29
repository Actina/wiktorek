package pl.gov.mf.etoll.front.ridedata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentRideDataBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment

class RideDataFragment :
    BaseDatabindingFragment<RideDataViewModel, RideDataFragmentComponent>() {

    private lateinit var binding: FragmentRideDataBinding

    override fun createComponent(): RideDataFragmentComponent = activityComponent.plus(
        RideDataFragmentModule(this, lifecycle)
    )

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_ride_data,
            container, false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialToolbar>(R.id.ride_data_toolbar).setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}