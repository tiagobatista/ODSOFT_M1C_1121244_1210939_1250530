package pt.psoft.g1.psoftg1.shared.model;

import lombok.Getter;

/**
 * Domain Value Object - Name (sem anotações JPA)
 */
@Getter
public class Name {

    private String name;

    public Name(String name) {
        setName(name);
    }

    protected Name() {
    }

    public void setName(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");
        if (name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank, nor only white spaces");
        if (!StringUtilsCustom.isAlphanumeric(name))
            throw new IllegalArgumentException("Name can only contain alphanumeric characters");

        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}