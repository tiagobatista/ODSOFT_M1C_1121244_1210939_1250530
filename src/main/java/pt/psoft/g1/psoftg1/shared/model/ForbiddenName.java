package pt.psoft.g1.psoftg1.shared.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Domain Model - ForbiddenName (sem anotações JPA)
 */
public class ForbiddenName {

    private Long pk;

    @Getter
    @Setter
    private String forbiddenName;

    public ForbiddenName(String name) {
        this.forbiddenName = name;
    }

    public ForbiddenName() {
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }
}