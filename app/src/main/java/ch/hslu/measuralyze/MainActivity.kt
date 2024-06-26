package ch.hslu.measuralyze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
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
            val sharedViewModel: SharedViewModel = remember { SharedViewModel(baseContext) }
            var settingsActive by remember { mutableStateOf(false) }
            val openDialog = remember { mutableStateOf(false) }

            MeasuralyzeTheme(darkTheme = false) {
                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                    topBar = {
                        CenterAlignedTopAppBar(
                            navigationIcon = {
                                if (settingsActive) {
                                    IconButton(onClick = { if (sharedViewModel.hasUnsavedConfigChanges()) openDialog.value = true else settingsActive = false }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back to measure screen"
                                        )
                                    }
                                }
                            },
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
                                    IconButton(onClick = { settingsActive = true }, enabled = !sharedViewModel.measuring.value) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = "Configuration"
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Save", modifier = Modifier
                                            .clickable {
                                                sharedViewModel.saveConfig()
                                                settingsActive = false
                                            }
                                            .padding(end = 16.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
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
                            ConfigurationScreen(
                                modifier = Modifier.padding(innerPadding),
                                sharedViewModel = sharedViewModel
                            )
                        } else {
                            MeasureScreen(
                                modifier = Modifier.padding(innerPadding),
                                sharedViewModel = sharedViewModel
                            )
                        }
                    }
                }

                if (openDialog.value) {
                    UnsavedChangesAlert(
                        onDismissRequest = {
                            openDialog.value = false
                            settingsActive = false
                            sharedViewModel.configFormDirty = false
                        },
                        onConfirmation = {
                            sharedViewModel.saveConfig()
                            openDialog.value = false
                            settingsActive = false
                        },
                        dialogTitle = "Unsaved Changes",
                        dialogText = "Do you want to save your changes?"
                    )
                }
            }
            fun createOnBackPressedCallback(): OnBackPressedCallback {
                return object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {

                        if (settingsActive) {
                            if (sharedViewModel.hasUnsavedConfigChanges()) {
                                openDialog.value = true
                            } else {
                                settingsActive = false
                            }
                        } else {
                            finish()
                        }
                    }
                }
            }
            onBackPressedDispatcher.addCallback(createOnBackPressedCallback())
        }
    }
}

@Composable
fun UnsavedChangesAlert(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String
) {
    AlertDialog(
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Discard")
            }
        }
    )
}