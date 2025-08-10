package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Test class for Calculator
 */
public class CalculatorTest {
    
    @Test
    public void testAdd() {
        Calculator calc = new Calculator();
        int result = calc.add(2, 3);
        Assertions.assertEquals(5, result);
    }
    
    @Test
    public void testSubtract() {
        Calculator calc = new Calculator();
        int result = calc.subtract(5, 2);
        Assertions.assertEquals(3, result);
    }
    
    @Test
    public void testMultiply() {
        Calculator calc = new Calculator();
        int result = calc.multiply(3, 4);
        Assertions.assertEquals(12, result);
    }
    
    @Test
    public void testDivide() {
        Calculator calc = new Calculator();
        double result = calc.divide(10, 2);
        Assertions.assertEquals(5.0, result, 0.001);
    }
    
    @Test
    public void testDivideByZero() {
        Calculator calc = new Calculator();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            calc.divide(10, 0);
        });
    }
}
