package com.slbmax.metrics.test;

public class Fish extends Animal {

    public Fish(String name, int age, String species) {
        super(name, age, species);
    }

    @Override
    public void makeSound() {
        System.out.println("..."); // Fish are quiet
    }

     // Overriding move() from Animal
    @Override
    public void move() {
        System.out.println("Fish is swimming");
    }
}