package pt.psoft.g1.psoftg1.usermanagement.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import pt.psoft.g1.psoftg1.shared.model.Name;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
public class User implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String createdBy;
    private String modifiedBy;

    @Setter
    private boolean enabled = true;

    @Setter
    private String username;

    private String password;

    private Name name;

    private final Set<Role> authorities = new HashSet<>();

    protected User() {
        // for ORM only
    }

    public User(final String username, final String password) {
        this.username = username;
        setPassword(password);
    }

    public static User newUser(final String username, final String password, final String name) {
        final var u = new User(username, password);
        u.setName(name);
        return u;
    }

    public static User newUser(final String username, final String password, final String name, final String role) {
        final var u = new User(username, password);
        u.setName(name);
        u.addAuthority(new Role(role));
        return u;
    }

    public void setPassword(final String password) {
        Password passwordCheck = new Password(password);
        this.password = password;
    }

    public void addAuthority(final Role r) {
        authorities.add(r);
    }

    public void setName(String name) {
        this.name = new Name(name);
    }

    // Setters para reconstrução do domain a partir da entidade
    public void setId(Long id) {
        this.id = id;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    // Implementação dos métodos de UserDetails
    @Override
    public boolean isAccountNonExpired() {
        return isEnabled();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isEnabled();
    }
}