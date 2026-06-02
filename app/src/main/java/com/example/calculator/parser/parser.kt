package com.example.calculator.parser

import androidx.compose.runtime.mutableStateOf
import org.mariuszgromada.math.mxparser.*

val previous_ans = mutableStateOf("")

fun evaluateExpression(latex: String): String {
    if (latex.isBlank()) return ""

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

    // 2. Identify the variable and operator
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
                
                // Using solve(f(x), x, a, b) from mXparser tutorial
                val ranges = listOf(-10.0 to 10.0, -100.0 to 100.0, -1000.0 to 1000.0, -100000.0 to 100000.0)
                var boundary = Double.NaN
                
                for ((a, b) in ranges) {
                    val solver = Expression("solve($funcExpr, $variable, $a, $b)")
                    boundary = solver.calculate()
                    if (!boundary.isNaN()) break
                }

                if (!boundary.isNaN()) {
                    val bFormatted = formatResult(boundary)
                    
                    if (op == "=") {
                        val result = "$variable = $bFormatted"
                        previous_ans.value = bFormatted
                        return result
                    } else {
                        // Inequality testing - must add argument first so mXparser knows the variable
                        val arg = Argument(variable, 0.0)
                        val tester = Expression(cleanExpr, arg)
                        
                        arg.argumentValue = boundary - 1.0
                        val isSmallTrue = tester.calculate() == 1.0
                        
                        arg.argumentValue = boundary + 1.0
                        val isBigTrue = tester.calculate() == 1.0
                        
                        val openSymbol = if (op == "<" || op == ">") "(" else "["
                        val closeSymbol = if (op == "<" || op == ">") ")" else "]"
                        
                        val result = when {
                            isSmallTrue && !isBigTrue -> "$variable is set of (-inf, $bFormatted$closeSymbol"
                            !isSmallTrue && isBigTrue -> "$variable is set of $openSymbol$bFormatted, inf)"
                            isSmallTrue && isBigTrue -> "All real numbers"
                            else -> "No solution"
                        }
                        previous_ans.value = result
                        return result
                    }
                }
            }
        }
    }

    // 4. Constant Comparison Mode (e.g., 5 = 5)
    if (op != null && variables.isEmpty()) {
        val cleanComp = cleanExpr.replace("=", "==")
        val checker = Expression(cleanComp)
        val result = checker.calculate()
        val out = if (result == 1.0) "True" else "False"
        previous_ans.value = out
        return out
    }

    // 5. Basic Evaluation Mode
    val e = Expression(cleanExpr)
    e.addArguments(Argument("x", 0.0), Argument("y", 0.0), Argument("z", 0.0))
    val result = e.calculate()
    
    return if (result.isNaN()) {
        "Error"
    } else {
        val out = formatResult(result)
        previous_ans.value = out
        out
    }
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
    if (value.isNaN()) return "Error"
    return if (value == Math.floor(value)) {
        value.toLong().toString()
    } else {
        val rounded = Math.round(value * 1e10) / 1e10
        rounded.toString()
    }
}
