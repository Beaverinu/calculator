package com.example.calculator.parser

import androidx.compose.runtime.mutableStateOf
import org.mariuszgromada.math.mxparser.*

val previous_ans = mutableStateOf("/")
val live_result = mutableStateOf("/")
val isResultFinalized = mutableStateOf(false)

fun evaluateExpression(latex: String, isLive: Boolean = false): String {
    if (latex.isBlank()) {
        if (isLive) live_result.value = "/"
        return "/"
    }

    // 1. Initial cleanup and standard LaTeX mapping
    var cleanExpr = latex
        .replace("\\frac{", "(")
        .replace("}{", ")/(")
        .replace("\\sqrt[", "root(")
        .replace("]{", ",")
        .replace("\\sqrt{", "sqrt(")
        .replace("\\log_{", "log(")
        .replace("}(", ",")
        .replace("\\sin", "sin")
        .replace("\\cos", "cos")
        .replace("\\tan", "tan")
        .replace("\\sinh", "sinh")
        .replace("\\cosh", "cosh")
        .replace("\\tanh", "tanh")
        .replace("\\ln", "ln")
        .replace("\\pi", "pi")
        .replace("\\times", "*")
        .replace("\\div", "/")
        .replace("\\bmod", "#")
        .replace("\\left|", "abs(")
        .replace("\\right|", ")")
        .replace("^{", "^(")
        .replace("_{", "_(")
        .replace("{", "(")
        .replace("}", ")")
        .replace(" ", "")
        .replace("\\le", "<=")
        .replace("\\ge", ">=")

    cleanExpr = cleanExpr.replace("()", "0")
    cleanExpr = handleImplicitMultiplication(cleanExpr)

    val variables = listOf("x", "y", "z").filter { cleanExpr.contains(it) }
    val ops = listOf("<=", ">=", "<", ">", "=")
    val op = ops.find { cleanExpr.contains(it) }

    // 3. Solving Mode (Equations and Inequalities)
    if (variables.size == 1 && op != null) {
        val variable = variables[0]
        val parts = cleanExpr.split(op)
        if (parts.size == 2) {
            val left = parts[0]
            val right = parts[1]
            if (left.isNotEmpty() && right.isNotEmpty()) {
                val funcExpr = "($left)-($right)"
                
                if (op == "=") {
                    val roots = findMultipleRoots(funcExpr, variable)
                    if (roots.isNotEmpty()) {
                        val result = if (roots.size == 1) {
                            "$variable = ${formatResult(roots[0])}"
                        } else {
                            roots.mapIndexed { index, root ->
                                "${variable}_{${index + 1}} = ${formatResult(root)}"
                            }.joinToString(", ")
                        }
                        if (!isLive) previous_ans.value = result
                        else live_result.value = result
                        return result
                    }
                } else {
                    // Inequality solving (Testing intervals)
                    val roots = findMultipleRoots(funcExpr, variable)
                    val uniqueRoots = roots.sorted()
                    
                    val tester = Expression(cleanExpr, Argument(variable, 0.0))
                    val intervals = mutableListOf<String>()
                    val points = mutableListOf<Double>()
                    points.add(Double.NEGATIVE_INFINITY)
                    points.addAll(uniqueRoots)
                    points.add(Double.POSITIVE_INFINITY)
                    
                    for (i in 0 until points.size - 1) {
                        val a = points[i]
                        val b = points[i+1]
                        
                        // Test a point in the interval
                        val testPoint = when {
                            a == Double.NEGATIVE_INFINITY && b == Double.POSITIVE_INFINITY -> 0.0
                            a == Double.NEGATIVE_INFINITY -> b - 1.0
                            b == Double.POSITIVE_INFINITY -> a + 1.0
                            else -> (a + b) / 2.0
                        }
                        
                        tester.setArgumentValue(variable, testPoint)
                        if (tester.calculate() == 1.0) {
                            val openSymbol = if (op == "<" || op == ">") "(" else "["
                            val closeSymbol = if (op == "<" || op == ">") ")" else "]"
                            
                            val startPart = if (a == Double.NEGATIVE_INFINITY) "(-inf" else "$openSymbol${formatResult(a)}"
                            val endPart = if (b == Double.POSITIVE_INFINITY) "inf)" else "${formatResult(b)}$closeSymbol"
                            intervals.add("$startPart, $endPart")
                        }
                    }
                    
                    if (intervals.isNotEmpty()) {
                        val result = "$variable ∈ " + intervals.joinToString(" ∪ ")
                        if (!isLive) previous_ans.value = result
                        else live_result.value = result
                        return result
                    }
                }
            }
        }
        if (isLive) live_result.value = "/"
        return "/"
    }

    // 4. Constant Comparison Mode
    if (op != null && variables.isEmpty()) {
        val cleanComp = cleanExpr.replace("=", "==")
        val checker = Expression(cleanComp)
        val result = checker.calculate()
        val out = if (result == 1.0) "True" else "False"
        if (!isLive) previous_ans.value = out
        else live_result.value = out
        return out
    }

    // 5. Basic Evaluation Mode
    val e = Expression(cleanExpr)
    e.addArguments(Argument("x", 0.0), Argument("y", 0.0), Argument("z", 0.0))
    val result = e.calculate()
    
    return if (result.isNaN()) {
        if (isLive) live_result.value = "/"
        "/"
    } else {
        val out = formatResult(result)
        if (!isLive) previous_ans.value = out
        else live_result.value = out
        out
    }
}

