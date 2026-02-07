package com.packtrace.repository;

import com.packtrace.model.Gear;
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
public class JDBCGearRepository implements GearRepository{

    @Inject
    AgroalDataSource dataSource;

    @Override
    public void deleteById(Long id) {
        String sql = """
                DELETE FROM gear
                WHERE id = ?;
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();

        } catch (SQLException sqlException) {
            throw new RuntimeException("Error deleting gear", sqlException);
        }
    }

    @Override
    public Optional<Gear> findById(Long id) {
        String sql = """
                SELECT id, owner_id, name, brand, weight_grams, website_url, category
                FROM gear
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
            throw new RuntimeException("Error finding gear by id", sqlException);
        }
        return Optional.empty();
    }

    @Override
    public List<Gear> findByOwnerId(UUID ownerId) {
        String sql = """
                SELECT id, owner_id, name, brand, weight_grams, website_url, category
                FROM gear
                WHERE owner_id = ?;
                """;
        List<Gear> gears = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, ownerId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next())
                    gears.add(mapRow(resultSet));
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error finding gear by owner id", sqlException);
        }
        return gears;
    }

    private Gear mapRow(ResultSet resultSet) throws SQLException {
        return new Gear(
                resultSet.getLong("id"),
                resultSet.getObject("owner_id", java.util.UUID.class),
                resultSet.getString("name"),
                resultSet.getString("brand"),
                resultSet.getObject("weight_grams", java.lang.Integer.class),
                resultSet.getString("website_url"),
                resultSet.getString("category")
        );
    }

    @Override
    public void persist(Gear gear) {
        String sql = """
                INSERT INTO gear (owner_id, name, brand, weight_grams, website_url, category)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, gear.getOwnerId());
            preparedStatement.setString(2, gear.getName());
            preparedStatement.setString(3, gear.getBrand());
            // Handle nullable weight_grams
            if (gear.getWeightGrams() != null) {
                preparedStatement.setInt(4, gear.getWeightGrams());
            } else {
                preparedStatement.setNull(4, java.sql.Types.INTEGER);
            }
            preparedStatement.setString(5, gear.getWebsiteURL());
            preparedStatement.setString(6, gear.getCategory());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    gear.setId(resultSet.getLong(1));
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error creating gear", sqlException);
        }
    }

    @Override
    public void update(Gear gear) {
        String sql = """
                UPDATE gear
                SET name = ?, brand = ?, weight_grams = ?, website_url = ?, category = ?
                WHERE id = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, gear.getName());
            preparedStatement.setString(2, gear.getBrand());
            // Handle nullable weight_grams
            if (gear.getWeightGrams() != null) {
                preparedStatement.setInt(3, gear.getWeightGrams());
            } else {
                preparedStatement.setNull(3, java.sql.Types.INTEGER);
            }
            preparedStatement.setString(4, gear.getWebsiteURL());
            preparedStatement.setString(5, gear.getCategory());
            preparedStatement.setLong(6, gear.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error updating gear", sqlException);
        }
    }
}
