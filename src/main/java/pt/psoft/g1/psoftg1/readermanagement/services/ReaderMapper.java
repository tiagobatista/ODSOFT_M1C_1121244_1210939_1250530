package pt.psoft.g1.psoftg1.readermanagement.services;

import org.mapstruct.*;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.PhoneNumber;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderNumber;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.util.List;

/**
 * Brief guide:
 * <a href="https://www.baeldung.com/mapstruct">https://www.baeldung.com/mapstruct</a>
 */
@Mapper(componentModel = "spring")
public abstract class ReaderMapper {

    @Mapping(target = "username", source = "request.username")
    @Mapping(target = "password", source = "request.password")
    @Mapping(target = "name", source = "request.fullName")
    public abstract Reader createReader(CreateReaderRequest request);

    // MapStruct não funciona bem com construtores complexos, usa método manual
    public ReaderDetails createReaderDetails(
            int readerNumber,
            Reader reader,
            CreateReaderRequest request,
            String photoURI,
            List<Genre> interestList) {

        return new ReaderDetails(
                readerNumber,
                reader,
                request.getBirthDate(),
                request.getPhoneNumber(),
                request.isGdpr(),
                request.isMarketing(),
                request.isThirdParty(),
                photoURI,
                interestList
        );
    }
}