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
import cu.todus.app.ui.screens.home.HomeScreen
import cu.todus.app.ui.screens.contacts.ContactsScreen
import cu.todus.app.ui.screens.chat.ChatScreen
import cu.todus.app.ui.screens.editprofile.EditProfileScreen
import cu.todus.app.ui.screens.contactprofile.ContactProfileScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object PhoneInput : Screen("phone_input")
    object Home : Screen("home")
    object Contacts : Screen("contacts")
    object EditProfile : Screen("edit_profile")
    object ContactProfile : Screen("contact_profile/{phone}") {
        fun createRoute(phone: String) = "contact_profile/$phone"
    }
    object Chat : Screen("chat/{jid}/{name}") {
        fun createRoute(jid: String, name: String) = "chat/$jid/$name"
    }
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController(), startDestination: String = Screen.Welcome.route) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(onContinue = { navController.navigate(Screen.PhoneInput.route) })
        }
        composable(Screen.PhoneInput.route) {
            PhoneInputScreen(
                onBack = { navController.popBackStack() },
                onContinue = { phone, jwt ->
                    navController.navigate(Screen.Home.route) { popUpTo(0) }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onChatClick = { jid, name -> navController.navigate(Screen.Chat.createRoute(jid, name)) },
                onNewChat = { navController.navigate(Screen.Contacts.route) },
                onProfileClick = { navController.navigate(Screen.EditProfile.route) }
            )
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(Screen.Contacts.route) {
            ContactsScreen(
                onBack = { navController.popBackStack() },
                onContactClick = { jid, name ->
                    navController.navigate(Screen.ContactProfile.createRoute(jid))
                }
            )
        }
        composable(
            route = Screen.ContactProfile.route,
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            ContactProfileScreen(
                phone = phone,
                onBack = { navController.popBackStack() },
                onMessage = { jid, name -> navController.navigate(Screen.Chat.createRoute(jid, name)) }
            )
        }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("jid") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val jid = backStackEntry.arguments?.getString("jid") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            ChatScreen(
                chatJid = jid,
                chatName = name,
                onBack = { navController.popBackStack() },
                onContactProfile = { phone -> navController.navigate(Screen.ContactProfile.createRoute(phone)) }
            )
        }
    }
}
