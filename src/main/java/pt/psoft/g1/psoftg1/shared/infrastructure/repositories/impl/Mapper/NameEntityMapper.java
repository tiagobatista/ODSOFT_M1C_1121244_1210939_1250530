package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.Mapper;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.shared.model.Name;
import pt.psoft.g1.psoftg1.shared.model.SQL.NameEntity;

@Mapper(componentModel = "spring")
public interface NameEntityMapper {

    Name toModel(NameEntity entity);

    NameEntity toEntity(Name model);

    default String map(NameEntity value)
    {
        return value == null ? null : value.getName();
    }

    default String map(Name value)
    {
        return value == null ? null : value.getName();
    }

    default Name map(String value)
    {
        return value == null ? null : new Name(value);
    }
}