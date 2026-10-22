package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.RowMapper;
import com.littlehorizons.erp.model.Person;
import com.littlehorizons.erp.model.enums.DbEnum;
import com.littlehorizons.erp.model.enums.DocumentType;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.ErrorLogger;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Clase base de todos los DAO con JDBC.
 * <p>
 * Centraliza el código JDBC repetitivo (idea del patrón Template Method): abrir la conexión, crear el
 * {@link PreparedStatement}, asignar los parámetros, ejecutar, convertir las filas en objetos y
 * cerrar todo con try-with-resources. Así cada DAO hijo solo escribe su SQL y un {@link RowMapper}.
 * </p>
 * <p>
 * Importante: TODAS las consultas usan PreparedStatement con signos {@code ?}. Los valores nunca se
 * concatenan en el texto SQL, lo que evita la inyección SQL.
 * </p>
 */
public abstract class BaseJdbcDao {

    // Logger único (Singleton) para dejar constancia de los errores de base de datos
    private final ErrorLogger logger = ErrorLogger.getInstance();

    // ====================== CONSULTAS (SELECT) ======================

    /**
     * Ejecuta un SELECT y convierte cada fila en un objeto.
     *
     * @param sql    consulta con signos ? donde irán los parámetros
     * @param mapper cómo convertir una fila en un objeto
     * @param params valores que reemplazan los ? en el mismo orden
     */
    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        // try-with-resources: la conexión, el statement y el resultado se cierran SOLOS al terminar,
        // incluso si ocurre un error (no se filtran conexiones)
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);                                  // se asignan los valores a los ?
            try (ResultSet resultSet = statement.executeQuery()) {    // se ejecuta el SELECT
                List<T> rows = new ArrayList<>();
                while (resultSet.next()) {                            // next() avanza fila por fila
                    rows.add(mapper.map(resultSet));                  // cada fila se convierte en objeto
                }
                return rows;
            }
        } catch (SQLException e) {
            // Se registra el error en el log y se lanza una excepción propia que la vista entiende
            throw fail("query", sql, e);
        }
    }

    /** Ejecuta un SELECT que devuelve como máximo una fila. */
    protected <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
        // Se reutiliza queryList y se toma el primer elemento (Optional vacío si no hubo filas)
        return queryList(sql, mapper, params).stream().findFirst();
    }

    /** Ejecuta un SELECT que devuelve un solo número (COUNT, MAX...). */
    protected int queryInt(String sql, Object... params) {
        // getInt(1) lee la primera columna; si no hay filas se devuelve 0
        return queryOne(sql, resultSet -> resultSet.getInt(1), params).orElse(0);
    }

    // ====================== CAMBIOS (UPDATE / DELETE) ======================

    /** Ejecuta un INSERT, UPDATE o DELETE con su propia conexión. @return filas afectadas */
    protected int update(String sql, Object... params) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            return update(connection, sql, params);
        } catch (SQLException e) {
            throw fail("update", sql, e);
        }
    }

    /** Igual que el anterior pero usando una conexión externa (para participar en una transacción). */
    protected int update(Connection connection, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);
            // executeUpdate devuelve cuántas filas cambió la sentencia
            return statement.executeUpdate();
        }
    }

    // ====================== INSERCIONES ======================

    /** Ejecuta un INSERT y devuelve el id AUTO_INCREMENT que generó MySQL. */
    protected int insert(String sql, Object... params) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            return insert(connection, sql, params);
        } catch (SQLException e) {
            throw fail("insert", sql, e);
        }
    }

    /** Igual que el anterior pero dentro de una transacción externa. */
    protected int insert(Connection connection, String sql, Object... params) throws SQLException {
        // RETURN_GENERATED_KEYS pide a MySQL que devuelva el id que acaba de crear
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, params);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    // ====================== ASIGNACIÓN DE PARÁMETROS ======================

    /**
     * Asigna cada parámetro al ? que le corresponde usando el tipo JDBC correcto.
     * Aquí se convierten los tipos de Java (LocalDate, enum...) a los de la base de datos.
     */
    private void bind(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object value = params[i];
            int index = i + 1;                       // en JDBC los ? se numeran desde 1, no desde 0
            if (value == null) {
                statement.setNull(index, Types.NULL);                       // dato vacío -> NULL en la base
            } else if (value instanceof DbEnum dbEnum) {
                statement.setString(index, dbEnum.getDbValue());            // enum -> texto en español
            } else if (value instanceof LocalDate date) {
                statement.setDate(index, Date.valueOf(date));               // LocalDate -> DATE de SQL
            } else if (value instanceof LocalTime time) {
                statement.setTime(index, Time.valueOf(time));               // LocalTime -> TIME de SQL
            } else if (value instanceof BigDecimal decimal) {
                statement.setBigDecimal(index, decimal);                    // dinero y notas sin perder decimales
            } else if (value instanceof Integer number) {
                statement.setInt(index, number);
            } else if (value instanceof Boolean flag) {
                statement.setBoolean(index, flag);
            } else {
                statement.setString(index, value.toString());               // cualquier otro dato como texto
            }
        }
    }

    // ====================== MANEJO DE ERRORES ======================

    /** Registra el error en error_log.txt y crea una excepción con un mensaje entendible. */
    private DataAccessException fail(String operation, String sql, SQLException e) {
        logger.error(getClass().getSimpleName(), "SQL " + operation + " failed: " + sql, e);
        return new DataAccessException(friendlyMessage(e), e);
    }

    /** Traduce los códigos de error de MySQL a mensajes que el usuario entienda. */
    private String friendlyMessage(SQLException e) {
        return switch (e.getErrorCode()) {
            case 1062 -> "Ya existe un registro con ese dato único (documento, código o número).";  // clave duplicada
            case 1451 -> "No se puede eliminar: el registro tiene información relacionada.";         // la fila es referenciada
            case 1452 -> "El registro relacionado no existe.";                                       // llave foránea inválida
            case 3819 -> "Un valor no cumple las reglas de la base de datos.";                       // falla un CHECK
            case 0 -> "No hay conexión con la base de datos. Verifique que MySQL esté encendido.";   // sin conexión
            default -> "Error de base de datos (código " + e.getErrorCode() + ").";
        };
    }

    // ====================== AYUDAS PARA LOS MAPPERS ======================
    // Los DAO hijos las usan al convertir una fila en un objeto.

    /** Lee una fecha que puede ser NULL (getDate devuelve null y no se puede convertir directamente). */
    protected static LocalDate getDate(ResultSet rs, String column) throws SQLException {
        Date date = rs.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    /** Lee una hora que puede ser NULL. */
    protected static LocalTime getTime(ResultSet rs, String column) throws SQLException {
        Time time = rs.getTime(column);
        return time == null ? null : time.toLocalTime();
    }

    /** Lee una columna INT que puede ser NULL como Integer (getInt convertiría NULL en 0). */
    protected static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;       // wasNull() dice si la última columna leída era NULL
    }

    /** Llena las columnas que comparten las tablas estudiantes, acudientes y profesores. */
    protected static void mapPerson(ResultSet rs, Person person, String idColumn) throws SQLException {
        person.setId(rs.getInt(idColumn));
        person.setDocumentType(DocumentType.fromDbValue(rs.getString("tipo_documento")));
        person.setDocumentNumber(rs.getString("numero_documento"));
        person.setFirstName(rs.getString("primer_nombre"));
        person.setMiddleName(rs.getString("segundo_nombre"));
        person.setLastName(rs.getString("primer_apellido"));
        person.setSecondLastName(rs.getString("segundo_apellido"));
        person.setEmail(rs.getString("correo_electronico"));
    }
}
