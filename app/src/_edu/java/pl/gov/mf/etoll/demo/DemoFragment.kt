package pl.gov.mf.etoll.demo

import android.os.Bundle
import pl.gov.mf.etoll.front.MainMenuFragment

class DemoFragment : MainMenuFragment<DemoFragmentViewModel, DemoFragmentComponent>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun createComponent(): DemoFragmentComponent =
        activityComponent.plus(DemoFragmentModule(this, lifecycle))

}