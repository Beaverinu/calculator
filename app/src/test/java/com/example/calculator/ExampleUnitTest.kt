package com.example.calculator

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val e = org.mariuszgromada.math.mxparser.Expression("(-1+sqrt(3)*i)")
        println("Eval: " + e.calculate())
        println(e.getErrorMessage())
    }
}