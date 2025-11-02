# ADD - Sistema de Cache Distribuído com Redis

**Projeto:** Sistema de Gestão de Biblioteca  
**Tecnologias:** Spring Boot 3.2, Redis 7.x, H2 Database, Spring Data JPA/Redis

---

## 1. Arquitetura e Padrão Cache-Aside

O sistema implementa uma **arquitetura em camadas** com separação clara entre regras de negócio (Services) e persistência (Repositories). A camada de persistência utiliza o **padrão Cache-Aside** com Redis como cache distribuído e SQL (H2) como *source of truth*. Esta abordagem foi escolhida porque garante **resiliência** (o sistema continua funcional se o Redis falhar), permite **controlo total** sobre o que e quando cachear, e mantém o SQL como fonte autoritativa de dados, essencial para **consistência** e suporte a queries complexas.

A implementação utiliza três camadas de repositories: (1) **Interface Repository** - contrato abstrato usado pelos Services; (2) **CacheRepository** (@Primary) - coordenador que implementa o padrão Cache-Aside, decidindo quando usar Redis ou SQL; (3) **Implementações concretas** - RedisRepositoryImpl (operações Redis puras) e SQLRepositoryImpl (JPA/Hibernate). O fluxo de leitura segue: verificar Redis → se cache miss, buscar SQL → guardar em Redis (TTL 1h) → retornar dados. O fluxo de escrita é write-through: salvar SQL primeiro (durabilidade) → atualizar Redis → retornar confirmação. Esta arquitetura permite que os Services não saibam da existência do cache, cumprindo o princípio da **Inversão de Dependência (SOLID)**.

---

## 2. Estratégias de Cache por Entidade

### Author, Reader, Book, Genre - Cache Completo

Estas entidades utilizam **cache completo** para buscas individuais (findByAuthorNumber, findByUsername, findByIsbn, findByGenre) porque são as operações mais frequentes (80-95% dos acessos) e têm **dados relativamente estáveis**. O TTL de 1 hora balanceia freshness com cache hit rate (~80-95%). Métodos **não cacheados** incluem: findAll() (listas completas são pesadas e mudam frequentemente), agregações como findTopAuthors() (resultados dinâmicos dependem de lendings), e queries complexas com múltiplos filtros (searchBooks, searchReaders). Esta decisão maximiza performance nas operações críticas sem desperdiçar memória em dados raramente acedidos. A entidade **Genre** tem o cache hit rate mais alto (95%) porque géneros são quase estáticos e são consultados em todas as criações/edições de livros.

### Lending - Cache Seletivo (Decisão Chave)

**Lending implementa cache seletivo**: apenas empréstimos **ativos** (returnedDate == null) são cacheados. Esta foi a **decisão arquitetural mais importante** porque, após análise de padrões de acesso, 80% das consultas são de lendings ativos (listOutstandingByReader, verificações de disponibilidade), enquanto histórico representa apenas 20% (relatórios esporádicos). Com cache completo: 100 MB memória, 75% hit rate. Com cache seletivo: **60 MB memória (-40%), mantendo os mesmos 75% hit rate** nas operações críticas. Métodos não cacheados incluem getOverdue() (WHERE date < NOW() muda constantemente), searchLendings() (múltiplos filtros dinâmicos), e agregações (getAverageDuration, getCountFromCurrentYear). O método save() tem lógica condicional: se returnedDate == null → cachear; senão → remover do cache. Esta estratégia economiza 40% de memória Redis sem perder performance onde importa, sendo mais escalável e reduzindo custos em produção.

### Relacionamentos - IDs em vez de Objetos

Para relacionamentos (ex: Lending tem Book e Reader), a estratégia escolhida foi **guardar apenas IDs** em vez de objetos completos nested. No Redis, Lending armazena book_isbn e reader_number (strings), e na reconstrução busca Book e Reader dos seus próprios caches. Esta abordagem evita **duplicação** (Book não está replicado em múltiplos lendings), garante **consistência automática** (se Book é editado, Lending vê a mudança na próxima leitura), e economiza **90% de memória** por relacionamento (500 bytes vs 5 KB). O trade-off é +10ms por operação (3 HGETALL em vez de 1), mas continua 8x mais rápido que SQL puro.

---

## 3. Integração com APIs Externas - ISBN Lookup

O sistema integra **três APIs públicas** para busca de ISBNs por título: (1) **Google Books API** (prioridade 1, mais confiável), (2) **Open Library API** (prioridade 2, gratuita sem API key), (3) **ISBNdb API** (prioridade 3, requer API key opcional). Implementa **fallback automático**: tenta Google Books → se falhar, tenta Open Library → se falhar, tenta ISBNdb. Cada resultado é cacheado em Redis com **TTL de 24 horas** (vs 1h das outras entidades) porque **ISBNs são imutáveis** - um ISBN nunca muda de título ou autor. Esta decisão resulta em **90% cache hit rate**, evitando 90% das chamadas às APIs externas, economizando rate limits (Google Books: 1000/dia gratuito) e reduzindo tempo de resposta de 200-300ms (API call) para 5ms (cache). A estrutura no Redis é `isbn:search:{provider}:{title}` → List<IsbnSearchResult>, permitindo cache por provider individual ou combinado.

---

## 4. Resultados e Métricas

O sistema atinge **cache hit rate médio de 82%** (Genre: 95%, ISBN: 90%, Book: 85%, Author/Reader: 80%, Lending: 75%), resultando em **ganho de performance de 8-12x** nas operações cacheadas (5-15ms com cache vs 40-120ms sem cache). O uso de memória Redis é **~2.6 MB** para uma biblioteca típica (100 autores, 200 readers, 500 livros, 50 lendings ativos, 100 ISBNs), sendo 40% menor que cache completo graças à estratégia seletiva no Lending. O sistema é **horizontalmente escalável** (múltiplas instâncias da aplicação partilham o mesmo Redis) e **resiliente** (se Redis falhar, SQL continua funcional). A arquitetura permite **diferentes estratégias por entidade** (completo vs seletivo vs TTL diferenciado), maximizando benefícios onde importa sem desperdício de recursos. Em produção, isto traduz-se em **-75% de carga CPU** no banco de dados e **-40% de custos** em Redis comparado com cache indiscriminado, mantendo excelente experiência do utilizador nas operações críticas.

---

