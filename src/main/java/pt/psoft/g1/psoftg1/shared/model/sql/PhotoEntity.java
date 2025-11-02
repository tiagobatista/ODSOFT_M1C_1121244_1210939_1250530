package pt.psoft.g1.psoftg1.shared.model.sql;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Entity
@Table(name="Photo")
@Profile("sql-redis")
@Primary
public class PhotoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long pk;


    @Getter
    @Setter
    private String photoFile;

    protected PhotoEntity() { }

    public PhotoEntity(String photoFile)
    {
        setPhotoFile(photoFile);
    }
}
