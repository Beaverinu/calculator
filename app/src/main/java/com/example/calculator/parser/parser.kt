package com.example.calculator.parser

import androidx.compose.runtime.mutableStateOf
import org.mariuszgromada.math.mxparser.*
import java.util.concurrent.ConcurrentHashMap

val previous_ans = mutableStateOf("/")
val live_result = mutableStateOf("/")
val isResultFinalized = mutableStateOf(false)

private val evaluationCache = ConcurrentHashMap<String, String>()

private val regexDigitVar = Regex("(\\d)([xyz]|pi|e)")
private val regexVarDigit = Regex("([xyz]|pi|e)(\\d)")
private val regexVarVar = Regex("([xyz]|pi|e)([xyz]|pi|e)")
private val regexDigitParen = Regex("(\\d)\\(")
private val regexVarParen = Regex("([xyz]|pi|e)\\(")
private val regexParenVar = Regex("\\)([xyz]|pi|e)")
private val regexParenDigit = Regex("\\)(\\d)")
private val regexParenParen = Regex("\\)\\(")

fun evaluateExpression(latex: String, isLive: Boolean = false): String {
    if (latex.isBlank()) {
        if (isLive) live_result.value = "/"
        return "/"
    }

    val equationStrings = latex.split(" ; ").filter { it.isNotBlank() }
    
    // 1. Detect if this is a System of Equations (multiple equations with variables)
    if (equationStrings.size > 1) {
        val systemResult = solveSystemOfEquations(equationStrings)
        if (systemResult != null) {
            if (!isLive) previous_ans.value = systemResult
            else live_result.value = systemResult
            return systemResult
        }
    }

    // 2. Fallback to independent evaluation for each line
    val results = equationStrings.map { singleLatex ->
        // Check cache for individual line
        val cacheKey = if (isLive) "live_$singleLatex" else "final_$singleLatex"
        evaluationCache[cacheKey]?.let { return@map it }

        val result = evaluateSingleLine(singleLatex, isLive)
        evaluationCache[cacheKey] = result
        result
    }
    
    val combined = results.joinToString(" ; ")
    if (!isLive) previous_ans.value = combined
    else live_result.value = combined
    return combined
}

private fun evaluateSingleLine(singleLatex: String, isLive: Boolean): String {
    val cleanExpr = prepareMathString(singleLatex)
    val vars = listOf("x", "y", "z").filter { cleanExpr.contains(it) }
    val ops = listOf("<=", ">=", "<", ">", "=")
    val op = ops.find { cleanExpr.contains(it) }

    if (vars.size == 1 && op != null) {
        val variable = vars[0]
        val parts = cleanExpr.split(op)
        if (parts.size == 2) {
            val left = parts[0]
            val right = parts[1]
            if (left.isNotEmpty() && right.isNotEmpty()) {
                val funcExpr = "($left)-($right)"
                
                if (op == "=") {
                    val roots = findMultipleRoots(funcExpr, variable, isLive)
                    if (roots.isNotEmpty()) {
                        return if (roots.size == 1) {
                            "$variable = ${formatResult(roots[0])}"
                        } else {
                            roots.mapIndexed { index, root ->
                                "${variable}_{${index + 1}} = ${formatResult(root)}"
                            }.joinToString(", ")
                        }
                    }
                } else {
                    // Inequality interval solving
                    val roots = findMultipleRoots(funcExpr, variable, isLive)
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
                    if (intervals.isNotEmpty()) return "$variable ∈ " + intervals.joinToString(" ∪ ")
                }
            }
        }
        return "/"
    }

    if (op != null && vars.isEmpty()) {
        val result = Expression(cleanExpr.replace("=", "==")).calculate()
        return if (result == 1.0) "True" else if (result == 0.0) "False" else "/"
    }

    val e = Expression(cleanExpr)
    e.addArguments(Argument("x", 0.0), Argument("y", 0.0), Argument("z", 0.0))
    val res = e.calculate()
    return if (res.isNaN()) "/" else formatResult(res)
}

