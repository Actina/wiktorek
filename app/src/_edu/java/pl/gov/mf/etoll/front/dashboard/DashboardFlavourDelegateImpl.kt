package pl.gov.mf.etoll.front.dashboard

import androidx.navigation.NavController
import pl.gov.mf.etoll.databinding.FragmentDashboardBinding
import pl.gov.mf.etoll.security.checker.SecuritySanityCheckerUseCase
import javax.inject.Inject

class DashboardFlavourDelegateImpl @Inject constructor(private val signAndroidComponentUseCase: SecuritySanityCheckerUseCase.SignAndroidComponentUseCase) :
    DashboardFlavourDelegate {
    override fun initializeView(binding: FragmentDashboardBinding, navController: NavController) {
        // do nothing
    }
}