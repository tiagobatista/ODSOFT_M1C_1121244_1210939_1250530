package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.Redis;



import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.Mapper.LendingRedisMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.model.LendingNumber;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Implementação Redis pura do LendingRepository
 * Esta classe apenas interage com Redis, não com SQL
 *
 * ESTRATÉGIA: Cache apenas dados essenciais de lendings ativos
 */
@Repository("redisLendingRepository")
@RequiredArgsConstructor
public class RedisLendingRepositoryImpl implements LendingRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final LendingRedisMapper mapper;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;

    private static final String KEY_PREFIX = "lending:";
    private static final String KEY_BY_LENDING_NUMBER = "lending:number:";
    private static final String KEY_BY_READER = "lending:reader:";
    private static final String KEY_OUTSTANDING = "lending:outstanding:";
    private static final Duration TTL = Duration.ofHours(1);

    /**
     * Busca Lending por lendingNumber
     * Reconstrói objetos Book e ReaderDetails dos seus respectivos caches
     */
    @Override
    public Optional<Lending> findByLendingNumber(String lendingNumber) {
        if (lendingNumber == null || lendingNumber.isBlank()) {
            return Optional.empty();
        }

        String key = KEY_PREFIX + lendingNumber;
        Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);

        if (hash.isEmpty()) {
            return Optional.empty();
        }

        return reconstructLending(hash);
    }

    /**
     * Lista lendings ativos por readerNumber
     */
    @Override
    public List<Lending> listOutstandingByReaderNumber(String readerNumber) {
        if (readerNumber == null || readerNumber.isBlank()) {
            return Collections.emptyList();
        }

        String key = KEY_OUTSTANDING + readerNumber;
        Set<Object> lendingNumbers = redisTemplate.opsForSet().members(key);

        if (lendingNumbers == null || lendingNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        List<Lending> lendings = new ArrayList<>();
        for (Object lendingNumber : lendingNumbers) {
            findByLendingNumber(lendingNumber.toString())
                    .ifPresent(lendings::add);
        }

        return lendings;
    }

    /**
     * Salva Lending no Redis
     */
    @Override
    public Lending save(Lending lending) {
        if (lending == null || lending.getLendingNumber() == null) {
            throw new IllegalArgumentException("Lending and LendingNumber cannot be null");
        }

        String key = KEY_PREFIX + lending.getLendingNumber();
        Map<String, String> hash = mapper.toRedisHash(lending);

        // Salvar hash principal
        redisTemplate.opsForHash().putAll(key, hash);
        redisTemplate.expire(key, TTL.getSeconds(), TimeUnit.SECONDS);

        // Criar índice por lending number
        String lendingNumberKey = KEY_BY_LENDING_NUMBER + lending.getLendingNumber();
        redisTemplate.opsForValue().set(lendingNumberKey, lending.getLendingNumber(), TTL);

        // Se lending está ativo (não devolvido), adicionar ao índice de outstanding
        if (lending.getReturnedDate() == null && lending.getBorrower() != null) {
            String outstandingKey = KEY_OUTSTANDING + lending.getBorrower().getReaderNumber();
            redisTemplate.opsForSet().add(outstandingKey, lending.getLendingNumber());
            redisTemplate.expire(outstandingKey, TTL.getSeconds(), TimeUnit.SECONDS);
        }

        // Criar índice por reader
        if (lending.getBorrower() != null) {
            String readerKey = KEY_BY_READER + lending.getBorrower().getReaderNumber();
            redisTemplate.opsForSet().add(readerKey, lending.getLendingNumber());
            redisTemplate.expire(readerKey, TTL.getSeconds(), TimeUnit.SECONDS);
        }

        return lending;
    }

    /**
     * Delete Lending do Redis
     */
    @Override
    public void delete(Lending lending) {
        if (lending == null || lending.getLendingNumber() == null) {
            return;
        }

        String key = KEY_PREFIX + lending.getLendingNumber();

        // Deletar índice por lending number
        String lendingNumberKey = KEY_BY_LENDING_NUMBER + lending.getLendingNumber();
        redisTemplate.delete(lendingNumberKey);

        // Remover do índice de outstanding
        if (lending.getBorrower() != null) {
            String outstandingKey = KEY_OUTSTANDING + lending.getBorrower().getReaderNumber();
            redisTemplate.opsForSet().remove(outstandingKey, lending.getLendingNumber());
        }

        // Remover do índice por reader
        if (lending.getBorrower() != null) {
            String readerKey = KEY_BY_READER + lending.getBorrower().getReaderNumber();
            redisTemplate.opsForSet().remove(readerKey, lending.getLendingNumber());
        }

        // Deletar hash principal
        redisTemplate.delete(key);
    }

    // ==================== Métodos não suportados no Redis ====================

    @Override
    public List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn) {
        return Collections.emptyList();
    }

    @Override
    public int getCountFromCurrentYear() {
        return 0;
    }

    @Override
    public Double getAverageDuration() {
        return null;
    }

    @Override
    public Double getAvgLendingDurationByIsbn(String isbn) {
        return null;
    }

    @Override
    public List<Lending> getOverdue(Page page) {
        return Collections.emptyList();
    }

    @Override
    public List<Lending> searchLendings(Page page, String readerNumber, String isbn,
                                        Boolean returned, LocalDate startDate, LocalDate endDate) {
        return Collections.emptyList();
    }

    // ==================== Métodos auxiliares ====================

    /**
     * Reconstrói um Lending completo a partir do hash Redis
     * Busca Book e ReaderDetails dos seus respectivos caches
     */
    private Optional<Lending> reconstructLending(Map<Object, Object> hash) {
        try {
            // Buscar Book do cache
            String bookIsbn = hash.containsKey("book_isbn") ? hash.get("book_isbn").toString() : null;
            if (bookIsbn == null) {
                return Optional.empty();
            }

            Book book = bookRepository.findByIsbn(bookIsbn).orElse(null);
            if (book == null) {
                return Optional.empty();
            }

            // Buscar ReaderDetails do cache
            String readerNumber = hash.containsKey("reader_number") ? hash.get("reader_number").toString() : null;
            if (readerNumber == null) {
                return Optional.empty();
            }

            ReaderDetails reader = readerRepository.findByReaderNumber(readerNumber).orElse(null);
            if (reader == null) {
                return Optional.empty();
            }

            // Reconstituir Lending
            LendingNumber lendingNumber = new LendingNumber(hash.get("lendingNumber").toString());
            LocalDate startDate = LocalDate.parse(hash.get("startDate").toString());
            LocalDate limitDate = LocalDate.parse(hash.get("limitDate").toString());
            LocalDate returnedDate = hash.containsKey("returnedDate")
                    ? LocalDate.parse(hash.get("returnedDate").toString())
                    : null;
            int fineValue = Integer.parseInt(hash.get("fineValuePerDayInCents").toString());

            Lending lending = new Lending(book, reader, lendingNumber, startDate, limitDate, returnedDate, fineValue);

            // Definir ID e version
            if (hash.containsKey("id")) {
                lending.setPk(Long.parseLong(hash.get("id").toString()));
            }

            if (hash.containsKey("version")) {
                lending.setVersion(Long.parseLong(hash.get("version").toString()));
            }

            // Definir commentary se existir
            if (hash.containsKey("commentary")) {
                lending.setCommentary(hash.get("commentary").toString());
            }

            return Optional.of(lending);

        } catch (Exception e) {
            System.err.println("Error reconstructing Lending from Redis: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
}