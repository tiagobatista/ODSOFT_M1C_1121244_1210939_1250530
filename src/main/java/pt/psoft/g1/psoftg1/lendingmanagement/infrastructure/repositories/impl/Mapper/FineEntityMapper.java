package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pt.psoft.g1.psoftg1.authormanagement.model.Bio;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.SQL.FineEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.model.SQL.LendingEntity;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;

@Mapper(componentModel = "spring", uses = {LendingEntityMapper.class})
public interface FineEntityMapper {


    Fine toModel(FineEntity entity);


    FineEntity toEntity(Fine model);


}