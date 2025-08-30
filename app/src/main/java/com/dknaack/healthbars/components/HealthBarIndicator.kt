package com.dknaack.healthbars.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dknaack.healthbars.HealthBar

@Composable
fun HealthBarIndicator(healthBar: HealthBar) {
    LinearProgressIndicator(
        drawStopIndicator = { },
        progress = { healthBar.getProgress() },
        modifier = Modifier.fillMaxWidth().height(16.dp),
    )
}
