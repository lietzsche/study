package com.example.demo;

public class Sum implements Expression {
    public Sum(Money augend, Money addend) {
        this.augend = augend;
        this.addend = addend;
    }
    Money augend;
    Money addend;

    @Override
    public Money reduce(String to) {
        int amount = augend.amount + addend.amount;
        return new Money(amount, to);
    }
}