fun prepareMathString(latex: String): String {
    var clean = latex
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
    
    clean = clean.replace("()", "0")
    return handleImplicitMultiplication(clean)
}

private fun solveSystemOfEquations(equations: List<String>): String? {
    val prepared = equations.map { prepareMathString(it) }
    // Only attempt to solve if they all contain '=' and at least one variable
    if (prepared.any { !it.contains("=") }) return null
    
    val vars = listOf("x", "y", "z").filter { v -> prepared.any { it.contains(v) } }
    if (vars.isEmpty()) return null

    try {
        val n = vars.size
        val m = prepared.size
        
        val a = Array(m) { DoubleArray(n) }
        val b = DoubleArray(m)
        
        for (i in 0 until m) {
            val parts = prepared[i].split("=")
            val fExpr = "(${parts[0]}) - (${parts[1]})"
            val exp = Expression(fExpr)
            val argX = Argument("x", 0.0)
            val argY = Argument("y", 0.0)
            val argZ = Argument("z", 0.0)
            exp.addArguments(argX, argY, argZ)
            
            val c = exp.calculate()
            b[i] = -c 
            
            for (j in 0 until n) {
                argX.argumentValue = 0.0
                argY.argumentValue = 0.0
                argZ.argumentValue = 0.0
                when (vars[j]) {
                    "x" -> argX.argumentValue = 1.0
                    "y" -> argY.argumentValue = 1.0
                    "z" -> argZ.argumentValue = 1.0
                }
                a[i][j] = exp.calculate() - c
            }
        }
        
        if (n == m) {
            val solution = solveLinearSystem(a, b)
            if (solution != null) {
                return vars.indices.joinToString(", ") { i ->
                    "${vars[i]} = ${formatResult(solution[i])}"
                }
            }
        }
    } catch (e: Exception) {
        return null
    }
    
    return null
}

private fun solveLinearSystem(a: Array<DoubleArray>, b: DoubleArray): DoubleArray? {
    val n = b.size
    for (i in 0 until n) {
        var max = i
        for (k in i + 1 until n) {
            if (Math.abs(a[k][i]) > Math.abs(a[max][i])) max = k
        }
        val temp = a[i]
        a[i] = a[max]
        a[max] = temp
        val t = b[i]
        b[i] = b[max]
        b[max] = t

        if (Math.abs(a[i][i]) < 1e-10) return null 

        for (k in i + 1 until n) {
            val factor = a[k][i] / a[i][i]
            b[k] -= factor * b[i]
            for (j in i until n) {
                a[k][j] -= factor * a[i][j]
            }
        }
    }

    val x = DoubleArray(n)
    for (i in n - 1 downTo 0) {
        var sum = 0.0
        for (j in i + 1 until n) {
            sum += a[i][j] * x[j]
        }
        x[i] = (b[i] - sum) / a[i][i]
    }
    return x
}

private fun findMultipleRoots(funcExpr: String, variable: String, isLive: Boolean = false): List<Double> {
    val roots = mutableListOf<Double>()
    val start = if (isLive) -50.0 else -100.0
    val end = if (isLive) 50.0 else 100.0
    val step = if (isLive) 2.0 else 0.5
    
    val expr = Expression(funcExpr, Argument(variable, 0.0))
    var prevX = start
    expr.setArgumentValue(variable, prevX)
    var prevVal = expr.calculate()
    
    var x = start + step
    while (x <= end) {
        expr.setArgumentValue(variable, x)
        val valAtX = expr.calculate()
        if (prevVal * valAtX <= 0) {
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
    return roots.sorted()
}

private fun handleImplicitMultiplication(expr: String): String {
    var result = expr
    result = result.replace(regexDigitVar, "$1*$2")
    result = result.replace(regexVarDigit, "$1*$2")
    result = result.replace(regexVarVar, "$1*$2")
    result = result.replace(regexDigitParen, "$1*(")
    result = result.replace(regexVarParen, "$1*(")
    result = result.replace(regexParenVar, ")*$1")
    result = result.replace(regexParenDigit, ")*$1")
    result = result.replace(regexParenParen, ")*(")
    return result
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
