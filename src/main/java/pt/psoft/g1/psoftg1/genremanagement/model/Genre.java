package pt.psoft.g1.psoftg1.genremanagement.model;

import lombok.Getter;

/**
 * Domain Model - Genre (sem anotações de persistência)
 */
public class Genre {

    public static final int GENRE_MAX_LENGTH = 100;

    @Getter
    private Long pk;

    @Getter
    private String genre;

    // Constructor completo para Mappers
    public Genre(Long pk, String genre) {
        this.pk = pk;
        setGenre(genre);
    }

    // Constructor para criar novo Genre
    public Genre(String genre) {
        this(null, genre);
    }

    public Genre() {
    }

    private void setGenre(String genre) {
        if (genre == null)
            throw new IllegalArgumentException("Genre cannot be null");
        if (genre.isBlank())
            throw new IllegalArgumentException("Genre cannot be blank");
        if (genre.length() > GENRE_MAX_LENGTH)
            throw new IllegalArgumentException("Genre has a maximum of 100 characters");
        this.genre = genre;
    }

    // Setter público para MapStruct (sem validação - só para reconstruir do DB)
    public void setGenreInternal(String genre) {
        this.genre = genre;
    }

    // Setter para Mapper
    public void setPk(Long pk) {
        this.pk = pk;
    }

    @Override
    public String toString() {
        return genre;
    }
}