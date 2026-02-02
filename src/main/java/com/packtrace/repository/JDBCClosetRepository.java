package com.packtrace.repository;

import com.packtrace.model.Closet;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JDBCClosetRepository implements ClosetRepository {

    @Inject
    AgroalDataSource dataSource;

    @Override
    public void persist(Closet closet) {
        String sql = """
                INSERT INTO closet (account_id, name, description)
                VALUES (?, ?, ?)
                RETURNING id;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, closet.getAccountId());
            preparedStatement.setString(2, closet.getName());
            preparedStatement.setString(3, closet.getDescription());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    closet.setId(resultSet.getLong(1));
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error persisting closet", sqlException);
        }
    }

    @Override
    public Optional<Closet> findById(Long id) {
        String sql = """
                SELECT id, account_id, name, description
                FROM closet
                WHERE id = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    return Optional.of(mapRow(resultSet));
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error finding closet by id", sqlException);
        }
        return Optional.empty();
    }

    @Override
    public List<Closet> findByAccountId(UUID accountId) {
        String sql = """
                SELECT id, account_id, name, description
                FROM closet
                WHERE account_id = ?;
                """;
        List<Closet> closets = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, accountId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next())
                    closets.add(mapRow(resultSet));
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error finding closets by account id", sqlException);
        }
        return closets;
    }

    @Override
    public void update(Closet closet) {
        String sql = """
                UPDATE closet
                SET name = ?, description = ?
                WHERE id = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, closet.getName());
            preparedStatement.setString(2, closet.getDescription());
            preparedStatement.setLong(3, closet.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error updating closet", sqlException);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = """
                DELETE FROM closet
                WHERE id = ?;
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();

        } catch (SQLException sqlException) {
            throw new RuntimeException("Error deleting closet", sqlException);
        }
    }

    @Override
    public boolean existsByAccountIdAndName(UUID accountId, String name) {
        String sql = """
                SELECT 1
                FROM closet
                WHERE account_id = ? AND name = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, accountId);
            preparedStatement.setString(2, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error checking closet existence", sqlException);
        }
    }

    private Closet mapRow(ResultSet resultSet) throws SQLException {
        return new Closet(
                resultSet.getLong("id"),
                resultSet.getObject("account_id", java.util.UUID.class),
                resultSet.getString("name"),
                resultSet.getString("description")
        );
    }
}
