package com.slbmax.metrics.test;

public abstract class Mammal extends Animal {
    private int numberOfLegs;

    public Mammal(String name, int age, String species, int numberOfLegs) {
        super(name, age, species);
        this.numberOfLegs = numberOfLegs;
    }

    public int getNumberOfLegs() { // Defined method
        return numberOfLegs;
    }

    // Overriding move() from Animal
    @Override
    public void move() {
        System.out.println("Mammal is walking/running");
    }

    // Abstract method from Animal must be implemented or stay abstract
    // abstract void makeSound(); // Still abstract here
}