package com.example.calculator.parser

import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.Argument

class Complex(val r: Double, val i: Double) {
    operator fun plus(other: Complex) = Complex(r + other.r, i + other.i)
    operator fun minus(other: Complex) = Complex(r - other.r, i - other.i)
    operator fun times(other: Complex) = Complex(r * other.r - i * other.i, r * other.i + i * other.r)
    operator fun div(other: Complex): Complex {
        val den = other.r * other.r + other.i * other.i
        return Complex((r * other.r + i * other.i) / den, (i * other.r - r * other.i) / den)
    }
    fun length() = Math.sqrt(r * r + i * i)
}

fun findPolynomialRoots(funcExpr: String, variable: String): List<Complex>? {
    val maxDegree = 10
    val N = maxDegree + 2
    val y = DoubleArray(N)
    for (i in 0 until N) {
        val arg = Argument(variable, i.toDouble())
        y[i] = Expression(funcExpr, arg).calculate()
    }
    
    var degree = -1
    val diffs = Array(N) { DoubleArray(N) }
    for (i in 0 until N) diffs[0][i] = y[i]
    
    for (d in 1 until N) {
        var allZero = true
        for (i in 0 until N - d) {
            diffs[d][i] = diffs[d-1][i+1] - diffs[d-1][i]
            if (Math.abs(diffs[d][i]) > 1e-4) allZero = false
        }
        if (allZero) {
            degree = d - 1
            break
        }
    }
    
    if (degree <= 0) return null 
    
    val n = degree + 1
    val matrix = Array(n) { DoubleArray(n + 1) }
    for (i in 0 until n) {
        var xVal = 1.0
        for (j in 0 until n) {
            matrix[i][j] = xVal
            xVal *= i
        }
        matrix[i][n] = y[i]
    }
    
    for (i in 0 until n) {
        var maxRow = i
        for (k in i + 1 until n) {
            if (Math.abs(matrix[k][i]) > Math.abs(matrix[maxRow][i])) maxRow = k
        }
        val temp = matrix[i]
        matrix[i] = matrix[maxRow]
        matrix[maxRow] = temp
        
        for (k in i + 1 until n) {
            val factor = matrix[k][i] / matrix[i][i]
            for (j in i..n) {
                matrix[k][j] -= factor * matrix[i][j]
            }
        }
    }
    
    val coeffs = DoubleArray(n)
    for (i in n - 1 downTo 0) {
        var sum = 0.0
        for (j in i + 1 until n) {
            sum += matrix[i][j] * coeffs[j]
        }
        coeffs[i] = (matrix[i][n] - sum) / matrix[i][i]
    }
    
    val testArg = Argument(variable, 2.5)
    val expected = Expression(funcExpr, testArg).calculate()
    var actual = 0.0
    var p = 1.0
    for (c in coeffs) {
        actual += c * p
        p *= 2.5
    }
    if (Math.abs(expected - actual) > 1e-3) return null 
    
    val an = coeffs[degree]
    val c = DoubleArray(degree + 1) { coeffs[it] / an }
    
    val roots = Array(degree) { idx ->
        val angle = 2.0 * Math.PI * idx / degree
        val radius = 2.0 
        Complex(radius * Math.cos(angle) + 0.01*(idx+1), radius * Math.sin(angle) - 0.01*(idx+1)) 
    }
    
    val maxIter = 2000
    for (iter in 0 until maxIter) {
        var maxDiff = 0.0
        for (i in 0 until degree) {
            var polyVal = Complex(c[0], 0.0)
            var zPower = Complex(1.0, 0.0)
            val z = roots[i]
            for (j in 1..degree) {
                zPower *= z
                polyVal += Complex(c[j], 0.0) * zPower
            }
            
            var denominator = Complex(1.0, 0.0)
            for (j in 0 until degree) {
                if (i != j) {
                    denominator *= (z - roots[j])
                }
            }
            
            val offset = polyVal / denominator
            roots[i] = roots[i] - offset
            maxDiff = Math.max(maxDiff, offset.length())
        }
        if (maxDiff < 1e-12) break
    }
    
    // Sort roots to have real roots first, then complex conjugates
    val sortedRoots = roots.sortedWith(Comparator { a, b ->
        val aIsReal = Math.abs(a.i) < 1e-6
        val bIsReal = Math.abs(b.i) < 1e-6
        if (aIsReal && !bIsReal) -1
        else if (!aIsReal && bIsReal) 1
        else {
            if (Math.abs(a.r - b.r) > 1e-6) a.r.compareTo(b.r)
            else a.i.compareTo(b.i)
        }
    })
    
    return sortedRoots
}

fun formatComplexRoot(c: Complex): String {
    val re = if (Math.abs(c.r) < 1e-6) 0.0 else c.r
    val im = if (Math.abs(c.i) < 1e-6) 0.0 else c.i
    
    fun formatDouble(value: Double): String {
        if (value.isInfinite()) return "Infinity"
        if (value.isNaN()) return "/"
        return if (value == Math.floor(value)) {
            value.toLong().toString()
        } else {
            val rounded = Math.round(value * 1e6) / 1e6
            rounded.toString()
        }
    }

    if (im == 0.0) return formatDouble(re)
    val reStr = if (re == 0.0) "" else formatDouble(re)
    val imVal = Math.abs(im)
    val imStr = if (imVal == 1.0) "i" else formatDouble(imVal) + "i"
    
    if (reStr.isEmpty()) {
        return if (im > 0) imStr else "-" + imStr
    } else {
        return if (im > 0) reStr + " + " + imStr else reStr + " - " + imStr
    }
}
