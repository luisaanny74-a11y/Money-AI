package com.moneyai.repository;

import com.moneyai.model.Transaction;
import com.moneyai.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByOrderByDateDescIdDesc();

    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.type = :type")
    BigDecimal sumByType(TransactionType type);

    @Query("""
            select t.category, sum(t.amount) from Transaction t
            where t.type = com.moneyai.model.TransactionType.EXPENSE
            group by t.category order by sum(t.amount) desc""")
    List<Object[]> expensesByCategory();
}
