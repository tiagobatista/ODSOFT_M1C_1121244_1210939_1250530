package pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.PhoneNumber;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderNumber;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.BirthDateEntity;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.PhoneNumberEntity;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.ReaderDetailsEntity;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.ReaderNumberEntity;
import pt.psoft.g1.psoftg1.shared.model.sql.NameEntity;
import pt.psoft.g1.psoftg1.shared.model.sql.PhotoEntity;
import pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.Mapper.UserEntityMapper;

/**
 * Mapper interface for ReaderDetails conversions
 * Handles mapping between domain model and persistence entity
 */
@Mapper(componentModel = "spring", uses = {UserEntityMapper.class})
public interface ReaderDetailsEntityMapper {

    // Entity to Model conversion
    ReaderDetails toModel(ReaderDetailsEntity entity);

    // Model to Entity conversion with explicit mappings
    @Mapping(target = "readerNumber", source = "readerNumber", qualifiedByName = "convertReaderNumber")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "convertPhoneNumber")
    @Mapping(target = "marketingConsent", source = "marketingConsent")
    @Mapping(target = "thirdPartySharingConsent", source = "thirdPartySharingConsent")
    @Mapping(target = "photo", source = "photo")
    ReaderDetailsEntity toEntity(ReaderDetails model);

    // Photo conversion
    default String map(PhotoEntity photoEntity) {
        return photoEntity == null ? null : photoEntity.getPhotoFile();
    }

    // Name conversion
    default String map(NameEntity nameEntity) {
        return nameEntity == null ? null : nameEntity.getName();
    }

    // PhoneNumber conversions
    @Named("convertPhoneNumber")
    default PhoneNumberEntity convertPhoneNumber(String phoneStr) {
        return phoneStr == null ? null : new PhoneNumberEntity(phoneStr);
    }

    default PhoneNumber map(PhoneNumberEntity entity) {
        return entity == null ? null : new PhoneNumber(entity.getPhoneNumber());
    }

    default PhoneNumberEntity map(PhoneNumber phoneNumber) {
        return phoneNumber == null ? null : new PhoneNumberEntity(phoneNumber.getPhoneNumber());
    }

    @Named("createPhoneNumber")
    default PhoneNumber createPhoneNumber(String phoneStr) {
        return phoneStr == null ? null : new PhoneNumber(phoneStr);
    }

    // ReaderNumber conversions
    @Named("convertReaderNumber")
    default ReaderNumberEntity convertReaderNumber(String readerNumStr) {
        return readerNumStr == null ? null : new ReaderNumberEntity(readerNumStr);
    }

    default ReaderNumber map(ReaderNumberEntity entity) {
        return entity == null ? null : new ReaderNumber(entity.getReaderNumber());
    }

    default ReaderNumber map(int sequenceNumber) {
        return new ReaderNumber(sequenceNumber);
    }

    // BirthDate conversions
    default BirthDate map(BirthDateEntity entity) {
        return entity == null ? null : new BirthDate(entity.getBirthDate().toString());
    }

    default BirthDateEntity map(BirthDate birthDate) {
        if (birthDate == null || birthDate.getBirthDate() == null) return null;
        return new BirthDateEntity(birthDate.getBirthDate());
    }

    @Named("createBirthDate")
    default BirthDate createBirthDate(String dateStr) {
        return new BirthDate(dateStr);
    }
}