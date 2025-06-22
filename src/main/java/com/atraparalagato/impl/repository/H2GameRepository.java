package com.atraparalagato.impl.repository;

import com.atraparalagato.base.repository.DataRepository;
import com.atraparalagato.impl.model.HexGameState;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


/**
 * Implementación esqueleto de DataRepository usando base de datos H2.
 * 
 * Los estudiantes deben completar los métodos marcados con todo.
 * 
 * Conceptos a implementar:
 * - Conexión a base de datos H2
 * - Operaciones CRUD con SQL
 * - Manejo de transacciones
 * - Mapeo objeto-relacional
 * - Consultas personalizadas
 * - Manejo de errores de BD
 */
public class H2GameRepository extends DataRepository<HexGameState, String> {
    
    // Los estudiantes deben definir la configuración de la base de datos
    // Ejemplos: DataSource, JdbcTemplate, EntityManager, etc.

    private final JdbcTemplate jdbcTemplate;
    
        //public H2GameRepository() {
        // Inicializar conexión a H2 y crear tablas si no existen
        // Pista: Usar spring.datasource.url configurado en application.properties
    
    public H2GameRepository() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:hexgame;DB_CLOSE_DELAY=-1");
        ds.setUsername("sa");
        ds.setPassword("");
        this.jdbcTemplate = new JdbcTemplate(ds);

