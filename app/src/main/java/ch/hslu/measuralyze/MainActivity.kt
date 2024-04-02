package ch.hslu.measuralyze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.hslu.measuralyze.screen.ConfigurationScreen
import ch.hslu.measuralyze.screen.MeasureScreen
import ch.hslu.measuralyze.ui.theme.MeasuralyzeTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sharedViewModel: SharedViewModel = remember { SharedViewModel() }
            var settingsActive by remember { mutableStateOf(false) }

            MeasuralyzeTheme(darkTheme = false) {
                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                    topBar = {
                        CenterAlignedTopAppBar(
                            navigationIcon = {
                                if (settingsActive) {IconButton(onClick = { settingsActive = false }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back to measure screen"
                                    )
                                }
                            } else {null}},
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text(
                                    "measuralyze",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            actions = {
                                if (!settingsActive) {
                                    IconButton(onClick = { settingsActive = true }) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = "Configuration"
                                        )
                                    }
                                } else {
                                    Text(text = "Save", modifier = Modifier.clickable { sharedViewModel.saveConfig()
                                        settingsActive = false }.padding(end = 16.dp),
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            scrollBehavior = scrollBehavior,
                        )
                    },
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // if there are going to be more than two screens in the future, it would be better to use a navigation component
                        if (settingsActive) {
                            ConfigurationScreen(modifier = Modifier.padding(innerPadding), sharedViewModel = sharedViewModel)
                        } else {
                            MeasureScreen(modifier = Modifier.padding(innerPadding), sharedViewModel = sharedViewModel)
                        }
                    }
                }
            }
        }
    }

}