package pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.Mapper.AuthorEntityMapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.BookEntity;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.DescriptionEntity;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.IsbnEntity;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.TitleEntity;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Mapper.GenreEntityMapper;
import pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.Mapper.PhotoEntityMapper;

/**
 * MapStruct interface for Book entity conversions
 * Maps between Book domain model and BookEntity JPA entity
 */
@Mapper(componentModel = "spring", uses = {GenreEntityMapper.class, AuthorEntityMapper.class, PhotoEntityMapper.class})
public interface BookEntityMapper {

    // Entity to Model mapping
    @Mapping(target = "photoURI", source = "photo")
    Book toModel(BookEntity entity);

    // Model to Entity mapping
    BookEntity toEntity(Book model);

    // Title entity conversion
    default String map(TitleEntity titleEntity) {
        return titleEntity == null ? null : titleEntity.getTitle();
    }

    // ISBN entity conversion
    default String map(IsbnEntity isbnEntity) {
        return isbnEntity == null ? null : isbnEntity.getIsbn();
    }

    // Description entity conversion
    default String map(DescriptionEntity descEntity) {
        return descEntity == null ? null : descEntity.getDescription();
    }
}