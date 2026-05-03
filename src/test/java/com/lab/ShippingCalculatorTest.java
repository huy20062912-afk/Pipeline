package com.lab;

import org.junit.jupiter.api.Test;
import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.testng.AssertJUnit.assertEquals;

public class ShippingCalculatorTest {

    ShippingCalculator calc = new ShippingCalculator();

    @Test
    void testStandard() {
        assertEquals(15000.0, calc.calculate(5, "STANDARD"));
    }

    @Test
    void testExpress() {
        assertEquals(45000.0, calc.calculate(5, "EXPRESS"));
    }

    @org.testng.annotations.Test
    void testInvalidWeight() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.calculate(-1, "STANDARD"));
    }
}