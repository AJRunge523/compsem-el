package com.arunge.el.attribute;

public class DenseVectorAttribute implements Attribute {
    
    private double[] vector;
    
    public DenseVectorAttribute(double[] vector) {
        this.vector = vector;
    }

    public double[] getValue() {
        return vector;
    }
    
    @Override
    public String getValueAsStr() {
        StringBuilder sb = new StringBuilder()
                .append("[");
        for(double d : vector) {
            sb.append(d + ",");
        }
        return sb.append("]").toString();
    }
    
    public static DenseVectorAttribute valueOf(double[] vector) {
        return new DenseVectorAttribute(vector);
    }
}
