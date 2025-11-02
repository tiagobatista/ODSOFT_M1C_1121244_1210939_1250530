package pt.psoft.g1.psoftg1.usermanagement.model.SQL;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.usermanagement.model.Role;

@Profile("sql-redis")
@Primary
@Entity
@Table(name = "T_READER")
public class ReaderEntity extends UserEntity {

    protected ReaderEntity() {
        // for ORM only
    }

    public ReaderEntity(String username, String password) {
        super(username, password);
        this.addAuthority(new Role(Role.READER));
    }

    public static ReaderEntity newReader(final String username, final String password, final String name) {
        final var u = new ReaderEntity(username, password);
        u.setName(name);
        return u;
    }
}
