package truong.nv.clockos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navigator = remember(navController) {
        AppNavigatorImpl(navController)
    }

    NavHost(navController = navController, startDestination = MainRouter) {
        composable<MainRouter> {
            MainScreen(navigator = navigator)
        }
    }
}