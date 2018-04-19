package com.arunge.el.attribute;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 *<p>class_comment_here<p>
 *
 * @author Andrew Runge
 *
 */
public class SparseVectorAttribute implements Attribute {

    private Map<Integer, Double> vector;
    
    public SparseVectorAttribute(Map<Integer, Double> vector) {
        this.vector = vector;
    }

    public Map<Integer, Double> getValue() {
        return vector;
    }
    
    @Override
    public String getValueAsStr() {
        StringBuilder sb = new StringBuilder()
                .append("[");
        for(Map.Entry<Integer, Double> e : vector.entrySet()) {
            sb.append(e.getKey() + " : " + e.getValue() + ",");
        }
        return sb.append("]").toString();
    }
    
    public static SparseVectorAttribute valueOf(Map<Integer, Double> vector) {
        return new SparseVectorAttribute(vector);
    }
    
    public static SparseVectorAttribute valueOf(double[] vector) {
        Map<Integer, Double> sparseVec = new HashMap<>();
        for(int i = 0; i < vector.length; i++) {
            if(vector[i] != 0.0) {
                sparseVec.put(i, vector[i]);
            }
        }
        return new SparseVectorAttribute(sparseVec);
    }
    
}
