package pt.psoft.g1.psoftg1.shared.model;

import java.nio.file.Path;

/**
 * Domain Value Object - Photo
 * NÃO contém pk (isso é detalhe de persistência)
 */
public class Photo {

    private String photoFile;

    public Photo(Path photoFile) {
        setPhotoFile(photoFile.toString());
    }

    protected Photo() {
        // for frameworks if needed
    }

    private void setPhotoFile(String photofile) {
        this.photoFile = photofile;
    }

    public String getPhotoFile() {
        return this.photoFile;
    }
}