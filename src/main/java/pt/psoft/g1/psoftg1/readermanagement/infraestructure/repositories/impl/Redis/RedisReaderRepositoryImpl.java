package pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.Redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.Mapper.ReaderRedisMapper;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Implementação Redis pura do ReaderRepository
 * Esta classe apenas interage com Redis, não com SQL
 */
@Repository("redisReaderRepository")
@RequiredArgsConstructor
public class RedisReaderRepositoryImpl implements ReaderRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ReaderRedisMapper mapper;

    private static final String KEY_PREFIX = "reader:";
    private static final String KEY_BY_READER_NUMBER = "reader:number:";
    private static final String KEY_BY_USERNAME = "reader:username:";
    private static final String KEY_BY_USER_ID = "reader:userid:";
    private static final String KEY_BY_PHONE = "reader:phone:";
    private static final String KEY_ALL_READERS = "reader:all";
    private static final Duration TTL = Duration.ofHours(1);

    /**
     * Busca ReaderDetails por readerNumber
     */
    @Override
    public Optional<ReaderDetails> findByReaderNumber(String readerNumber) {
        if (readerNumber == null || readerNumber.isBlank()) {
            return Optional.empty();
        }

        String key = KEY_PREFIX + readerNumber;
        Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);

        if (hash.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(mapper.fromRedisHash(hash));
    }

    /**
     * Busca ReaderDetails por phoneNumber
     */
    @Override
    public List<ReaderDetails> findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return Collections.emptyList();
        }

        String indexKey = KEY_BY_PHONE + phoneNumber;
        Set<Object> readerNumbers = redisTemplate.opsForSet().members(indexKey);

        if (readerNumbers == null || readerNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReaderDetails> readers = new ArrayList<>();
        for (Object readerNumber : readerNumbers) {
            findByReaderNumber(readerNumber.toString())
                    .ifPresent(readers::add);
        }

        return readers;
    }

    /**
     * Busca ReaderDetails por username
     */
    @Override
    public Optional<ReaderDetails> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        String indexKey = KEY_BY_USERNAME + username.toLowerCase();
        String readerNumber = (String) redisTemplate.opsForValue().get(indexKey);

        if (readerNumber == null) {
            return Optional.empty();
        }

        return findByReaderNumber(readerNumber);
    }

    /**
     * Busca ReaderDetails por userId
     */
    @Override
    public Optional<ReaderDetails> findByUserId(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        String indexKey = KEY_BY_USER_ID + userId;
        String readerNumber = (String) redisTemplate.opsForValue().get(indexKey);

        if (readerNumber == null) {
            return Optional.empty();
        }

        return findByReaderNumber(readerNumber);
    }

    /**
     * Count from current year - não suportado no Redis
     * Retorna 0 (deve ser buscado do SQL)
     */
    @Override
    public int getCountFromCurrentYear() {
        return 0;
    }

    /**
     * Salva ReaderDetails no Redis
     */
    @Override
    public ReaderDetails save(ReaderDetails readerDetails) {
        if (readerDetails == null || readerDetails.getReaderNumber() == null) {
            throw new IllegalArgumentException("ReaderDetails and ReaderNumber cannot be null");
        }

        String key = KEY_PREFIX + readerDetails.getReaderNumber();
        Map<String, String> hash = mapper.toRedisHash(readerDetails);

        // 1. Salvar hash principal
        redisTemplate.opsForHash().putAll(key, hash);
        redisTemplate.expire(key, TTL.getSeconds(), TimeUnit.SECONDS);

        // 2. Criar índice por readerNumber (redundante mas útil para consistência)
        String readerNumberKey = KEY_BY_READER_NUMBER + readerDetails.getReaderNumber();
        redisTemplate.opsForValue().set(readerNumberKey, readerDetails.getReaderNumber(), TTL);

        // 3. Criar índice por username
        if (readerDetails.getReader() != null && readerDetails.getReader().getUsername() != null) {
            String usernameKey = KEY_BY_USERNAME + readerDetails.getReader().getUsername().toLowerCase();
            redisTemplate.opsForValue().set(usernameKey, readerDetails.getReaderNumber(), TTL);
        }

        // 4. Criar índice por userId
        if (readerDetails.getReader() != null && readerDetails.getReader().getId() != null) {
            String userIdKey = KEY_BY_USER_ID + readerDetails.getReader().getId();
            redisTemplate.opsForValue().set(userIdKey, readerDetails.getReaderNumber(), TTL);
        }

        // 5. Criar índice por phoneNumber (Set porque pode haver múltiplos readers com mesmo telefone)
        if (readerDetails.getPhoneNumber() != null) {
            String phoneKey = KEY_BY_PHONE + readerDetails.getPhoneNumber();
            redisTemplate.opsForSet().add(phoneKey, readerDetails.getReaderNumber());
            redisTemplate.expire(phoneKey, TTL.getSeconds(), TimeUnit.SECONDS);
        }

        // 6. Adicionar à lista de todos os readers
        redisTemplate.opsForSet().add(KEY_ALL_READERS, readerDetails.getReaderNumber());
        redisTemplate.expire(KEY_ALL_READERS, TTL.getSeconds(), TimeUnit.SECONDS);

        return readerDetails;
    }

    /**
     * Retorna todos os ReaderDetails
     */
    @Override
    public Iterable<ReaderDetails> findAll() {
        Set<Object> readerNumbers = redisTemplate.opsForSet().members(KEY_ALL_READERS);

        if (readerNumbers == null || readerNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReaderDetails> readers = new ArrayList<>();
        for (Object readerNumber : readerNumbers) {
            findByReaderNumber(readerNumber.toString())
                    .ifPresent(readers::add);
        }

        return readers;
    }

    /**
     * Top readers - não suportado diretamente no Redis
     * Retorna página vazia (deve ser buscado do SQL)
     */
    @Override
    public Page<ReaderDetails> findTopReaders(Pageable pageable) {
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    /**
     * Top by genre - não suportado diretamente no Redis
     * Retorna página vazia (deve ser buscado do SQL)
     */
    @Override
    public Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable, String genre, LocalDate startDate, LocalDate endDate) {
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    /**
     * Delete ReaderDetails do Redis
     */
    @Override
    public void delete(ReaderDetails readerDetails) {
        if (readerDetails == null || readerDetails.getReaderNumber() == null) {
            return;
        }

        String key = KEY_PREFIX + readerDetails.getReaderNumber();

        // 1. Deletar índice por readerNumber
        String readerNumberKey = KEY_BY_READER_NUMBER + readerDetails.getReaderNumber();
        redisTemplate.delete(readerNumberKey);

        // 2. Deletar índice por username
        if (readerDetails.getReader() != null && readerDetails.getReader().getUsername() != null) {
            String usernameKey = KEY_BY_USERNAME + readerDetails.getReader().getUsername().toLowerCase();
            redisTemplate.delete(usernameKey);
        }

        // 3. Deletar índice por userId
        if (readerDetails.getReader() != null && readerDetails.getReader().getId() != null) {
            String userIdKey = KEY_BY_USER_ID + readerDetails.getReader().getId();
            redisTemplate.delete(userIdKey);
        }

        // 4. Deletar índice por phoneNumber
        if (readerDetails.getPhoneNumber() != null) {
            String phoneKey = KEY_BY_PHONE + readerDetails.getPhoneNumber();
            redisTemplate.opsForSet().remove(phoneKey, readerDetails.getReaderNumber());
        }

        // 5. Remover da lista de todos os readers
        redisTemplate.opsForSet().remove(KEY_ALL_READERS, readerDetails.getReaderNumber());

        // 6. Deletar o hash principal
        redisTemplate.delete(key);
    }

    /**
     * Search readers - não suportado diretamente no Redis
     * Retorna lista vazia (deve ser buscado do SQL)
     */
    @Override
    public List<ReaderDetails> searchReaderDetails(pt.psoft.g1.psoftg1.shared.services.Page page, SearchReadersQuery query) {
        return Collections.emptyList();
    }
}