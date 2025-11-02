package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.Mapper;

import org.mapstruct.Mapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.sql.FineEntity;

@Mapper(componentModel = "spring", uses = {LendingEntityMapper.class})
public interface FineEntityMapper {


    Fine toModel(FineEntity entity);


    FineEntity toEntity(Fine model);


}