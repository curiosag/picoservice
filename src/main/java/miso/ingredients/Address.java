package miso.ingredients;

import java.util.Objects;

public class Address {

    public String value;

    String sticker = "";

    Address(String value) {
        this.value = value;
    }

    void setSticker(String sticker) {
        this.sticker = " " + sticker;
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
        return "{" + value + sticker + '}';
    }
}
