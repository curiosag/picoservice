package nano.ingredients;

import java.io.Serializable;
import java.util.Objects;

public class Address implements Serializable {
    private static final long serialVersionUID = 0L;

    public Long id;

    public String value;

    public String label = "";

    public Address(String value, Long id) {
        this.value = value + "-" + id;
        this.id = id;
    }

    void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(value, address.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.format("{%d %s(%s)}", id, value, label);
    }
}
