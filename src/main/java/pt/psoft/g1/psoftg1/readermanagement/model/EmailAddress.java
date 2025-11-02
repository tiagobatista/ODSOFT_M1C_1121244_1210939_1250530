package pt.psoft.g1.psoftg1.readermanagement.model;

import lombok.Getter;

import java.util.regex.Pattern;

@Getter
public class EmailAddress {

    private final String address;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public EmailAddress(String address) {
        if(address == null || !EMAIL_PATTERN.matcher(address).matches()) {
            throw new IllegalArgumentException("Invalid email address: " + address);
        }
        this.address = address;
    }

    @Override
    public String toString() {
        return this.address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailAddress that = (EmailAddress) o;
        return address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }
}