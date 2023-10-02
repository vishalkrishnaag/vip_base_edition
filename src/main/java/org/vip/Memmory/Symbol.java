package org.vip.Memmory;

import java.util.List;

public class Symbol {
    public event eventType;
//    Map<String,Object> attributes;
    public List<String> Elements;

    public List<String> getElements() {
        return Elements;
    }

    public void setElements(List<String> Elements) {
        this.Elements = Elements;
    }
    public boolean addElements(String Elements) {
       return this.Elements.add(Elements);
    }
    public boolean removeElements(String Elements) {
        return this.Elements.remove(Elements);
    }
    public boolean fetchElements(String Elements) {
        return this.Elements.contains(Elements);
    }

    public Symbol(event eventType,List<String> elements) {
        this.eventType = eventType;
        this.Elements = elements;
    }
}
