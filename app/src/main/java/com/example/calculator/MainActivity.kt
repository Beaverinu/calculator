package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calculator.layout.InitTopBar
import com.example.calculator.layout.standard.InitStandardCalculator
import com.example.calculator.layout.equations.InitEquationsCalculator
import com.example.calculator.layout.functions.InitFunctionsCalculator
import com.example.calculator.ui.theme.CalculatorTheme
import com.example.calculator.layout.calculator_current_type
import com.hrm.latex.base.LatexSDK

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LatexSDK.initialize()
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(true) }
            var selectedIndex by remember { mutableIntStateOf(0) }

            CalculatorTheme(
                darkTheme = isDarkMode,
                dynamicColor = false
            ) {
                val innerPadding = 12.dp
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        InitTopBar(
                            isDarkMode = isDarkMode,
                            onDarkModeChange = { isDarkMode = it },
                            selectedIndex = selectedIndex,
                            onSelectedIndexChange = { selectedIndex = it }
                        )
                    }
                ) { innerPadding ->
                    if (calculator_current_type == "Standard") {
                        InitStandardCalculator(
                            paddingValues = innerPadding,
                            isDarkMode = isDarkMode,
                            onDarkModeChange = { isDarkMode = it }
                        )
                    }
                    else if(calculator_current_type == "Equations"){
                        InitEquationsCalculator(
                            paddingValues = innerPadding,
                            isDarkMode = isDarkMode,
                            onDarkModeChange = { isDarkMode = it }
                        )
                    }
                    else if(calculator_current_type == "Functions"){
                        InitFunctionsCalculator (
                            paddingValues = innerPadding,
                            isDarkMode = isDarkMode,
                            onDarkModeChange = { isDarkMode = it }
                        )
                    }
                    else {
                        Text("HOWWWWW?!")
                    }
                }
            }
        }
    }
}
