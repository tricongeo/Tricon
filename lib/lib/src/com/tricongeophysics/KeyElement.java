package com.tricongeophysics;

public class KeyElement {
        public String value;
        public String label;
        
        public KeyElement(String v, String l) {
            value = v;
            label = l;
        }
        
        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

        public String getLabel()
        {
            return label;
        }

        public void setLabel(String label)
        {
            this.label = label;
        }

        public KeyElement(){}
    }