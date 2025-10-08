package com.sih.erp.repository;

import com.sih.erp.entity.XPTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface XPTransactionRepository extends JpaRepository<XPTransaction, UUID> {

    /**
     * Calculates the sum of all XP points for a specific user.
     * This will be used for the leaderboard and displaying a student's total XP.
     * @param userId The UUID of the user.
     * @return The total XP as an Integer, or 0 if they have none.
     */
    @Query("SELECT SUM(xpt.points) FROM XPTransaction xpt WHERE xpt.user.userId = :userId")
    Integer findTotalXpByUserId(UUID userId);

}