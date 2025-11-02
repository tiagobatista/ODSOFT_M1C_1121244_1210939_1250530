package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.Mapper;

import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.model.LendingNumber;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapper para converter Lending para/de formato Redis Hash
 *
 * NOTA IMPORTANTE: Este mapper guarda apenas os IDs de Book e ReaderDetails,
 * não os objetos completos. Os objetos completos devem ser reconstruídos
 * buscando do cache ou SQL quando necessário.
 */
@Component
public class LendingRedisMapper {

    /**
     * Converte Lending para Map (formato Redis Hash)
     * Guarda apenas IDs de relacionamentos, não objetos completos
     */
    public Map<String, String> toRedisHash(Lending lending) {
        if (lending == null) {
            return null;
        }

        Map<String, String> hash = new HashMap<>();

        // ID e Version
        if (lending.getId() != null) {
            hash.put("id", lending.getId().toString());
        }

        if (lending.getVersionControl() != null) {
            hash.put("version", lending.getVersionControl().toString());
        }

        // LendingNumber
        if (lending.getLendingNumber() != null) {
            hash.put("lendingNumber", lending.getLendingNumber());
        }

        // Datas
        if (lending.getStartDate() != null) {
            hash.put("startDate", lending.getStartDate().toString());
        }

        if (lending.getLimitDate() != null) {
            hash.put("limitDate", lending.getLimitDate().toString());
        }

        if (lending.getReturnedDate() != null) {
            hash.put("returnedDate", lending.getReturnedDate().toString());
        }

        // Fine
        hash.put("fineValuePerDayInCents", String.valueOf(lending.getDailyFineAmount()));

        // Feedback
        if (lending.getCommentary() != null) {
            hash.put("commentary", lending.getCommentary());
        }

        // Relacionamentos - APENAS IDs e informações essenciais
        if (lending.getBorrowedBook() != null) {
            if (lending.getBorrowedBook().getPk() != null) {
                hash.put("book_pk", lending.getBorrowedBook().getPk().toString());
            }
            if (lending.getBorrowedBook().getIsbn() != null) {
                hash.put("book_isbn", lending.getBorrowedBook().getIsbn().toString());
            }
            if (lending.getBorrowedBook().getTitle() != null) {
                hash.put("book_title", lending.getBorrowedBook().getTitle().toString());
            }
        }

        if (lending.getBorrower() != null) {
            if (lending.getBorrower().getPk() != null) {
                hash.put("reader_pk", lending.getBorrower().getPk().toString());
            }
            if (lending.getBorrower().getReaderNumber() != null) {
                hash.put("reader_number", lending.getBorrower().getReaderNumber());
            }
        }

        return hash;
    }

    /**
     * Converte Map (Redis Hash) para Lending
     *
     * IMPORTANTE: Este método retorna um Lending com dados PARCIAIS.
     * Os objetos Book e ReaderDetails precisam ser completados posteriormente
     * buscando dos seus respectivos caches/repositories.
     */
    public Lending fromRedisHash(Map<Object, Object> hash) {
        if (hash == null || hash.isEmpty()) {
            return null;
        }

        try {
            // NOTA: Não podemos reconstituir o Lending completamente aqui
            // porque precisamos dos objetos Book e ReaderDetails completos.
            // Este método serve apenas para ler os dados básicos do cache.

            // Para reconstituição completa, use o método fromRedisHashWithDependencies
            // no RedisLendingRepositoryImpl que busca Book e Reader dos seus caches.

            return null; // Forçar uso do método correto no Repository

        } catch (Exception e) {
            System.err.println("Error converting Redis hash to Lending: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extrai dados básicos do hash para uso em queries
     */
    public String extractLendingNumber(Map<Object, Object> hash) {
        return hash.containsKey("lendingNumber") ? hash.get("lendingNumber").toString() : null;
    }

    public String extractReaderNumber(Map<Object, Object> hash) {
        return hash.containsKey("reader_number") ? hash.get("reader_number").toString() : null;
    }

    public String extractBookIsbn(Map<Object, Object> hash) {
        return hash.containsKey("book_isbn") ? hash.get("book_isbn").toString() : null;
    }

    public boolean isReturned(Map<Object, Object> hash) {
        return hash.containsKey("returnedDate");
    }

    public LocalDate extractReturnedDate(Map<Object, Object> hash) {
        if (!hash.containsKey("returnedDate")) {
            return null;
        }
        return LocalDate.parse(hash.get("returnedDate").toString());
    }
}