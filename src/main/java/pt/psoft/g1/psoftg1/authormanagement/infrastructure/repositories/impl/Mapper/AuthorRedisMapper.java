package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.Mapper;

import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.Bio;
import pt.psoft.g1.psoftg1.shared.model.Name;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper para converter Author para/de formato Redis Hash
 */
@Component
public class AuthorRedisMapper {

    /**
     * Converte Author para Map (formato Redis Hash)
     */
    public Map<String, String> toRedisHash(Author author) {
        if (author == null) {
            return null;
        }

        Map<String, String> hash = new HashMap<>();

        if (author.getAuthorNumber() != null) {
            hash.put("authorNumber", author.getAuthorNumber().toString());
        }

        hash.put("version", String.valueOf(author.getVersion()));

        if (author.getName() != null) {
            hash.put("name", author.getName().toString());
        }

        if (author.getBio() != null) {
            hash.put("bio", author.getBio().toString());
        }

        // Photo - guardar o photoFile (path da imagem)
        if (author.getPhoto() != null && author.getPhoto().getPhotoFile() != null) {
            hash.put("photo", author.getPhoto().getPhotoFile());
        }

        return hash;
    }

    /**
     * Converte Map (Redis Hash) para Author
     */
    public Author fromRedisHash(Map<Object, Object> hash) {
        if (hash == null || hash.isEmpty()) {
            return null;
        }

        try {
            Author author = new Author();

            // AuthorNumber
            if (hash.containsKey("authorNumber")) {
                author.setAuthorNumber(Long.parseLong(hash.get("authorNumber").toString()));
            }

            // Version
            if (hash.containsKey("version")) {
                author.setVersion(Long.parseLong(hash.get("version").toString()));
            }

            // Name
            if (hash.containsKey("name")) {
                author.setName(new Name(hash.get("name").toString()));
            }

            // Bio
            if (hash.containsKey("bio")) {
                author.setBio(new Bio(hash.get("bio").toString()));
            }

            // Photo - setPhoto espera String (photoURI), não objeto Photo
            if (hash.containsKey("photo")) {
                String photoFile = hash.get("photo").toString();
                author.setPhoto(photoFile);  // ← setPhoto(String)
            }

            return author;

        } catch (Exception e) {
            System.err.println("Error converting Redis hash to Author: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}