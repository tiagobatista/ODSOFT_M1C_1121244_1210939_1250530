package pt.psoft.g1.psoftg1.usermanagement.model.sql;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.usermanagement.model.Role;

@Profile("sql-redis")
@Primary
@Entity
@Table(name = "T_LIBRARIAN")
public class LibrarianEntity extends UserEntity {

    protected LibrarianEntity() {
        // for ORM only
    }

    public LibrarianEntity(String username, String password) {
        super(username, password);
    }

    public static LibrarianEntity newLibrarian(final String username, final String password, final String name) {
        final var u = new LibrarianEntity(username, password);
        u.setName(name);
        u.addAuthority(new Role(Role.LIBRARIAN));
        return u;
    }
}
