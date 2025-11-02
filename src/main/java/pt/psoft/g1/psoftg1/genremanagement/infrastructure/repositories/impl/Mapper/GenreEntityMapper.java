package pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Mapper;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.sql.GenreEntity;

/**
 * Mapper para Genre ↔ GenreEntity
 */
@Mapper(componentModel = "spring")
public interface GenreEntityMapper {

    /**
     * Converte GenreEntity para Genre (Domain Model)
     */
    default Genre toModel(GenreEntity entity) {
        if (entity == null) return null;
        return new Genre(entity.getPk(), entity.getGenre());  // ← MUDOU AQUI!
    }

    /**
     * Converte Genre para GenreEntity (JPA Entity)
     */
    default GenreEntity toEntity(Genre model) {
        if (model == null) return null;
        return new GenreEntity(model.getGenre());
    }

    /**
     * Converte GenreEntity.genre (String) para String
     * Usado pelo MapStruct automaticamente
     */
    default String map(GenreEntity entity) {
        return entity == null ? null : entity.getGenre();
    }

    /**
     * Converte Genre para String
     * Usado pelo MapStruct automaticamente
     */
    default String genreToString(Genre genre) {
        return genre == null ? null : genre.getGenre();
    }
}