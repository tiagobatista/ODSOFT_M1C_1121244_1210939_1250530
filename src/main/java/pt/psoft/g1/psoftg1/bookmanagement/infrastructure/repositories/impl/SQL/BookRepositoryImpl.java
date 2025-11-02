package pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.SQL;



import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl.SQL.AuthorRepositoryImpl;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.sql.AuthorEntity;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.Mapper.BookEntityMapper;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.SpringDataBookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.sql.BookEntity;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl.SQL.GenreRepositoryImpl;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.sql.GenreEntity;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Profile("sql-redis")

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository
{
    private final SpringDataBookRepository bookRepo;
    private final GenreRepositoryImpl genreRepo;
    private final AuthorRepositoryImpl authorRepo;
    private final BookEntityMapper bookEntityMapper;
    private final EntityManager em;

    @Override
    public List<Book> findByGenre(@Param("genre") String genre)
    {
        List<Book> books = new ArrayList<>();
        for (BookEntity b: bookRepo.findByGenre(genre))
        {
            books.add(bookEntityMapper.toModel(b));
        }

        return books;
    }

    @Override
    public List<Book> findByTitle(@Param("title") String title)
    {
        List<Book> books = new ArrayList<>();
        for (BookEntity b: bookRepo.findByTitle(title))
        {
            books.add(bookEntityMapper.toModel(b));
        }

        return books;
    }

    @Override
    public List<Book> findByAuthorName(@Param("authorName") String authorName)
    {
        List<Book> books = new ArrayList<>();
        for (BookEntity b: bookRepo.findByAuthorName(authorName))
        {
            books.add(bookEntityMapper.toModel(b));
        }

        return books;
    }

    @Override
    @Cacheable(value = "isbn", key = "#isbn")
    public Optional<Book> findByIsbn(@Param("isbn") String isbn)
    {
        Optional<BookEntity> entityOpt = bookRepo.findByIsbn(isbn);
        if(entityOpt.isPresent())
        {
            return Optional.of(bookEntityMapper.toModel(entityOpt.get()));
        }
        else
        {
            return Optional.empty();
        }
    }

    @Override
    public Page<BookCountDTO> findTop5BooksLent(@Param("oneYearAgo") LocalDate oneYearAgo, Pageable pageable)
    {
        //TODO: Corrigir este
        return bookRepo.findTop5BooksLent(oneYearAgo, pageable);
    }

    @Override
    public List<Book> findBooksByAuthorNumber(Long authorNumber)
    {
        List<Book> books = new ArrayList<>();
        for (BookEntity b: bookRepo.findBooksByAuthorNumber(authorNumber))
        {
            books.add(bookEntityMapper.toModel(b));
        }

        return books;
    }

    @Override
    public List<Book> searchBooks(pt.psoft.g1.psoftg1.shared.services.Page page, SearchBooksQuery query)
    {
        String title = query.getTitle();
        String genre = query.getGenre();
        String authorName = query.getAuthorName();

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<BookEntity> cq = cb.createQuery(BookEntity.class);
        final Root<BookEntity> root = cq.from(BookEntity.class);
        final Join<BookEntity, GenreEntity> genreJoin = root.join("genre");
        final Join<BookEntity, AuthorEntity> authorJoin = root.join("authors");
        cq.select(root);

        final List<Predicate> where = new ArrayList<>();

        if (StringUtils.hasText(title))
            where.add(cb.like(root.get("title").get("title"), title + "%"));

        if (StringUtils.hasText(genre))
            where.add(cb.like(genreJoin.get("genre"), genre + "%"));

        if (StringUtils.hasText(authorName))
            where.add(cb.like(authorJoin.get("name").get("name"), authorName + "%"));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("title"))); // Order by title, alphabetically

        final TypedQuery<BookEntity> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        List <Book> books = new ArrayList<>();

        for (BookEntity bookEntity : q.getResultList()) {
            books.add(bookEntityMapper.toModel(bookEntity));
        }

        return books;
    }

    @Override
    @Transactional
    @CacheEvict(value = "isbn", key = "#book.isbn")
    public Book save(Book book) {

        BookEntity entity = bookEntityMapper.toEntity(book);


        // Genre handling

        Genre genreModel = genreRepo.findByString(book.getGenre().getGenre())
                .orElseThrow(() -> new RuntimeException("Genre not found"));


        GenreEntity genreEntity = em.getReference(GenreEntity.class, genreModel.getPk());
        entity.setGenre(genreEntity);


        // Authors handling
        List<AuthorEntity> authors = new ArrayList<>();

        int authorIndex = 0;
        for (var author : book.getAuthors()) {
            authorIndex++;



            List<Author> foundAuthors = authorRepo.searchByNameName(author.getName().getName());


            if (foundAuthors.isEmpty()) {

                throw new RuntimeException("Author not found: " + author.getName().getName());
            }

            Author auth = foundAuthors.get(0);


            if (auth.getAuthorNumber() == null) {

                throw new RuntimeException("Author has null authorNumber: " + auth.getName().getName());
            }


            AuthorEntity authorEntity = em.getReference(AuthorEntity.class, auth.getAuthorNumber());


            authors.add(authorEntity);

        }


        entity.setAuthors(authors);


        BookEntity savedEntity = bookRepo.save(entity);



        Book result = bookEntityMapper.toModel(savedEntity);


        return result;
    }

    @Override
    public void delete(Book book)
    {
        // TODO
    }
}


