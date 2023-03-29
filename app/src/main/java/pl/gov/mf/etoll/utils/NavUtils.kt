package pl.gov.mf.etoll.utils

import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun NavController.navigate(scope: CoroutineScope, job: Job, directions: NavDirections) =
    scope.launch(job + Dispatchers.Main) { navigate(directions) }

fun NavController.popBackStack(scope: CoroutineScope, job: Job) =
    scope.launch(job + Dispatchers.Main) { popBackStack() }

fun NavController.popBackStack(
    scope: CoroutineScope,
    job: Job,
    @IdRes destinationId: Int,
    inclusive: Boolean,
) = scope.launch(job + Dispatchers.Main) { popBackStack(destinationId, inclusive) }