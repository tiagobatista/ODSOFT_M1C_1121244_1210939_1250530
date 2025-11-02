package pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.Mapper;

import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.PhoneNumber;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderNumber;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper para converter ReaderDetails para/de formato Redis Hash
 */
@Component
public class ReaderRedisMapper {

    /**
     * Converte ReaderDetails para Map (formato Redis Hash)
     */
    public Map<String, String> toRedisHash(ReaderDetails reader) {
        if (reader == null) {
            return null;
        }

        Map<String, String> hash = new HashMap<>();

        // Primary Key
        if (reader.getPk() != null) {
            hash.put("pk", reader.getPk().toString());
        }

        // Version
        if (reader.getVersion() != null) {
            hash.put("version", reader.getVersion().toString());
        }

        // ReaderNumber
        if (reader.getReaderNumber() != null) {
            hash.put("readerNumber", reader.getReaderNumber());
        }

        // PhoneNumber
        if (reader.getPhoneNumber() != null) {
            hash.put("phoneNumber", reader.getPhoneNumber());
        }

        // BirthDate
        if (reader.getBirthDate() != null) {
            hash.put("birthDate", reader.getBirthDate().toString());
        }

        // Consents
        hash.put("gdprConsent", String.valueOf(reader.isGdprConsent()));
        hash.put("marketingConsent", String.valueOf(reader.isMarketingConsent()));
        hash.put("thirdPartySharingConsent", String.valueOf(reader.isThirdPartySharingConsent()));

        // Photo
        if (reader.getPhoto() != null && reader.getPhoto().getPhotoFile() != null) {
            hash.put("photo", reader.getPhoto().getPhotoFile());
        }

        // Reader (User) data
        if (reader.getReader() != null) {
            Reader user = reader.getReader();

            if (user.getUsername() != null) {
                hash.put("reader_username", user.getUsername());
            }

            // Name - converter objeto Name para String usando toString()
            if (user.getName() != null) {
                hash.put("reader_name", user.getName().toString());
            }

            if (user.getId() != null) {
                hash.put("reader_id", user.getId().toString());
            }

            // Guardar password hash (necessário para reconstituir o objeto)
            if (user.getPassword() != null) {
                hash.put("reader_password", user.getPassword());
            }
        }

        return hash;
    }

    /**
     * Converte Map (Redis Hash) para ReaderDetails
     */
    public ReaderDetails fromRedisHash(Map<Object, Object> hash) {
        if (hash == null || hash.isEmpty()) {
            return null;
        }

        try {
            // Criar Reader primeiro (precisa de username e password)
            Reader user = null;
            if (hash.containsKey("reader_username") && hash.containsKey("reader_password")) {
                String username = hash.get("reader_username").toString();
                String password = hash.get("reader_password").toString();

                // Usar factory method do Reader
                String name = hash.containsKey("reader_name") ? hash.get("reader_name").toString() : null;
                user = Reader.newReader(username, password, name);

                // Definir ID se existir
                if (hash.containsKey("reader_id")) {
                    user.setId(Long.parseLong(hash.get("reader_id").toString()));
                }
            }

            if (user == null) {
                // Não conseguimos reconstituir o Reader, retornar null
                System.err.println("Cannot reconstruct Reader from Redis hash - missing username or password");
                return null;
            }

            // Criar ReaderDetails usando construtor default
            ReaderDetails readerDetails = new ReaderDetails();

            // Primary Key
            if (hash.containsKey("pk")) {
                readerDetails.pk = Long.parseLong(hash.get("pk").toString());
            }

            // Version
            if (hash.containsKey("version")) {
                readerDetails.setVersion(Long.parseLong(hash.get("version").toString()));
            }

            // Reader
            readerDetails.setReader(user);

            // ReaderNumber
            if (hash.containsKey("readerNumber")) {
                readerDetails.setReaderNumber(new ReaderNumber(hash.get("readerNumber").toString()));
            }

            // PhoneNumber
            if (hash.containsKey("phoneNumber")) {
                readerDetails.setPhoneNumber(new PhoneNumber(hash.get("phoneNumber").toString()));
            }

            // BirthDate
            if (hash.containsKey("birthDate")) {
                readerDetails.setBirthDate(new BirthDate(hash.get("birthDate").toString()));
            }

            // Consents
            if (hash.containsKey("gdprConsent")) {
                readerDetails.setGdprConsent(Boolean.parseBoolean(hash.get("gdprConsent").toString()));
            }

            if (hash.containsKey("marketingConsent")) {
                readerDetails.setMarketingConsent(Boolean.parseBoolean(hash.get("marketingConsent").toString()));
            }

            if (hash.containsKey("thirdPartySharingConsent")) {
                readerDetails.setThirdPartySharingConsent(Boolean.parseBoolean(hash.get("thirdPartySharingConsent").toString()));
            }

            // Photo
            if (hash.containsKey("photo")) {
                String photoFile = hash.get("photo").toString();
                readerDetails.setPhoto(photoFile);
            }

            return readerDetails;

        } catch (Exception e) {
            System.err.println("Error converting Redis hash to ReaderDetails: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}