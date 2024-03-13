package ch.hslu.measuralyze.components.measure

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MeasureButton(text: String, color: Color, onClick: () -> Unit) {
    Surface(
        color = color,
        shape = CircleShape,
        modifier = Modifier
            .size(250.dp)
            .clickable { onClick() }
    ) {
        Column (verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
        }
    }
}