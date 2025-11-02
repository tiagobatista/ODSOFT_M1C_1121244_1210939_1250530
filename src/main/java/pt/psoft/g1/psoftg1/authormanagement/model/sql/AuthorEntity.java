package pt.psoft.g1.psoftg1.authormanagement.model.sql;


import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.shared.model.sql.EntityWithPhotoEntity;
import pt.psoft.g1.psoftg1.shared.model.sql.NameEntity;
import pt.psoft.g1.psoftg1.shared.model.sql.PhotoEntity;

/**
 * SQL/JPA Entity - AuthorEntity
 * Versão JPA do Author (Domain)
 * Extende EntityWithPhotoEntity para herdar campo photoFile
 */
@Entity
@Table(name = "Author")
@Profile("sql-redis")
@Primary
public class AuthorEntity extends EntityWithPhotoEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "AUTHOR_NUMBER")
    @Getter
    private Long authorNumber;

    @Version
    private long version;

    @Embedded
    private NameEntity name;

    @Embedded
    private BioEntity bio;

    protected AuthorEntity() {}

    public AuthorEntity(NameEntity name, BioEntity bio, PhotoEntity photo)
    {
        setName(name);
        setBio(bio);
        setPhoto(photo);
    }

    // Getters
    public Long getVersion() { return version; }
    public NameEntity getName() { return name; }
    public BioEntity getBio() { return bio; }

    // Setters
    public void setName(NameEntity name) { this.name = name; }
    public void setBio(BioEntity bio) { this.bio = bio; }

    @Override
    public String toString() {
        return name != null ? name.toString() : "Unknown Author";
    }
}
