package com.slbmax.metrics.test;

public abstract class Animal {
    private String name;
    protected int age;
    public String species;

    public Animal(String name, int age, String species) {
        this.name = name;
        this.age = age;
        this.species = species;
    }

    public String getName() { // Public method
        return name;
    }

    protected int getAge() { // Protected method
        return age;
    }

    private void internalMethod() { // Private method
        System.out.println("Internal animal logic");
    }

    public abstract void makeSound(); // Abstract method

    public void move() { // Defined method
        System.out.println("Animal is moving");
    }
}