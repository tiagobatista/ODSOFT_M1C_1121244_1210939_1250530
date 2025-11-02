package pt.psoft.g1.psoftg1.usermanagement.model;

public class Librarian extends User {

    protected Librarian() {
        // for ORM only
    }

    public Librarian(String username, String password) {
        super(username, password);
    }

    public static Librarian newLibrarian(final String username, final String password, final String name) {
        final var u = new Librarian(username, password);
        u.setName(name);
        u.addAuthority(new Role(Role.LIBRARIAN));
        return u;
    }
}