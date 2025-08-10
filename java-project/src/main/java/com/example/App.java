package com.example;

import java.util.List;
import java.util.Optional;

/**
 * A sample application demonstrating various Java features
 */
public class App {
    
    public static void main(String[] args) {
        App app = new App();
        app.demonstrateFeatures();
    }
    
    public void demonstrateFeatures() {
        // Using modern Java features
        var items = List.of("Hello", "World", "Java", "21");
        
        // Java 21+ features with enhanced Optional
        Optional<String> first = items.stream().findFirst();
        first.ifPresent(item -> System.out.println("First item: " + item));
        
        // Modern enhanced for loop with var
        for (var item : items) {
            System.out.println("Item: " + item);
        }
        
        // Using text blocks (Java 15+) and string interpolation concepts
        var textBlock = """
                Welcome to Java 21!
                This project has been upgraded from Java 8.
                Enjoy the modern language features!
                """;
        System.out.println(textBlock);
        
        // Pattern matching with instanceof (Java 16+)
        Object obj = "Hello Java 21";
        if (obj instanceof String str) {
            System.out.println("String length: " + str.length());
        }
        
        // Calculate something with enhanced features
        Calculator calc = new Calculator();
        var result = calc.add(5, 3);
        System.out.println("5 + 3 = " + result);
        
        // Using switch expressions (Java 14+)
        var dayType = switch (java.time.LocalDate.now().getDayOfWeek()) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
            case SATURDAY, SUNDAY -> "Weekend";
        };
        System.out.println("Today is a: " + dayType);
    }
}
