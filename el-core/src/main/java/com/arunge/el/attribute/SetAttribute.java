package com.arunge.el.attribute;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SetAttribute implements Attribute {

    private Set<String> values;
    
    public SetAttribute(Collection<String> values) {
        this.values = new HashSet<>(values);
    }
    
    public SetAttribute(String[] values) {
        this.values = Arrays.stream(values).collect(Collectors.toSet());
    }
    
    public Set<String> getSetValue() {
        return values;
    }

    public String getValueAsStr() {
        Optional<String> str = values.stream().reduce((a, b) -> a + ", " + b);
        if(str.isPresent()) {
            return str.get();
        }
        return "";
    }
    
}
