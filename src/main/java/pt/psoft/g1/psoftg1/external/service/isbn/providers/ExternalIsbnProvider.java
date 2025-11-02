package pt.psoft.g1.psoftg1.external.service.isbn.providers;

import pt.psoft.g1.psoftg1.external.service.isbn.IsbnSearchResult;

import java.util.List;

/**
 * Interface comum para todos os provedores de ISBN externos
 */
public interface ExternalIsbnProvider {

    /**
     * Nome do provedor (ex: "Google Books", "Open Library")
     */
    String getProviderName();

    /**
     * Busca ISBNs por título de livro
     *
     * @param title Título do livro para buscar
     * @return Lista de resultados encontrados (pode ser vazia)
     * @throws Exception Se houver erro na comunicação com a API
     */
    List<IsbnSearchResult> searchByTitle(String title) throws Exception;

    /**
     * Verifica se o provedor está disponível (configurado corretamente)
     */
    boolean isAvailable();

    /**
     * Prioridade do provedor (menor número = maior prioridade)
     * Google Books = 1, Open Library = 2, ISBNdb = 3
     */
    int getPriority();
}