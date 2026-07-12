package cu.todus.app.ui.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cu.todus.app.ui.screens.welcome.WelcomeScreen
import cu.todus.app.ui.screens.phone.PhoneInputScreen
import cu.todus.app.ui.screens.profile.ProfileScreen
import cu.todus.app.ui.screens.home.HomeScreen
import cu.todus.app.ui.screens.contacts.ContactsScreen
import cu.todus.app.ui.screens.chat.ChatScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object PhoneInput : Screen("phone_input")
    object Profile : Screen("profile")
    object Home : Screen("home")
    object Contacts : Screen("contacts")
    object Chat : Screen("chat/{jid}/{name}") {
        fun createRoute(jid: String, name: String) = "chat/$jid/$name"
    }
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Welcome.route) {
        composable(Screen.Welcome.route) { WelcomeScreen(onContinue = { navController.navigate(Screen.PhoneInput.route) }) }
        composable(Screen.PhoneInput.route) { PhoneInputScreen(onBack = { navController.popBackStack() }, onContinue = { navController.navigate(Screen.Profile.route) }) }
        composable(Screen.Profile.route) { ProfileScreen(onBack = { navController.popBackStack() }, onContinue = { navController.navigate(Screen.Home.route) { popUpTo(0) } }) }
        composable(Screen.Home.route) { HomeScreen(onChatClick = { jid, name -> navController.navigate(Screen.Chat.createRoute(jid, name)) }, onNewChat = { navController.navigate(Screen.Contacts.route) }) }
        composable(Screen.Contacts.route) { ContactsScreen(onBack = { navController.popBackStack() }, onContactClick = { jid, name -> navController.navigate(Screen.Chat.createRoute(jid, name)) }) }
        composable(route = Screen.Chat.route, arguments = listOf(navArgument("jid") { type = NavType.StringType }, navArgument("name") { type = NavType.StringType })) { backStackEntry ->
            val jid = backStackEntry.arguments?.getString("jid") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            ChatScreen(chatJid = jid, chatName = name, onBack = { navController.popBackStack() })
        }
    }
}
