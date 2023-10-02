package org.vip.Codegen;

public class Quintet<K,K1,K2,K3,K4> {
    public K k;
    public K1 k1;
    public K2 k2;
    public K3 k3;
    public K4 k4;

    public Quintet(K k, K1 k1, K2 k2, K3 k3, K4 k4) {
        this.k = k;
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
        this.k4 = k4;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Quads) {
            Quintet<K, K1, K2, K3,K4> other = (Quintet<K, K1, K2, K3,K4>) obj;
            return k.equals(other.k) && k1.equals(other.k1) && k2.equals(other.k2) && k3.equals(other.k3)&& k3.equals(other.k4);
        }

        return false;
    }

    public boolean contains(String s) {
        if(k.equals(s))
        {
            return true;
        }
        return false;
    }
    public boolean hasClass(Integer s) {
        if(k2==s)
        {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return k.hashCode() + k1.hashCode()+k2.hashCode()+k3.hashCode()+k4.hashCode();
    }

    @Override
    public String toString() {
        return "(" + k + ", " + k1 +", " + k2 +", " + k3+ ", " + k4+")";
    }

    public String to_String() {
        return "(" + k + ", " + k1 +", " + k2 +", " + k3+", " + k4+ ")";
    }
}