        // Crear tabla si no existe
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS hex_game_state (" +
            "id VARCHAR(255) PRIMARY KEY," +
            "state CLOB" +
            ")"
);
    }
    
    @Override
    public HexGameState save(HexGameState entity) {
            // 1. Validar entidad antes de guardar
        if (entity == null || entity.getGameId() == null) {
            throw new IllegalArgumentException("Entidad o ID no puede ser null");
        }

        beforeSave(entity);

        try {
            // 2. Serializar el estado del juego (puedes usar JSON, XML, etc.)
            String serializedState = serializeGameState(entity);

            // 3. Usar INSERT o UPDATE según si existe
            int updated = jdbcTemplate.update(
                "UPDATE hex_game_state SET state = ? WHERE id = ?",
                serializedState, entity.getGameId()
            );
            if (updated == 0) {
                jdbcTemplate.update(
                    "INSERT INTO hex_game_state (id, state) VALUES (?, ?)",
                    entity.getGameId(), serializedState
                );
            }
        } catch (Exception e) {
            // 4. Manejar errores de BD
            throw new RuntimeException("Error al guardar el estado del juego en la base de datos", e);
        }

        afterSave(entity);
        return entity;
    }
    
    @Override
    public Optional<HexGameState> findById(String id) {
        // Buscar juego por ID en la base de datos
        // 1. Ejecutar consulta SQL SELECT
        // 2. Mapear resultado a HexGameState
        // 3. Deserializar estado del juego
        // 4. Retornar Optional.empty() si no existe
        try {
        String sql = "SELECT state, board_size FROM hex_game_state WHERE id = ?";
        Map<String, Object> row = jdbcTemplate.queryForMap(sql, id);

        if (row == null || !row.containsKey("state") || !row.containsKey("board_size")) {
            return Optional.empty();
        }

        String serializedState = (String) row.get("state");
        int boardSize = (int) row.get("board_size");
        HexGameState gameState = deserializeGameState(serializedState, id, boardSize);
        return Optional.of(gameState);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar el estado del juego en la base de datos", e);
        }
}
    
    @Override
    public List<HexGameState> findAll() {
        // Obtener todos los juegos de la base de datos
        // Considerar paginación para grandes volúmenes de datos
        String sql = "SELECT state, id, board_size FROM hex_game_state";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        List<HexGameState> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String serializedState = (String) row.get("state");
            String gameId = (String) row.get("id");
            int boardSize = (int) row.get("board_size");
            HexGameState gameState = deserializeGameState(serializedState, gameId, boardSize);
            result.add(gameState);
        }
        return result;
}
    
    @Override
    public List<HexGameState> findWhere(Predicate<HexGameState> condition) {
        // Implementar búsqueda con condiciones
        // Opciones:
        // Opción 1: Cargar todos y filtrar en memoria
        List<HexGameState> allStates = findAll();
        List<HexGameState> result = new ArrayList<>();
        for (HexGameState state : allStates) {
            if (condition.test(state)) {
                result.add(state);
            }
        }
        return result;
    }
    
    @Override
    public <R> List<R> findAndTransform(Predicate<HexGameState> condition, 
                                       Function<HexGameState, R> transformer) {
        // Buscar y transformar en una operación
        // Puede optimizarse para hacer la transformación en SQL
        // Buscar y transformar en una sola operación, filtrando en memoria
        List<HexGameState> allStates = findAll();
        List<R> result = new ArrayList<>();
        for (HexGameState state : allStates) {
            if (condition.test(state)) {
                result.add(transformer.apply(state));
            }
        }
        return result;
}
    
    @Override
    public long countWhere(Predicate<HexGameState> condition) {
        // Contar registros que cumplen condición
        // Preferiblemente usar COUNT(*) en SQL para eficiencia
        String whereClause = predicateToSql(condition); // Debes implementar este método para casos específicos
        String sql = "SELECT COUNT(*) FROM hex_game_state";
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
}
    
    @Override
    public boolean deleteById(String id) {
        // Eliminar juego por ID
        // Retornar true si se eliminó, false si no existía
        String sql = "DELETE FROM hex_game_state WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        return rows > 0;
}
    
    @Override
    public long deleteWhere(Predicate<HexGameState> condition) {
        // Eliminar múltiples registros según condición
        // Retornar número de registros eliminados// Obtener todos los estados y filtrar los que cumplen la condición
        List<HexGameState> allStates = findAll();
        List<String> idsToDelete = new ArrayList<>();
        for (HexGameState state : allStates) {
            if (condition.test(state)) {
                idsToDelete.add(state.getGameId());
            }
        }
        long deletedCount = 0;
        for (String id : idsToDelete) {
            String sql = "DELETE FROM hex_game_state WHERE id = ?";
            int rows = jdbcTemplate.update(sql, id);
            if (rows > 0) {
                deletedCount += rows;
            }
        }
        return deletedCount;
    }
    
    @Override
    public boolean existsById(String id) {
        // Verificar si existe un juego con el ID dado
        // Usar SELECT COUNT(*) para eficiencia
        String sql = "SELECT COUNT(*) FROM hex_game_state WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
}
    
    @Override
    public <R> R executeInTransaction(Function<DataRepository<HexGameState, String>, R> operation) {
        // Ejecutar operación en transacción
        // 1. Iniciar transacción
        javax.sql.DataSource ds = jdbcTemplate.getDataSource();
        java.sql.Connection conn = null;
        boolean previousAutoCommit = true;
        try {
            conn = ds.getConnection();
            previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 2. Ejecutar operación
            R result = operation.apply(this);

            // 3. Commit si exitoso
            conn.commit();
            return result;
        } catch (Exception e) {
            // 3. Rollback si error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ex) {
                    // Puedes registrar el error si lo deseas
                }
            }
            // 4. Manejar excepciones apropiadamente
            throw new RuntimeException("Error en transacción", e);
        } finally {
            // Restaurar auto-commit y cerrar conexión
            if (conn != null) {
                try {
                    conn.setAutoCommit(previousAutoCommit);
                    conn.close();
                } catch (Exception ex) {
                    // Puedes registrar el error si lo deseas
                }
            }
        }
    }
    
    @Override
    public List<HexGameState> findWithPagination(int page, int size) {
        // Implementar paginación con LIMIT y OFFSET
        // Validar parámetros de entrada
        if (page < 0 || size <= 0) {
        return Collections.emptyList();
        }

        // Consulta SQL con LIMIT y OFFSET
        String sql = "SELECT state, id, board_size FROM hex_game_state LIMIT ? OFFSET ?";
        int offset = page * size;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, size, offset);
        List<HexGameState> result = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            String serializedState = (String) row.get("state");
            String gameId = (String) row.get("id");
            int boardSize = (int) row.get("board_size");
            HexGameState gameState = deserializeGameState(serializedState, gameId, boardSize);
            result.add(gameState);
        }

        return result;}
    
    @Override
    public List<HexGameState> findAllSorted(Function<HexGameState, ? extends Comparable<?>> sortKeyExtractor, 
                                           boolean ascending) {
        // Implementar ordenamiento
        // Convertir sortKeyExtractor a ORDER BY SQL
        @SuppressWarnings("unchecked")
        Comparator<HexGameState> comparator = (Comparator<HexGameState>) Comparator.comparing(
        (Function<HexGameState, Comparable<Object>>) sortKeyExtractor
        );

        if (!ascending) {
            comparator = comparator.reversed();
        }

        // Suponiendo que tienes un método findAll() que retorna todos los HexGameState
        return findAll().stream()
                .sorted(comparator)
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public <R> List<R> executeCustomQuery(String query, Function<Object, R> resultMapper) {
        List<R> results = new ArrayList<>();
        try {
            // 1. Validar consulta SQL (simple: no null o vacía)
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("La consulta SQL no puede ser nula o vacía");
            }

            // 2. Ejecutar consulta
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);

            // 3. Mapear resultados usando resultMapper
            for (Map<String, Object> row : rows) {
                results.add(resultMapper.apply(row));
            }
        } catch (Exception e) {
            // 4. Manejar errores SQL
            throw new RuntimeException("Error al ejecutar consulta personalizada", e);
        }
        return results;
    }
    
    @Override
    protected void initialize() {
        // Inicializar base de datos
        // 1. Crear tablas si no existen
        createSchema();

        // 2. Configurar índices (opcional, ejemplo para columna id)
        try {
            String indexSql = "CREATE INDEX IF NOT EXISTS idx_hex_game_state_id ON hex_game_state(id)";
            jdbcTemplate.execute(indexSql);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear índice en la base de datos", e);
        }
    }
    
    @Override
    protected void cleanup() {
        //1. Cerrar conexiones//
        try {
            if (jdbcTemplate != null && jdbcTemplate.getDataSource() != null) {
                jdbcTemplate.getDataSource().getConnection().close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cerrar la conexión a la base de datos", e);
        }
    }
    
    // Métodos auxiliares que los estudiantes pueden implementar
    
    /**
     * Crear el esquema de la base de datos.
     * Definir tablas, columnas, tipos de datos, restricciones.
     */
    private void createSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS hex_game_state (" +
                 "id VARCHAR(255) PRIMARY KEY, " +
                 "board_size INT NOT NULL, " +
                 "state CLOB NOT NULL" +
                 ")";
        // Ejecutar la sentencia SQL usando JDBC o JdbcTemplate
        jdbcTemplate.execute(sql);
    }
    
    /**
     * Serializar HexGameState a formato de BD.
     * Puede usar JSON, XML, o campos separados.
     */
    private String serializeGameState(HexGameState gameState) {
        Object serializable = gameState.getSerializableState();
        return (serializable != null) ? serializable.toString() : "";
    }
    
    /**
     *Deserializar desde formato de BD a HexGameState.
     * Debe ser compatible con serializeGameState.
     */
    private HexGameState deserializeGameState(String serializedData, String gameId, int boardSize) {
        HexGameState gameState = new HexGameState(gameId, boardSize);
        gameState.restoreFromSerializable(serializedData);
        return gameState;
        
    }
    
    /**
     * Convertir Predicate a cláusula WHERE SQL.
     * Implementación avanzada opcional.
     */
    private String predicateToSql(Predicate<HexGameState> predicate) {
        throw new UnsupportedOperationException("Método auxiliar avanzado para implementar");
    }
} 