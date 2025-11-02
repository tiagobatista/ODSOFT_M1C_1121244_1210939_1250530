package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.Mapper;

import org.mapstruct.Mapper;

import pt.psoft.g1.psoftg1.authormanagement.model.Author;

import pt.psoft.g1.psoftg1.authormanagement.model.SQL.AuthorEntity;

import pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.Mapper.NameEntityMapper;
import pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.Mapper.PhotoEntityMapper;


@Mapper(componentModel = "spring", uses = { NameEntityMapper.class, BioEntityMapper.class, PhotoEntityMapper.class})
public interface AuthorEntityMapper
{
    Author toModel(AuthorEntity entity);

    AuthorEntity toEntity(Author model);

}