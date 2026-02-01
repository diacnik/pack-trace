package com.packtrace.repository;

import com.packtrace.model.Account;
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

@ApplicationScoped
public class JDBCAccountRepository implements AccountRepository{

    @Inject
    AgroalDataSource dataSource;

    @Override
    public Optional<Account> findByAuth0Id(String auth0Id) {
        String sql = """
                SELECT id, auth0_id, name, bio
                FROM account
                WHERE auth0_id = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, auth0Id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error executing findByAuth0Id", sqlException);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        String sql = """
                SELECT id, auth0_id, username, bio
                FROM account
                WHERE username = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    return Optional.of(mapRow(resultSet));
            }

        } catch (SQLException sqlException) {
            throw new RuntimeException("Error executing findByUsername", sqlException);
        }
        return Optional.empty();
    }

    @Override
    public List<String> findAllUsernamesStartingWith(String prefix) {
        String sql = """
                SELECT name
                FROM account
                WHERE name = ? OR name LIKE ?;
                """;
        List<String> usernames = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, prefix);
            preparedStatement.setString(2, prefix + "-%");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next())
                    usernames.add(resultSet.getString("name"));
            }

        } catch (SQLException sqlException) {
            throw new RuntimeException("Error executing findAllUsernamesStartingWith", sqlException);
        }
        return usernames;
    }

    private Account mapRow(ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getObject("id", java.util.UUID.class),
                resultSet.getString("auth0_id"),
                resultSet.getString("username"),
                resultSet.getString("bio")
        );
    }

    @Override
    public void persist(Account account) {
        String sql = """
                INSERT INTO account (auth0_id, username)
                VALUES (?, ?)
                RETURNING id;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement =  connection.prepareStatement(sql)) {

            preparedStatement.setString(1, account.getAuth0Id());
            preparedStatement.setString(2, account.getUsername());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    account.setId(resultSet.getObject(1, java.util.UUID.class));
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error persisting account", sqlException);
        }
    }
}
