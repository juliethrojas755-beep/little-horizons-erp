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
 * Base class for every JDBC DAO.
 * <p>
 * It centralizes the repetitive JDBC code (Template Method idea): open the connection, create the
 * {@link PreparedStatement}, bind parameters, run, map rows and close everything with
 * try-with-resources. Subclasses only write SQL and a {@link RowMapper}.
 * </p>
 */
public abstract class BaseJdbcDao {

    private final ErrorLogger logger = ErrorLogger.getInstance();

    /** Runs a SELECT and maps every row. */
    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(mapper.map(resultSet));
                }
                return rows;
            }
        } catch (SQLException e) {
            throw fail("query", sql, e);
        }
    }

    /** Runs a SELECT that returns at most one row. */
    protected <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
        return queryList(sql, mapper, params).stream().findFirst();
    }

    /** Runs a SELECT that returns a single number (COUNT, MAX...). */
    protected int queryInt(String sql, Object... params) {
        return queryOne(sql, resultSet -> resultSet.getInt(1), params).orElse(0);
    }

    /** Runs an INSERT, UPDATE or DELETE with its own connection. */
    protected int update(String sql, Object... params) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            return update(connection, sql, params);
        } catch (SQLException e) {
            throw fail("update", sql, e);
        }
    }

    /** Same as {@link #update(String, Object...)} but inside an external transaction. */
    protected int update(Connection connection, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);
            return statement.executeUpdate();
        }
    }

    /** Runs an INSERT and returns the AUTO_INCREMENT id. */
    protected int insert(String sql, Object... params) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            return insert(connection, sql, params);
        } catch (SQLException e) {
            throw fail("insert", sql, e);
        }
    }

    /** Same as {@link #insert(String, Object...)} but inside an external transaction. */
    protected int insert(Connection connection, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, params);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    /** Binds every parameter with the right JDBC type; Java types are converted here only. */
    private void bind(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object value = params[i];
            int index = i + 1;
            if (value == null) {
                statement.setNull(index, Types.NULL);
            } else if (value instanceof DbEnum dbEnum) {
                statement.setString(index, dbEnum.getDbValue());
            } else if (value instanceof LocalDate date) {
                statement.setDate(index, Date.valueOf(date));
            } else if (value instanceof LocalTime time) {
                statement.setTime(index, Time.valueOf(time));
            } else if (value instanceof BigDecimal decimal) {
                statement.setBigDecimal(index, decimal);
            } else if (value instanceof Integer number) {
                statement.setInt(index, number);
            } else if (value instanceof Boolean flag) {
                statement.setBoolean(index, flag);
            } else {
                statement.setString(index, value.toString());
            }
        }
    }

    private DataAccessException fail(String operation, String sql, SQLException e) {
        logger.error(getClass().getSimpleName(), "SQL " + operation + " failed: " + sql, e);
        return new DataAccessException(friendlyMessage(e), e);
    }

    /** Translates common MySQL error codes into messages the user understands. */
    private String friendlyMessage(SQLException e) {
        return switch (e.getErrorCode()) {
            case 1062 -> "Ya existe un registro con ese dato único (documento, código o número).";
            case 1451 -> "No se puede eliminar: el registro tiene información relacionada.";
            case 1452 -> "El registro relacionado no existe.";
            case 3819 -> "Un valor no cumple las reglas de la base de datos.";
            case 0 -> "No hay conexión con la base de datos. Verifique que MySQL esté encendido.";
            default -> "Error de base de datos (código " + e.getErrorCode() + ").";
        };
    }

    // ---------- reading helpers used by the row mappers ----------

    protected static LocalDate getDate(ResultSet rs, String column) throws SQLException {
        Date date = rs.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    protected static LocalTime getTime(ResultSet rs, String column) throws SQLException {
        Time time = rs.getTime(column);
        return time == null ? null : time.toLocalTime();
    }

    /** Reads a nullable INT column as {@link Integer} (getInt would turn NULL into 0). */
    protected static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    /** Fills the columns shared by estudiantes, acudientes and profesores. */
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
