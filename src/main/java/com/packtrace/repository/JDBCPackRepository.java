package com.packtrace.repository;

import com.packtrace.model.Pack;
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
public class JDBCPackRepository implements PackRepository {

    @Inject
    AgroalDataSource dataSource;

    @Override
    public void persist(Pack pack) {
        String sql = """
                INSERT INTO pack (account_id, name, description)
                VALUES (?, ?, ?)
                RETURNING id;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, pack.getAccountId());
            preparedStatement.setString(2, pack.getName());
            preparedStatement.setString(3, pack.getDescription());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    pack.setId(resultSet.getLong(1));
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error persisting pack", sqlException);
        }
    }

    @Override
    public Optional<Pack> findById(Long id) {
        String sql = """
                SELECT id, account_id, name, description
                FROM pack
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
            throw new RuntimeException("Error finding pack by id", sqlException);
        }
        return Optional.empty();
    }

    @Override
    public List<Pack> findByAccountId(UUID accountId) {
        String sql = """
                SELECT id, account_id, name, description
                FROM pack
                WHERE account_id = ?;
                """;
        List<Pack> packs = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, accountId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next())
                    packs.add(mapRow(resultSet));
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error finding packs by account id", sqlException);
        }
        return packs;
    }

    @Override
    public void update(Pack pack) {
        String sql = """
                UPDATE pack
                SET name = ?, description = ?
                WHERE id = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, pack.getName());
            preparedStatement.setString(2, pack.getDescription());
            preparedStatement.setLong(3, pack.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error updating pack", sqlException);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = """
                DELETE FROM pack
                WHERE id = ?;
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();

        } catch (SQLException sqlException) {
            throw new RuntimeException("Error deleting pack", sqlException);
        }
    }

    @Override
    public boolean existsByAccountIdAndName(UUID accountId, String name) {
        String sql = """
                SELECT 1
                FROM pack
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
            throw new RuntimeException("Error checking pack existence", sqlException);
        }
    }

    private Pack mapRow(ResultSet resultSet) throws SQLException {
        return new Pack(
                resultSet.getLong("id"),
                resultSet.getObject("account_id", java.util.UUID.class),
                resultSet.getString("name"),
                resultSet.getString("description")
        );
    }
}
