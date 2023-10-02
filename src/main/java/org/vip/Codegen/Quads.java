package org.vip.Codegen;
import org.jetbrains.annotations.NotNull;
public class Quads<K,K1,K2,K3>{
   /*
   key value another key
   * */


   public K key;
   public K1 k1;
   public K2 k2;
   public K3 k3;

    public Quads(K key, K1 key1,K2 key2,K3 key3) {
        this.key = key;
        this.k1 = key1;
        this.k2 = key2;
        this.k3 = key3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Quads) {
            Quads<K, K1,K2,K3> other = (Quads<K, K1, K2, K3>) obj;
            return key.equals(other.key) && k1.equals(other.k1) && k2.equals(other.k2) && k3.equals(other.k3);
        }

        return false;
    }

    public boolean contains(String s) {
            if(key.equals(s))
            {
                return true;
            }
        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode() + k1.hashCode()+k2.hashCode()+k3.hashCode();
    }

    @Override
    public String toString() {
        return "(" + key + ", " + k1 +", " + k2 +", " + k3+ ")";
    }

    public String to_String() {
        return "(" + key + ", " + k1 +", " + k2 +", " + k3+ ")";
    }
}

