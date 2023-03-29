package pl.gov.mf.etoll.front.bottomNavigation.ds

data class BottomNavigationState(
    val shouldBeVisible: Boolean,
    val selectedState: BottomNavigationSelectedSection = BottomNavigationSelectedSection.CENTER
) {
    override fun equals(other: Any?): Boolean {
        return if (other is BottomNavigationState) {
            other.shouldBeVisible == shouldBeVisible && selectedState == other.selectedState
        } else {
            super.equals(other)
        }
    }
}