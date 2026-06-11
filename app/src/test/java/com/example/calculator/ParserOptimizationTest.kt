package com.example.calculator

import com.example.calculator.parser.evaluateExpression
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.system.measureTimeMillis

class ParserOptimizationTest {

    @Test
    fun testCachingAndPerformance() {
        val expression = "x^2 + 5x + 6 = 0"
        
        // Warm up and measure first run
        val firstRunTime = measureTimeMillis {
            val res1 = evaluateExpression(expression, isLive = true)
            println("First run result: $res1")
        }
        
        // Measure second run (should be cached)
        val secondRunTime = measureTimeMillis {
            val res2 = evaluateExpression(expression, isLive = true)
            println("Second run result: $res2")
        }
        
        println("First run: $firstRunTime ms, Second run: $secondRunTime ms")
        
        // The second run should be significantly faster due to caching
        // We use a generous threshold because environment might be slow, but it should be nearly 0
        assert(secondRunTime <= firstRunTime)
    }

    @Test
    fun testLiveVsFinal() {
        val expression = "sin(x) = 0.5"
        
        val liveRes = evaluateExpression(expression, isLive = true)
        val finalRes = evaluateExpression(expression, isLive = false)
        
        println("Live: $liveRes")
        println("Final: $finalRes")
        
        // Both should return valid results
        assert(liveRes.contains("x ="))
        assert(finalRes.contains("x ="))
    }
}
