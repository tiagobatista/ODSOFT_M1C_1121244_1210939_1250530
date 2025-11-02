package pt.psoft.g1.psoftg1.shared.model.sql;

import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("sql-redis")
@Primary
@Getter
@MappedSuperclass
public abstract class EntityWithPhotoEntity
{
    @Nullable
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="photo_id", nullable = true)
    @Setter
    @Getter
    private PhotoEntity photo;

    protected EntityWithPhotoEntity() { }

    public EntityWithPhotoEntity(PhotoEntity photo)
    {
        setPhotoInternal(photo);
    }

    protected void setPhotoInternal(PhotoEntity photoURI)
    {
        this.photo = photoURI;
    }
    protected void setPhotoInternal(String photo)
    {
        setPhotoInternal(new PhotoEntity(photo));
    }
}
