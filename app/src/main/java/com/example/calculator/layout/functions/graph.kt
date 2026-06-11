package com.example.calculator.layout.functions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun GraphCanvas(modifier: Modifier = Modifier) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(1f) }
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        fontSize = 10.sp
    )

    val graphPoints = remember { mutableStateListOf<List<Offset?>>() }

    BoxWithConstraints(modifier = modifier) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        LaunchedEffect(functions_list.toList(), offset, zoom, width, height) {
            val states = functions_list.toList()

            withContext(Dispatchers.Default) {
                val newPoints = states.map { state ->
                    val originalText = state.text.toString().replace(" ", "")
                    if (originalText.isBlank()) return@map emptyList<Offset?>()

                    val centerX = width / 2 + offset.x
                    val centerY = height / 2 + offset.y

                    // Case 1: x = constant or x = f(y)
                    if (originalText.startsWith("x=") || originalText.startsWith("x\\approx")) {
                        val exprStr = if (originalText.contains("=")) originalText.substringAfter("=") else originalText.substringAfter("\\approx")
                        val cleanExpr = com.example.calculator.parser.prepareMathString(exprStr)
                        
                        val mathExpr = org.mariuszgromada.math.mxparser.Expression(cleanExpr)
                        
                        if (cleanExpr.contains("y")) {
                            // x = f(y)
                            val argY = org.mariuszgromada.math.mxparser.Argument("y", 0.0)
                            mathExpr.addArguments(argY)
                            val points = mutableListOf<Offset?>()
                            for (py in 0 until height.toInt() step 2) {
                                val coordY = (centerY - py) / (100f * zoom)
                                argY.argumentValue = coordY.toDouble()
                                val coordX = mathExpr.calculate()
                                if (!coordX.isNaN() && coordX.isFinite()) {
                                    val px = centerX + (coordX.toFloat() * 100f * zoom)
                                    points.add(Offset(px, py.toFloat()))
                                } else {
                                    points.add(null)
                                }
                            }
                            points
                        } else {
                            // x = constant
                            val xVal = mathExpr.calculate()
                            if (!xVal.isNaN() && xVal.isFinite()) {
                                val px = centerX + (xVal.toFloat() * 100f * zoom)
                                listOf(Offset(px, -2000f), Offset(px, height + 2000f))
                            } else emptyList()
                        }
                    } 
                    // Case 2: y = f(x) or just f(x)
                    else {
                        val exprStr = if (originalText.contains("=")) {
                            if (originalText.startsWith("y=")) originalText.substringAfter("=")
                            else if (originalText.contains("f_{")) originalText.substringAfter("=")
                            else originalText // fallback
                        } else originalText

                        val cleanExpr = com.example.calculator.parser.prepareMathString(exprStr)
                        val mathExpr = org.mariuszgromada.math.mxparser.Expression(cleanExpr)
                        val argX = org.mariuszgromada.math.mxparser.Argument("x", 0.0)
                        mathExpr.addArguments(argX)
                        
                        val points = mutableListOf<Offset?>()
                        for (px in 0 until width.toInt() step 2) {
                            val coordX = (px - centerX) / (100f * zoom)
                            argX.argumentValue = coordX.toDouble()
                            val coordY = mathExpr.calculate()
                            
                            if (!coordY.isNaN() && coordY.isFinite()) {
                                val py = centerY - (coordY.toFloat() * 100f * zoom)
                                points.add(Offset(px.toFloat(), py))
                            } else {
                                points.add(null)
                            }
                        }
                        points
                    }
                }
                graphPoints.clear()
                graphPoints.addAll(newPoints)
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, gestureZoom, _ ->
                        val oldZoom = zoom
                        zoom = (zoom * gestureZoom).coerceIn(0.001f, 1000f)
                        
                        val zoomChange = zoom / oldZoom
                        val relCentroidX = centroid.x - size.width / 2
                        val relCentroidY = centroid.y - size.height / 2
                        
                        offset = Offset(
                            x = relCentroidX - (relCentroidX - offset.x) * zoomChange + pan.x,
                            y = relCentroidY - (relCentroidY - offset.y) * zoomChange + pan.y
                        )
                    }
                }
        ) {
            val centerX = width / 2 + offset.x
            val centerY = height / 2 + offset.y
            val rawUnitsPer100px = 1f / zoom
            val powerOf10 = Math.floor(Math.log10(rawUnitsPer100px.toDouble())).toInt()
            val fraction = rawUnitsPer100px / Math.pow(10.0, powerOf10.toDouble()).toFloat()
            
            val niceFraction = when {
                fraction < 1.5f -> 1f
                fraction < 3.5f -> 2f
                fraction < 7.5f -> 5f
                else -> 10f
            }
            
            val unitValue = niceFraction * Math.pow(10.0, powerOf10.toDouble()).toFloat()
            val step = 100f * zoom * unitValue
            
            var gridX = centerX % step
            if (gridX < 0) gridX += step
            while (gridX < width) {
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(gridX, 0f),
                    end = Offset(gridX, height),
                    strokeWidth = 1f
                )
                gridX += step
            }

            var gridY = centerY % step
            if (gridY < 0) gridY += step
            while (gridY < height) {
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(0f, gridY),
                    end = Offset(width, gridY),
                    strokeWidth = 1f
                )
                gridY += step
            }

            if (centerX in 0f..width) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, height),
                    strokeWidth = 2f
                )
            }
            if (centerY in 0f..height) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 2f
                )
            }

            fun formatLabel(v: Double): String {
                return if (v == Math.floor(v)) v.toLong().toString()
                else {
                    val s = v.toString()
                    if (s.length > 5) "%.2f".format(java.util.Locale.US, v).trimEnd('0').trimEnd('.')
                    else s
                }
            }

            var labelMultiplier = Math.ceil((-centerX / step).toDouble()).toInt()
            var xPos = centerX + labelMultiplier * step
            while (xPos < width) {
                if (labelMultiplier != 0) {
                    val displayVal = labelMultiplier * unitValue.toDouble()
                    val labelText = formatLabel(displayVal)
                    val textLayoutResult = textMeasurer.measure(labelText, textStyle)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = labelText,
                        style = textStyle,
                        topLeft = Offset(xPos - textLayoutResult.size.width / 2, centerY.coerceIn(0f, height - textLayoutResult.size.height - 20f) + 5f)
                    )
                }
                labelMultiplier++
                xPos += step
            }

            labelMultiplier = Math.ceil((-centerY / step).toDouble()).toInt()
            var yPos = centerY + labelMultiplier * step
            while (yPos < height) {
                if (labelMultiplier != 0) {
                    val displayVal = -labelMultiplier * unitValue.toDouble()
                    val labelText = formatLabel(displayVal)
                    val textLayoutResult = textMeasurer.measure(labelText, textStyle)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = labelText,
                        style = textStyle,
                        topLeft = Offset(centerX.coerceIn(0f, width - textLayoutResult.size.width - 20f) + 10f, yPos - textLayoutResult.size.height / 2)
                    )
                }
                labelMultiplier++
                yPos += step
            }

            val colors = listOf(Color.Blue, Color.Red, Color.Green, Color.Magenta, Color.Cyan, Color.Yellow)

            graphPoints.forEachIndexed { index, points ->
                val color = colors[index % colors.size]
                var prevPoint: Offset? = null

                points.forEach { currentPoint ->
                    if (currentPoint != null && prevPoint != null) {
                        if ((currentPoint.y in -2000f..(height + 2000f)) && 
                            (prevPoint.y in -2000f..(height + 2000f))) {

                            val yDiff = Math.abs(currentPoint.y - prevPoint.y)
                            if (yDiff < height * 2) {
                                drawLine(color = color, start = prevPoint, end = currentPoint, strokeWidth = 3f)
                            }
                        }
                    }
                    prevPoint = currentPoint
                }
            }
        }
    }
}
