package pt.psoft.g1.psoftg1.authormanagement.model;

import org.hibernate.StaleObjectStateException;
import pt.psoft.g1.psoftg1.authormanagement.services.UpdateAuthorRequest;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;
import pt.psoft.g1.psoftg1.shared.model.Name;

import java.util.Objects;

/**
 * Domain Model - Author (sem anotações de persistência)
 */
public class Author extends EntityWithPhoto {

    private Long authorNumber;
    private long version;
    private Name name;
    private Bio bio;

    public Author() {
    }

    public Author(String name, String bio, String photoURI) {
        this(new Name(name), new Bio(bio), photoURI);
    }

    public Author(Name name, Bio bio, String photoURI) {
        setName(name);
        setBio(bio);
        setPhotoInternal(photoURI);
        this.version = 0L;
    }

    // Getters
    public Long getAuthorNumber() {
        return authorNumber;
    }

    public Long getId() {
        return authorNumber;
    }

    public long getVersion() {
        return version;
    }

    public Name getName() {
        return name;
    }

    public Bio getBio() {
        return bio;
    }

    // Setters
    public void setAuthorNumber(Long authorNumber) {  // ← ADICIONA ESTE
        this.authorNumber = authorNumber;
    }

    public void setVersion(long version) {  // ← ADICIONA ESTE
        this.version = version;
    }

    public void setName(Name name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }

    public void setName(String name) {
        setName(new Name(name));
    }

    public void setBio(Bio bio) {
        if (bio == null) {
            throw new IllegalArgumentException("Bio cannot be null");
        }
        this.bio = bio;
    }

    public void setBio(String bio) {
        setBio(new Bio(bio));
    }

    // Lógica de negócio
    public void applyPatch(final long expectedVersion, final UpdateAuthorRequest request) {
        if (!Objects.equals(this.version, expectedVersion)) {
            throw new StaleObjectStateException("Object was already modified by another user", this.authorNumber);
        }

        if (request.getName() != null) {
            setName(new Name(request.getName()));
        }

        if (request.getBio() != null) {
            setBio(new Bio(request.getBio()));
        }

        if (request.getPhotoURI() != null) {
            setPhotoInternal(request.getPhotoURI());
        }
    }

    public void removePhoto(long expectedVersion) {
        if (!Objects.equals(expectedVersion, this.version)) {
            throw new ConflictException("Provided version does not match latest version of this object");
        }
        setPhotoInternal(null);
    }
}