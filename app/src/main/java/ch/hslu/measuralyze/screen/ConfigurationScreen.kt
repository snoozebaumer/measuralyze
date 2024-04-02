package ch.hslu.measuralyze.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Stages(stages = sharedViewModel.stagesFormData, onStageAdded = (::onStageAdded)) {
                onStageDeleted(it)
            }
        }
    }
}

@Composable
fun Stages(
    stages: State<List<String>>,
    onStageAdded: (String) -> Unit,
    onStageDeleted: (Int) -> Unit
) {
    Column {
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

@Preview(showBackground = true)
@Composable
fun ConfigurationScreenPreview() {
    ConfigurationScreen(sharedViewModel = SharedViewModel(LocalContext.current))
}