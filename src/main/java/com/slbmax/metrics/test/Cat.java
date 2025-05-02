package com.slbmax.metrics.test;

public class Cat extends Mammal {

    public Cat(String name, int age) {
       super(name, age, "Cat", 4);
   }

   @Override
   public void makeSound() {
       System.out.println("Meow!");
   }


}
