package com.moneyai.service;

import com.moneyai.dto.TransactionRequest;
import com.moneyai.exception.NotFoundException;
import com.moneyai.model.Transaction;
import com.moneyai.model.TransactionType;
import com.moneyai.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionServiceTest {

    private TransactionRepository repository;
    private TransactionService service;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepository.class);
        service = new TransactionService(repository);
        when(repository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static TransactionRequest request(String description, String category, LocalDate date) {
        return new TransactionRequest(description, new BigDecimal("25.90"), TransactionType.EXPENSE, category, date);
    }

    @Test
    void criaComCategoriaEDataPadraoQuandoNaoInformadas() {
        Transaction saved = service.create(request("  Café  ", null, null));

        assertThat(saved.getDescription()).isEqualTo("Café");
        assertThat(saved.getCategory()).isEqualTo("Outros");
        assertThat(saved.getDate()).isEqualTo(LocalDate.now());
        assertThat(saved.getAmount()).isEqualByComparingTo("25.90");
        assertThat(saved.getType()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    void categoriaEmBrancoViraOutros() {
        assertThat(service.create(request("Café", "   ", null)).getCategory()).isEqualTo("Outros");
    }

    @Test
    void mantemCategoriaEDataInformadas() {
        LocalDate date = LocalDate.of(2026, 5, 10);

        Transaction saved = service.create(request("Cinema", "Lazer", date));

        assertThat(saved.getCategory()).isEqualTo("Lazer");
        assertThat(saved.getDate()).isEqualTo(date);
    }

    @Test
    void buscaPorIdInexistenteLancaNotFound() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(9L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Lançamento 9 não encontrado.");
    }

    @Test
    void atualizaOsCamposDoLancamentoExistente() {
        Transaction existing = new Transaction();
        existing.setDescription("Antigo");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        Transaction updated = service.update(1L, request("Novo", "Lazer", LocalDate.of(2026, 1, 2)));

        assertThat(updated.getDescription()).isEqualTo("Novo");
        assertThat(updated.getCategory()).isEqualTo("Lazer");
    }

    @Test
    void atualizarInexistenteLancaNotFound() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(5L, request("X", null, null))).isInstanceOf(NotFoundException.class);
    }

    @Test
    void excluiOLancamentoEncontrado() {
        Transaction existing = new Transaction();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(repository).delete(existing);
    }

    @Test
    void excluirInexistenteLancaNotFound() {
        when(repository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(3L)).isInstanceOf(NotFoundException.class);
    }
}
