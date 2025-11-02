package pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Mapper;

import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper para converter Genre para/de formato Redis Hash
 *
 * Genre é uma entidade simples com apenas 2 campos:
 * - pk (Long)
 * - genre (String)
 */
@Component
public class GenreRedisMapper {

    /**
     * Converte Genre para Map (formato Redis Hash)
     *
     * @param genre Genre do domínio
     * @return Map com os campos do Genre
     */
    public Map<String, String> toRedisHash(Genre genre) {
        if (genre == null) {
            return null;
        }

        Map<String, String> hash = new HashMap<>();

        // PK (ID do Genre)
        if (genre.getPk() != null) {
            hash.put("pk", genre.getPk().toString());
        }

        // Genre (nome do género)
        if (genre.getGenre() != null) {
            hash.put("genre", genre.getGenre());
        }

        return hash;
    }

    /**
     * Converte Map (Redis Hash) para Genre
     *
     * @param hash Map do Redis
     * @return Genre do domínio
     */
    public Genre fromRedisHash(Map<Object, Object> hash) {
        if (hash == null || hash.isEmpty()) {
            return null;
        }

        try {
            Long pk = null;
            String genreName = null;

            // Extrair PK
            if (hash.containsKey("pk")) {
                pk = Long.parseLong(hash.get("pk").toString());
            }

            // Extrair Genre
            if (hash.containsKey("genre")) {
                genreName = hash.get("genre").toString();
            }

            // Validar que temos os dados mínimos
            if (genreName == null) {
                return null;
            }

            // Criar Genre usando constructor completo
            return new Genre(pk, genreName);

        } catch (Exception e) {
            System.err.println("Error converting Redis hash to Genre: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}