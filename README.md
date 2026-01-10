# ML Training Service

Aplikacja Spring Boot do trenowania modeli ML z PostgreSQL zoptymalizowanym pod miliony rekordów.

## Architektura

- **Spring Boot 4.0.1** z Java 21
- **JDBC + Spring Data JDBC** (nie JPA) - maksymalna wydajność
- **PostgreSQL 16** - zoptymalizowany pod bulk operations
- **HikariCP** - connection pooling

## Dlaczego JDBC zamiast JPA?

Dla 10 milionów rekordów i przetwarzania strumieniowego:
- **JDBC**: ~2-3x szybsze, brak overhead ORM, bezpośrednia kontrola nad SQL
- **JPA/Hibernate**: Wolniejsze przy bulk operations, problemy z pamięcią przy dużych zbiorach

## Uruchomienie

```bash
# Build i start
docker-compose up --build

# W tle
docker-compose up -d

# Logi
docker-compose logs -f

# Stop
docker-compose down

# Stop + usunięcie danych
docker-compose down -v
```

## Endpointy

- **Spring App**: http://localhost:8090
- **PostgreSQL**: localhost:22432
- **Health**: http://localhost:8090/actuator/health
- **Metrics**: http://localhost:8090/actuator/metrics

## Połączenie z PostgreSQL (zewnętrzne)

```bash
psql -h localhost -p 22432 -U mluser -d mltraining
# Password: mlpass123
```

## Kluczowe optymalizacje

### PostgreSQL
- `shared_buffers = 2GB` - cache
- `work_mem = 64MB` - sortowanie/hash
- `random_page_cost = 1.1` - SSD
- `synchronous_commit = off` - szybsze zapisy
- `reWriteBatchedInserts = true` - batch inserts

### Spring JDBC
- `fetchSize: 10000` - streaming
- `auto-commit: false` - kontrola transakcji
- `reWriteBatchedInserts: true` - przepisuje INSERT na bulk
- `queryForStream()` - streaming dużych zbiorów

## Przykład użycia - Streaming 10M rekordów

```java
@Transactional(readOnly = true)
public void processLargeDataset() {
    String sql = "SELECT * FROM training_data";

    try (Stream<Record> stream = jdbcTemplate.queryForStream(
            sql, (rs, rowNum) -> mapRow(rs))) {

        stream.parallel()
              .forEach(this::processRecord);
    }
}
```

## Przykład użycia - Batch Insert

```java
public void batchInsert(List<Record> records) {
    String sql = "INSERT INTO training_data VALUES (?, ?, ?)";

    jdbcTemplate.batchUpdate(sql, records, 10000,
        (ps, record) -> {
            ps.setDouble(1, record.feature1());
            ps.setDouble(2, record.feature2());
            ps.setInt(3, record.label());
        });
}
```

## Performance Tips

1. **Zawsze używaj batch operations**: `batchUpdate()` zamiast wielu `update()`
2. **Streaming**: `queryForStream()` + `@Transactional` dla dużych zbiorów
3. **Indeksy**: Stwórz indeksy na kolumnach używanych w WHERE/JOIN
4. **COPY**: Dla milionów insertów rozważ PostgreSQL COPY command
5. **Partycjonowanie**: Dla bardzo dużych tabel użyj table partitioning

## Monitoring

```bash
# Metryki HikariCP
curl http://localhost:8090/actuator/metrics/hikaricp.connections.active

# Health check
curl http://localhost:8090/actuator/health
```

## Konfiguracja produkcyjna

W `docker-compose.yml` zwiększ:
- `JAVA_OPTS: "-Xms4g -Xmx8g"`
- PostgreSQL `shared_buffers = 4GB` (25% RAM serwera)
- HikariCP `maximum-pool-size: 50`
