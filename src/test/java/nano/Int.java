package nano;

import java.util.Objects;

class Int {
    public Integer value;

    public void setValue(Integer value) {
        this.value = value;
    }

    public Int(Integer value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Int anInt = (Int) o;
        return value.equals(anInt.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public static Int Int(int i) {
        return new Int(i);
    }

}
