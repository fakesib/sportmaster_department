package com.fakesibwork.sportmaster.repo;

import com.fakesibwork.sportmaster.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional
public interface TransactionRepo extends JpaRepository<Transaction, Integer> {
    @Modifying
    @Query(value = "INSERT INTO transactions(id, transaction, chat, sum, type) values(DEFAULT, ?1, ?2, ?3, ?4)", nativeQuery = true)
    void createTransaction(String transaction, long userId, int sum, String type);

    @Modifying
    @Query(value = "DELETE FROM transactions WHERE transaction = ?1", nativeQuery = true)
    void deleteByTransaction(String transaction);

    @Query(value = "SELECT * FROM transactions WHERE transaction = ?1", nativeQuery = true)
    Transaction findByTransaction(String transaction);
}
