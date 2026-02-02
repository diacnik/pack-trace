package com.packtrace.repository;

import com.packtrace.dto.PackGearResponse;
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
public class JDBCPackGearRepository implements PackGearRepository {

    @Inject
    AgroalDataSource dataSource;

    @Override
    public void addGearToPack(Long packId, Long gearId, int quantity) {
        String sql = """
                INSERT INTO pack_gear (pack_id, gear_id, quantity)
                VALUES (?, ?, ?)
                ON CONFLICT (pack_id, gear_id)
                DO UPDATE SET quantity = quantity + ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, packId);
            preparedStatement.setLong(2, gearId);
            preparedStatement.setInt(3, quantity);
            preparedStatement.setInt(4, quantity);

            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error adding gear to pack", sqlException);
        }
    }

    @Override
    public void removeGearFromPack(Long packId, Long gearId) {
        String sql = """
                DELETE FROM pack_gear
                WHERE pack_id = ? AND gear_id = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, packId);
            preparedStatement.setLong(2, gearId);

            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error removing gear from pack", sqlException);
        }
    }

    @Override
    public List<PackGearResponse> findGearInPack(Long packId) {
        String sql = """
                SELECT g.id, g.name, g.brand, g.weight_grams, pg.quantity
                FROM pack_gear pg
                JOIN gear g ON pg.gear_id = g.id
                WHERE pg.pack_id = ?;
                """;

        List<PackGearResponse> gearList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, packId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    gearList.add(new PackGearResponse(
                            resultSet.getLong("id"),
                            resultSet.getString("name"),
                            resultSet.getString("brand"),
                            resultSet.getObject("weight_grams", Integer.class),
                            resultSet.getInt("quantity")
                    ));
                }
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error finding gear in pack", sqlException);
        }
        return gearList;
    }

    @Override
    public Optional<Integer> getQuantity(Long packId, Long gearId) {
        String sql = """
                SELECT quantity
                FROM pack_gear
                WHERE pack_id = ? AND gear_id = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, packId);
            preparedStatement.setLong(2, gearId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    return Optional.of(resultSet.getInt("quantity"));
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error getting gear quantity", sqlException);
        }
        return Optional.empty();
    }

    @Override
    public void updateQuantity(Long packId, Long gearId, int quantity) {
        String sql = """
                UPDATE pack_gear
                SET quantity = ?
                WHERE pack_id = ? AND gear_id = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, quantity);
            preparedStatement.setLong(2, packId);
            preparedStatement.setLong(3, gearId);

            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error updating gear quantity in pack", sqlException);
        }
    }
}
