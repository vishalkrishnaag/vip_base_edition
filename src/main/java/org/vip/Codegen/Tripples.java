package org.vip.Codegen;


public class Tripples<K,V,AV>{
   /*
   key value another key
   * */


    public K key;
    public V value;
    public AV value1;

    public Tripples(K key, V value,AV secondValue) {
        this.key = key;
        this.value = value;
        this.value1 = secondValue;
    }
    public Tripples() {
        this.key = null;
        this.value = null;
        this.value1 = null;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Tripples) {
            Tripples<K, V,AV> other = (Tripples<K, V, AV>) obj;
            return key.equals(other.key) && value.equals(other.value) && value1.equals(other.value1);
        }

        return false;
    }

    public boolean contains(String obj) {
        if(obj.equals(key))
        {
            return true;
        }

        return false;
    }

    public boolean hasValue1(Integer obj) {
        if(obj==value1)
        {
            return true;
        }

        return false;
    }
    public boolean hasValue(Integer obj) {
        if(obj==value)
        {
            return true;
        }

        return false;
    }
    public boolean hasValue1(Boolean obj) {
        if(obj==value1)
        {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode() + value.hashCode()+value1.hashCode();
    }

    @Override
    public String toString() {
        return "(" + key + ", " + value +", " + value1 + ")";
    }
}
