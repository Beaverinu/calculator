package com.example.calculator.layout

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp



val options = listOf("Standard", "Equations", "Functions")
var calculator_current_type by mutableStateOf("Standard")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitTopBar(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit
) {
    Box {
        CenterAlignedTopAppBar(
            title = {
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    options.forEachIndexed { index, title ->
                        val isSelected = selectedIndex == index
                        val underlineWidth by animateDpAsState(
                            targetValue = if (isSelected) 40.dp else 0.dp,
                            animationSpec = tween(durationMillis = 100),
                            label = "underlineWidth",
                        )
                        Column(
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { 
                                onSelectedIndexChange(index)
                                calculator_current_type = options[index]
                            },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Underline
                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(underlineWidth)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        )
        Switch(
            checked = isDarkMode,
            onCheckedChange = onDarkModeChange,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, top = 32.dp),
            thumbContent = {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    modifier = Modifier.padding(2.dp)
                )
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                checkedIconColor = Color.White,
                uncheckedIconColor = Color.White
            )
        )
    }
}


