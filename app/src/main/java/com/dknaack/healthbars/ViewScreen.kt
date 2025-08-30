package com.dknaack.healthbars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dknaack.healthbars.components.HealthBarIndicator
import com.dknaack.healthbars.data.HealthBar
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.round

/**
 * Shows data for a health bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewScreen(
    navController: NavController,
    healthBar: HealthBar,
) {
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val endDate = healthBar.startDate.plus(healthBar.duration)
    val endDateText = dateFormatter.format(endDate)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(healthBar.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { TODO("Implement editing") }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontSize = 30.sp)) {
                                    append(round(healthBar.getProgress() * 100f).toInt().toString())
                                }
                                withStyle(SpanStyle(fontSize = 14.sp)) {
                                    append("%")
                                }
                            },
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    HealthBarIndicator(healthBar)

                    Text("Your ${healthBar.name} dies on $endDateText")
                }
            }
        }
    }
}
