package com.slbmax.metrics.test;

public class Dog extends Mammal {
    private String breed;

    public Dog(String name, int age, String breed) {
        super(name, age, "Dog", 4); // Mammals usually have 4 legs
        this.breed = breed;
    }

    public String getBreed() { // Defined method
        return breed;
    }

    // Implementing abstract method makeSound() from Animal
    @Override
    public void makeSound() {
        System.out.println("Woof!");
    }

    // Overriding getAge() from Animal (accessible via protected)
    @Override
    protected int getAge() {
        System.out.println("Getting dog's age...");        
        return super.getAge();
    }

    // Defining a new private method
    private void fetch() {
        System.out.println("Fetching!");
    }
}