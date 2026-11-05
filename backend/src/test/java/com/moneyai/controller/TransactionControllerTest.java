package com.moneyai.controller;

import com.moneyai.exception.ApiExceptionHandler;
import com.moneyai.exception.NotFoundException;
import com.moneyai.model.Transaction;
import com.moneyai.model.TransactionType;
import com.moneyai.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testa o contrato HTTP (status e JSON) que o frontend consome. */
class TransactionControllerTest {

    private static final String VALID = """
            {"description":"Mercado","amount":50.5,"type":"EXPENSE","category":"Mercado","date":"2026-11-03"}""";

    private TransactionService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(TransactionService.class);
        mvc = MockMvcBuilders.standaloneSetup(new TransactionController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    private static Transaction sample() {
        Transaction t = new Transaction();
        t.setDescription("Mercado");
        t.setAmount(new BigDecimal("50.50"));
        t.setType(TransactionType.EXPENSE);
        t.setCategory("Mercado");
        t.setDate(LocalDate.of(2026, 11, 3));
        return t;
    }

    @Test
    void listaLancamentos() throws Exception {
        when(service.findAll()).thenReturn(List.of(sample()));

        mvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Mercado"))
                .andExpect(jsonPath("$[0].type").value("EXPENSE"))
                .andExpect(jsonPath("$[0].date").value("2026-11-03"));
    }

    @Test
    void criaLancamentoERetorna201() throws Exception {
        when(service.create(any())).thenReturn(sample());

        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Mercado"));
    }

    @Test
    void rejeitaLancamentoSemDescricaoComMensagemParaOUsuario() throws Exception {
        String invalid = """
                {"description":"","amount":10,"type":"EXPENSE"}""";

        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Confira os campos: descrição, valor e tipo são obrigatórios."));
    }

    @Test
    void rejeitaValorNegativo() throws Exception {
        String invalid = """
                {"description":"X","amount":-5,"type":"EXPENSE"}""";

        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscaInexistenteRetorna404ComDetalhe() throws Exception {
        when(service.findById(9L)).thenThrow(new NotFoundException("Lançamento 9 não encontrado."));

        mvc.perform(get("/api/transactions/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Lançamento 9 não encontrado."));
    }

    @Test
    void atualizaLancamento() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(sample());

        mvc.perform(put("/api/transactions/1").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Mercado"));
    }

    @Test
    void excluiLancamentoERetorna204() throws Exception {
        mvc.perform(delete("/api/transactions/1")).andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}
