package com.arunge.el.attribute;

/**
 * 
 *<p>Attribute of an entity represented as a single string..<p>
 *
 * @author Andrew Runge
 *
 */
public class StringAttribute implements Attribute {

    private String value;
    
    public StringAttribute(String value) {
        this.value = value;
    }
    
    public String getValueAsStr() {
        return value;
    }
}
