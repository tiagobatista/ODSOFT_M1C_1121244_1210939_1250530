package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.Mapper;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.model.SQL.ForbiddenNameEntity;

@Mapper(componentModel = "spring")
public interface ForbiddenNameEntityMapper
{
    ForbiddenName toModel(ForbiddenNameEntity entity);
    ForbiddenNameEntity toEntity(ForbiddenName model);

    
}
