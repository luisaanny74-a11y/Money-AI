package com.moneyai.service;

import com.moneyai.dto.TransactionRequest;
import com.moneyai.exception.NotFoundException;
import com.moneyai.model.Transaction;
import com.moneyai.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TransactionService {

    private static final String DEFAULT_CATEGORY = "Outros";

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction create(TransactionRequest request) {
        return repository.save(apply(new Transaction(), request));
    }

    @Transactional(readOnly = true)
    public List<Transaction> findAll() {
        return repository.findAllByOrderByDateDescIdDesc();
    }

    @Transactional(readOnly = true)
    public Transaction findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lançamento " + id + " não encontrado."));
    }

    public Transaction update(Long id, TransactionRequest request) {
        return apply(findById(id), request);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private Transaction apply(Transaction t, TransactionRequest r) {
        t.setDescription(r.description().trim());
        t.setAmount(r.amount());
        t.setType(r.type());
        t.setCategory(r.category() == null || r.category().isBlank() ? DEFAULT_CATEGORY : r.category().trim());
        t.setDate(r.date() == null ? LocalDate.now() : r.date());
        return t;
    }
}
