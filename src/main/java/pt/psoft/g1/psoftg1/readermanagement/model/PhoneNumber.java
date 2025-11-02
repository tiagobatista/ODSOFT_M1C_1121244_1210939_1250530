package pt.psoft.g1.psoftg1.readermanagement.model;

import lombok.Getter;

@Getter
public class PhoneNumber {

    private final String phoneNumber;

    public PhoneNumber(String phoneNumber) {
        if(phoneNumber == null ||
                !(phoneNumber.startsWith("9") || phoneNumber.startsWith("2")) ||
                phoneNumber.length() != 9) {
            throw new IllegalArgumentException("Phone number is not valid: " + phoneNumber);
        }
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return this.phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return phoneNumber.equals(that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return phoneNumber.hashCode();
    }
}