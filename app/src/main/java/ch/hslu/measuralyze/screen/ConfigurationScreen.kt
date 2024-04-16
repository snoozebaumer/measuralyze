package ch.hslu.measuralyze.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.hslu.measuralyze.SharedViewModel
import ch.hslu.measuralyze.model.MeasureLocation

@Composable
fun ConfigurationScreen(modifier: Modifier = Modifier, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current

    sharedViewModel.initConfigForm()

    fun onStageAdded(stage: String) {
        sharedViewModel.addStage(stage)
        sharedViewModel.configFormDirty = true
    }

    fun onStageDeleted(index: Int) {
        sharedViewModel.removeStage(index)
        sharedViewModel.configFormDirty = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Card(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                StagesEditor(
                    stages = sharedViewModel.stagesFormData,
                    onStageAdded = (::onStageAdded),
                    modifier = Modifier.padding(16.dp)
                ) {
                    onStageDeleted(it)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card {
                LocationsEditor(
                    locations = sharedViewModel.measureLocationsFormData,
                    onLocationAdded = {
                        sharedViewModel.addLocation(it)
                        sharedViewModel.configFormDirty = true
                    },
                    openLocation = {
                        val lat = it.latitude
                        val long = it.longitude
                        val uri = "geo:$lat,$long?q=$lat,$long&z=20"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        intent.resolveActivity(context.packageManager)?.let {
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    sharedViewModel.removeLocation(it)
                    sharedViewModel.configFormDirty = true
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    LabeledIntegerField(
                        value = sharedViewModel.iterationsFormData.value,
                        onValueChange = {
                            sharedViewModel.iterationsFormData.value = it
                            sharedViewModel.configFormDirty = true
                        },
                        label = "Iterations"
                    )
                }

                HorizontalDivider(Modifier.padding(horizontal = 16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    LabeledIntegerField(
                        value = sharedViewModel.measurementIntervalInMsFormData.value,
                        onValueChange = {
                            sharedViewModel.measurementIntervalInMsFormData.value = it
                            sharedViewModel.configFormDirty = true
                        },
                        label = "Measurement Interval (ms)"
                    )
                }
            }

        }
    }
}

@Composable
fun StagesEditor(
    modifier: Modifier = Modifier,
    stages: State<List<String>>,
    onStageAdded: (String) -> Unit,
    onStageDeleted: (Int) -> Unit
) {
    Column(modifier = modifier) {
        Text("Stages", style = MaterialTheme.typography.headlineSmall)

        stages.value.forEachIndexed { index, stage ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(stage, modifier = Modifier.weight(1f))

                if (stages.value.lastIndex > 0) {
                    IconButton(onClick = { onStageDeleted(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Stage")
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            var newStage by remember { mutableStateOf("") }

            OutlinedTextField(
                value = newStage,
                onValueChange = { newStage = it },
                label = { Text("Enter stage") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        onStageAdded(newStage)
                        newStage = ""
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface
                )
            )

            IconButton(onClick = {
                onStageAdded(newStage)
                newStage = ""
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Stage")
            }
        }
    }
}

@Composable
fun LocationsEditor(
    modifier: Modifier = Modifier,
    locations: State<List<MeasureLocation>>,
    onLocationAdded: (MeasureLocation) -> Unit,
    openLocation: (MeasureLocation) -> Unit,
    onLocationDeleted: (Int) -> Unit
) {
    Column(modifier = modifier) {
        Text("Locations", style = MaterialTheme.typography.headlineSmall)

        locations.value.forEachIndexed { index, location ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(location.description)

                    Text(
                        "lat: ${location.latitude}, long: ${location.longitude}",
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = { openLocation(location) }) {
                    Icon(Icons.Default.Place, contentDescription = "Open in Map")
                }

                if (locations.value.lastIndex > 0) {
                    IconButton(onClick = { onLocationDeleted(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Location")
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            var newDescription by remember { mutableStateOf("") }
            var newLat by remember { mutableStateOf("") }
            var newLong by remember { mutableStateOf("") }

            Column(modifier = Modifier.weight(1f)) {

                Row() {
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Enter location description") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surface,
                            unfocusedContainerColor = colorScheme.surface,
                            disabledContainerColor = colorScheme.surface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row() {
                    OutlinedTextField(
                        value = newLat,
                        onValueChange = { newLat = it },
                        label = { Text("Enter lat") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surface,
                            unfocusedContainerColor = colorScheme.surface,
                            disabledContainerColor = colorScheme.surface
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = newLong,
                        onValueChange = { newLong = it },
                        label = { Text("Enter long") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                val lat = newLat.toDoubleOrNull() ?: return@KeyboardActions
                                val long = newLong.toDoubleOrNull() ?: return@KeyboardActions
                                onLocationAdded(MeasureLocation(newDescription, lat, long))
                                newDescription = ""
                                newLat = ""
                                newLong = ""
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surface,
                            unfocusedContainerColor = colorScheme.surface,
                            disabledContainerColor = colorScheme.surface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            IconButton(onClick = {
                val lat = newLat.toDoubleOrNull() ?: return@IconButton
                val long = newLong.toDoubleOrNull() ?: return@IconButton
                onLocationAdded(MeasureLocation(newDescription, lat, long))
                newDescription = ""
                newLat = ""
                newLong = ""
            }, modifier = Modifier.padding(start = 8.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add Location")
            }
        }
    }
}

@Composable
fun LabeledIntegerField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { newValue ->
                val intValue = if (newValue.isEmpty()) 0 else newValue.toIntOrNull()
                    ?: value // If parsing fails, keep previous value
                onValueChange(intValue)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorScheme.surface,
                unfocusedContainerColor = colorScheme.surface,
                disabledContainerColor = colorScheme.surface
            ),
            modifier = Modifier.width(120.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigurationScreenPreview() {
    ConfigurationScreen(sharedViewModel = SharedViewModel(LocalContext.current))
}