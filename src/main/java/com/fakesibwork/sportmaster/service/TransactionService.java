package com.fakesibwork.sportmaster.service;

import com.fakesibwork.sportmaster.model.Transaction;
import com.fakesibwork.sportmaster.repo.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepo transactionRepo;

    public String createTransaction(long userId, String sum, String type) {
        try {
            UUID id = UUID.randomUUID();
            transactionRepo.createTransaction(id.toString(), userId, Integer.parseInt(sum), type);
            return "Номер транзакции: <code>" + id + "</code>\n\n<i>*Нажмите на номер чтобы скопировать</i>";
        } catch (Exception e){
            return "Exception transaction";
        }
    }

    public Transaction deleteTransaction(String transaction) {
        Transaction transactionData = transactionRepo.findByTransaction(transaction);
        transactionRepo.deleteByTransaction(transaction);
        return transactionData;
    }

    public void resetTransactions() {
        transactionRepo.deleteAll();
    }
}
