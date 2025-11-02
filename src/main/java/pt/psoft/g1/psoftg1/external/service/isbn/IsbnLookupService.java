package pt.psoft.g1.psoftg1.external.service.isbn;

import java.util.List;

/**
 * Serviço para buscar ISBN de livros usando APIs externas
 */
public interface IsbnLookupService {

    /**
     * Busca ISBN usando TODOS os providers disponíveis (com fallback)
     * Tenta em ordem de prioridade: Google Books → Open Library → ISBNdb
     */
    List<IsbnSearchResult> searchIsbnByTitle(String title);

    /**
     * Busca ISBN usando provider específico
     */
    List<IsbnSearchResult> searchIsbnByTitleWithProvider(String title, String providerName);

    /**
     * Lista todos os providers disponíveis
     */
    List<String> getAvailableProviders();
}