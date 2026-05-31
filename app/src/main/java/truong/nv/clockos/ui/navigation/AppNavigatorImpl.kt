package truong.nv.clockos.ui.navigation

import androidx.navigation.NavHostController

class AppNavigatorImpl(
    private val navController: NavHostController
) : AppNavigator {
    override fun backToTasks() {
        navController.popBackStack(MainRouter, inclusive = false)
    }

}