private fun findMultipleRoots(funcExpr: String, variable: String): List<Double> {
    val roots = mutableListOf<Double>()
    
    // Scan range with very small steps to catch roots of higher-order polynomials
    // We check for sign changes to identify root intervals
    val start = -100.0
    val end = 100.0
    val step = 0.5 // Granular step for scan
    
    val expr = Expression(funcExpr, Argument(variable, 0.0))
    
    var prevX = start
    expr.setArgumentValue(variable, prevX)
    var prevVal = expr.calculate()
    
    var x = start + step
    while (x <= end) {
        expr.setArgumentValue(variable, x)
        val valAtX = expr.calculate()
        
        // If sign change detected or one point is zero
        if (prevVal * valAtX <= 0) {
            // Refine the root using mXparser solve() in this small interval
            val solver = Expression("solve($funcExpr, $variable, $prevX, $x)")
            val root = solver.calculate()
            if (!root.isNaN()) {
                if (roots.none { Math.abs(it - root) < 1e-6 }) {
                    roots.add(root)
                }
            }
        }
        
        prevX = x
        prevVal = valAtX
        x += step
    }
    
    // Also check large ranges just in case
    val wideRanges = listOf(-10000.0 to -100.0, 100.0 to 10000.0)
    for ((a, b) in wideRanges) {
        val solver = Expression("solve($funcExpr, $variable, $a, $b)")
        val root = solver.calculate()
        if (!root.isNaN() && roots.none { Math.abs(it - root) < 1e-6 }) {
            roots.add(root)
        }
    }
    
    return roots.sorted()
}

private fun handleImplicitMultiplication(expr: String): String {
    var s = expr
    val vars = "([xyz]|pi|e)"
    s = s.replace(Regex("(\\d)$vars"), "$1*$2")
    s = s.replace(Regex("$vars(\\d)"), "$1*$2")
    s = s.replace(Regex("$vars$vars"), "$1*$2")
    s = s.replace(Regex("(\\d)\\("), "$1*(")
    s = s.replace(Regex("$vars\\("), "$1*(")
    s = s.replace(Regex("\\)(\\d)"), ")*$1")
    s = s.replace(Regex("\\)$vars"), ")*$1")
    s = s.replace(Regex("\\)\\("), ")*(")
    return s
}

private fun formatResult(value: Double): String {
    if (value.isInfinite()) return "Infinity"
    if (value.isNaN()) return "/"
    return if (value == Math.floor(value)) {
        value.toLong().toString()
    } else {
        val rounded = Math.round(value * 1e10) / 1e10
        rounded.toString()
    }
}
