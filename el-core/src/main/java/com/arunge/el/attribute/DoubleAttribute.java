package com.arunge.el.attribute;

public class DoubleAttribute implements Attribute {

    private double value;
    
    public DoubleAttribute(double value) {
        this.value = value;
    }
    
    public double getValue() {
        return value;
    }
    
    @Override
    public String getValueAsStr() {
        return null;
    }

}
