package pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.Mapper.GenreRedisMapper;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsDTO;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsPerMonthDTO;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Implementação Redis pura do GenreRepository
 * Esta classe apenas interage com Redis, não com SQL
 *
 * Estratégia de keys:
 * - genre:{pk} → Hash com dados do Genre
 * - genre:name:{genreName} → String com pk do Genre (índice para busca por nome)
 * - genre:all → Set com todos os pks de Genres
 */
@Repository("redisGenreRepository")
@RequiredArgsConstructor
public class RedisGenreRepositoryImpl implements GenreRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final GenreRedisMapper mapper;

    private static final String KEY_PREFIX = "genre:";
    private static final String KEY_BY_NAME = "genre:name:";
    private static final String KEY_ALL_GENRES = "genre:all";

    // TTL mais longo para Genre (24 horas) porque muda raramente
    private static final Duration TTL = Duration.ofHours(24);

    /**
     * Busca todos os Genres
     *
     * IMPORTANTE: Para Genre, faz sentido cachear findAll() porque:
     * - São poucos registos (~10-50 géneros)
     * - Mudam raramente
     * - São consultados frequentemente
     */
    @Override
    public Iterable<Genre> findAll() {
        // Buscar set com todos os pks
        Set<Object> genrePks = redisTemplate.opsForSet().members(KEY_ALL_GENRES);

        if (genrePks == null || genrePks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Genre> genres = new ArrayList<>();

        // Buscar cada Genre pelo pk
        for (Object pk : genrePks) {
            String key = KEY_PREFIX + pk.toString();
            Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);

            if (!hash.isEmpty()) {
                Genre genre = mapper.fromRedisHash(hash);
                if (genre != null) {
                    genres.add(genre);
                }
            }
        }

        return genres;
    }

    /**
     * Busca Genre por nome (findByString)
     */
    @Override
    public Optional<Genre> findByString(String genreName) {
        if (genreName == null || genreName.isBlank()) {
            return Optional.empty();
        }

        // Buscar pk pelo índice de nome
        String nameKey = KEY_BY_NAME + genreName.toLowerCase();
        String pk = (String) redisTemplate.opsForValue().get(nameKey);

        if (pk == null) {
            return Optional.empty();
        }

        // Buscar Genre pelo pk
        String key = KEY_PREFIX + pk;
        Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);

        if (hash.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(mapper.fromRedisHash(hash));
    }

    /**
     * Salva Genre no Redis
     */
    @Override
    public Genre save(Genre genre) {
        if (genre == null) {
            throw new IllegalArgumentException("Genre cannot be null");
        }

        // Se não tem pk, não podemos salvar no cache (precisa ser gerado pelo SQL primeiro)
        if (genre.getPk() == null) {
            return genre;
        }

        String key = KEY_PREFIX + genre.getPk();
        Map<String, String> hash = mapper.toRedisHash(genre);

        // Salvar como hash
        redisTemplate.opsForHash().putAll(key, hash);
        redisTemplate.expire(key, TTL.getSeconds(), TimeUnit.SECONDS);

        // Criar índice por nome (para busca findByString)
        if (genre.getGenre() != null) {
            String nameKey = KEY_BY_NAME + genre.getGenre().toLowerCase();
            redisTemplate.opsForValue().set(nameKey, genre.getPk().toString(), TTL);
        }

        // Adicionar à lista de todos os genres
        redisTemplate.opsForSet().add(KEY_ALL_GENRES, genre.getPk().toString());
        redisTemplate.expire(KEY_ALL_GENRES, TTL.getSeconds(), TimeUnit.SECONDS);

        return genre;
    }

    /**
     * Delete Genre do Redis
     */
    @Override
    public void delete(Genre genre) {
        if (genre == null || genre.getPk() == null) {
            return;
        }

        String key = KEY_PREFIX + genre.getPk();

        // Deletar índice por nome
        if (genre.getGenre() != null) {
            String nameKey = KEY_BY_NAME + genre.getGenre().toLowerCase();
            redisTemplate.delete(nameKey);
        }

        // Remover da lista de todos os genres
        redisTemplate.opsForSet().remove(KEY_ALL_GENRES, genre.getPk().toString());

        // Deletar o hash principal
        redisTemplate.delete(key);
    }

    // ==================== Queries complexas - NÃO suportadas no Redis ====================
    // Estas operações são complexas e devem sempre ir ao SQL

    @Override
    public org.springframework.data.domain.Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable) {
        // Agregações complexas não são suportadas no Redis cache
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    @Override
    public List<GenreLendingsPerMonthDTO> getLendingsPerMonthLastYearByGenre() {
        // Queries com agregações e joins complexos sempre vão ao SQL
        return Collections.emptyList();
    }

    @Override
    public List<GenreLendingsDTO> getAverageLendingsInMonth(LocalDate month, pt.psoft.g1.psoftg1.shared.services.Page page) {
        // Queries com agregações sempre vão ao SQL
        return Collections.emptyList();
    }

    @Override
    public List<GenreLendingsPerMonthDTO> getLendingsAverageDurationPerMonth(LocalDate startDate, LocalDate endDate) {
        // Queries com agregações sempre vão ao SQL
        return Collections.emptyList();
    }
}