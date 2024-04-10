package ch.hslu.measuralyze.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import ch.hslu.measuralyze.SharedViewModel

@Composable
fun ConfigurationScreen(modifier: Modifier = Modifier, sharedViewModel: SharedViewModel) {

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
            .padding(top = 16.dp),
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
                Stages(
                    stages = sharedViewModel.stagesFormData,
                    onStageAdded = (::onStageAdded),
                    modifier = Modifier.padding(16.dp)
                ) {
                    onStageDeleted(it)
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
fun Stages(
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
                modifier = Modifier.weight(1f)
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
                val intValue = if (newValue.isEmpty()) 0 else newValue.toIntOrNull() ?: value // If parsing fails, keep previous value
                onValueChange(intValue)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(120.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigurationScreenPreview() {
    ConfigurationScreen(sharedViewModel = SharedViewModel(LocalContext.current))
}