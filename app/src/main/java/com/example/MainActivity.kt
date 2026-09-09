package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.ui.MainViewModel
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.PortfolioScreen
import com.example.ui.screens.RewardsContactScreen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.TextToImageTheme

enum class ScreenTab(val title: String, val icon: ImageVector) {
    GENERATOR("Generator", Icons.Default.AutoAwesome),
    GALLERY("Gallery", Icons.Default.Collections),
    PORTFOLIO("Portfolio", Icons.Default.Person),
    REWARDS("Rewards", Icons.Default.Whatshot)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TextToImageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppContent(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(ScreenTab.GENERATOR) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                ScreenTab.values().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentPurple,
                            selectedTextColor = AccentPurple,
                            indicatorColor = AccentPurple.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            ScreenTab.GENERATOR -> GeneratorScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ScreenTab.GALLERY -> GalleryScreen(
                viewModel = viewModel,
                onNavigateToGenerator = { currentTab = ScreenTab.GENERATOR },
                modifier = Modifier.padding(innerPadding)
            )
            ScreenTab.PORTFOLIO -> PortfolioScreen(
                viewModel = viewModel,
                onNavigateToGeneratorWithPrompt = { prompt ->
                    viewModel.applyPromptSuggestion(prompt)
                    currentTab = ScreenTab.GENERATOR
                },
                modifier = Modifier.padding(innerPadding)
            )
            ScreenTab.REWARDS -> RewardsContactScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
