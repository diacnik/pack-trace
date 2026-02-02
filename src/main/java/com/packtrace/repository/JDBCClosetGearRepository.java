package com.packtrace.repository;

import com.packtrace.dto.ClosetGearResponse;
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
public class JDBCClosetGearRepository implements ClosetGearRepository {

    @Inject
    AgroalDataSource dataSource;

    @Override
    public void addGearToCloset(Long closetId, Long gearId, int quantity) {
        String sql = """
                INSERT INTO closet_gear (closet_id, gear_id, quantity)
                VALUES (?, ?, ?)
                ON CONFLICT (closet_id, gear_id)
                DO UPDATE SET quantity = quantity + ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, closetId);
            preparedStatement.setLong(2, gearId);
            preparedStatement.setInt(3, quantity);
            preparedStatement.setInt(4, quantity);

            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error adding gear to closet", sqlException);
        }
    }

    @Override
    public void removeGearFromCloset(Long closetId, Long gearId) {
        String sql = """
                DELETE FROM closet_gear
                WHERE closet_id = ? AND gear_id = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, closetId);
            preparedStatement.setLong(2, gearId);

            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error removing gear from closet", sqlException);
        }
    }

    @Override
    public List<ClosetGearResponse> findGearInCloset(Long closetId) {
        String sql = """
                SELECT g.id, g.name, g.brand, g.weight_grams, cg.quantity
                FROM closet_gear cg
                JOIN gear g ON cg.gear_id = g.id
                WHERE cg.closet_id = ?;
                """;

        List<ClosetGearResponse> gearList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, closetId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    gearList.add(new ClosetGearResponse(
                            resultSet.getLong("id"),
                            resultSet.getString("name"),
                            resultSet.getString("brand"),
                            resultSet.getObject("weight_grams", Integer.class),
                            resultSet.getInt("quantity")
                    ));
                }
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error finding gear in closet", sqlException);
        }
        return gearList;
    }

    @Override
    public Optional<Integer> getQuantity(Long closetId, Long gearId) {
        String sql = """
                SELECT quantity
                FROM closet_gear
                WHERE closet_id = ? AND gear_id = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, closetId);
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
    public void updateQuantity(Long closetId, Long gearId, int quantity) {
        String sql = """
                UPDATE closet_gear
                SET quantity = ?
                WHERE closet_id = ? AND gear_id = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, quantity);
            preparedStatement.setLong(2, closetId);
            preparedStatement.setLong(3, gearId);

            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error updating gear quantity in closet", sqlException);
        }
    }
}
