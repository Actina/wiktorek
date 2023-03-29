package pl.gov.mf.etoll.front.dashboard

import androidx.navigation.NavController
import pl.gov.mf.etoll.databinding.FragmentDashboardBinding

interface DashboardFlavourDelegate {
    fun initializeView(binding: FragmentDashboardBinding, navController: NavController)
    fun shouldShowGpsChangeSwitch(): Boolean = false
}