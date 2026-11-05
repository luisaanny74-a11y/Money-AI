package com.moneyai.service;

import com.moneyai.dto.ChatResponse;
import com.moneyai.dto.TransactionRequest;
import com.moneyai.model.TransactionType;
import com.moneyai.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdvisorServiceTest {

    private TransactionService transactions;
    private TransactionRepository repository;
    private AdvisorService advisor;

    @BeforeEach
    void setUp() {
        transactions = mock(TransactionService.class);
        repository = mock(TransactionRepository.class);
        advisor = new AdvisorService(transactions, repository);
    }

    private void givenTotals(String income, String expense) {
        when(repository.sumByType(TransactionType.INCOME)).thenReturn(new BigDecimal(income));
        when(repository.sumByType(TransactionType.EXPENSE)).thenReturn(new BigDecimal(expense));
    }

    private TransactionRequest registered() {
        ArgumentCaptor<TransactionRequest> captor = ArgumentCaptor.forClass(TransactionRequest.class);
        verify(transactions).create(captor.capture());
        return captor.getValue();
    }

    // ---------- registro por texto ----------

    @Test
    void registraGastoComValorDecimalEDescricao() {
        ChatResponse response = advisor.reply("Gastei 50,50 em Mercado");

        TransactionRequest request = registered();
        assertThat(request.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(request.amount()).isEqualByComparingTo("50.50");
        assertThat(request.description()).isEqualTo("Mercado");
        assertThat(response.dataChanged()).isTrue();
        assertThat(response.reply()).contains("Gasto").contains("Mercado");
    }

    @Test
    void registraReceitaIgnorandoAcentos() {
        ChatResponse response = advisor.reply("recebi 3000 de salário");

        TransactionRequest request = registered();
        assertThat(request.type()).isEqualTo(TransactionType.INCOME);
        assertThat(request.amount()).isEqualByComparingTo("3000");
        assertThat(request.description()).isEqualTo("Salario");
        assertThat(response.dataChanged()).isTrue();
    }

    @Test
    void gastoSemDescricaoUsaTextoPadrao() {
        advisor.reply("gastei 20");

        assertThat(registered().description()).isEqualTo("Sem descrição");
    }

    // ---------- consultas ----------

    @Test
    void saldoMostraReceitasDespesasESaldo() {
        givenTotals("3000", "50.50");

        ChatResponse response = advisor.reply("Ver meu saldo");

        assertThat(response.reply()).contains("3.000,00").contains("50,50").contains("2.949,50");
        assertThat(response.dataChanged()).isFalse();
        assertThat(response.suggestions()).startsWith("Analisar gastos");
        verify(transactions, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void gastosPorCategoriaListaCadaCategoria() {
        when(repository.expensesByCategory()).thenReturn(List.of(
                new Object[]{"Mercado", new BigDecimal("150.00")},
                new Object[]{"Lazer", new BigDecimal("80.00")}));

        ChatResponse response = advisor.reply("Analisar gastos");

        assertThat(response.reply()).contains("Mercado").contains("150,00").contains("Lazer").contains("80,00");
    }

    @Test
    void gastosPorCategoriaSemDespesasOrientaOUsuario() {
        when(repository.expensesByCategory()).thenReturn(List.of());

        assertThat(advisor.reply("analisar gastos").reply()).contains("ainda não tem despesas");
    }

    // ---------- orçamento ----------

    @Test
    void orcamentoDivide50_30_20SobreARenda() {
        givenTotals("3000", "0");

        String reply = advisor.reply("Criar orçamento").reply();

        assertThat(reply).contains("1.500,00").contains("900,00").contains("600,00");
    }

    @Test
    void orcamentoSemRendaPedeARenda() {
        givenTotals("0", "0");

        assertThat(advisor.reply("Criar orçamento").reply()).contains("recebi 3000");
    }

    // ---------- dicas ----------

    @Test
    void dicaAvisaQuandoOGastoPassaDaRenda() {
        givenTotals("1000", "1200");

        assertThat(advisor.reply("Dicas de economia").reply()).contains("120%").contains("vermelho");
    }

    @Test
    void dicaSugereRegra50_30_20QuandoAMargemEApertada() {
        givenTotals("1000", "900");

        assertThat(advisor.reply("Dicas de economia").reply()).contains("90%").contains("50/30/20");
    }

    @Test
    void dicaIncentivaReservaQuandoOGastoEBaixo() {
        givenTotals("1000", "500");

        ChatResponse response = advisor.reply("Dicas de economia");

        assertThat(response.reply()).contains("50%").contains("reserva de emergência");
        assertThat(response.suggestions()).contains("Aprender a investir");
    }

    @Test
    void dicaSemRendaPedeParaRegistrarReceitas() {
        givenTotals("0", "0");

        assertThat(advisor.reply("dicas para poupar").reply()).contains("Registre suas receitas");
    }

    // ---------- roteamento ----------

    @Test
    void aprenderAInvestirNaoCaiEmDicas() {
        String reply = advisor.reply("Aprender a investir").reply();

        assertThat(reply).contains("reserva de emergência").contains("não recomendação");
        verify(repository, never()).sumByType(TransactionType.INCOME);
    }

    @Test
    void ignoraMaiusculasEAcentosNaIntencao() {
        givenTotals("3000", "0");

        assertThat(advisor.reply("CRIAR ORÇAMENTO").reply()).contains("1.500,00");
    }

    @Test
    void mensagemDesconhecidaMostraAjuda() {
        ChatResponse response = advisor.reply("oi tudo bem?");

        assertThat(response.reply()).startsWith("Posso ajudar");
        assertThat(response.dataChanged()).isFalse();
    }

    // ---------- documentos ----------

    @Test
    void documentoComValoresInformaQuantidadeETotal() {
        String reply = advisor.analyzeDocument("fatura.pdf", "Mercado R$ 1.234,56\nPadaria R$ 45,90\nTotal 12,00");

        assertThat(reply).contains("fatura.pdf").contains("2 valores").contains("1.280,46").contains("1.234,56");
    }

    @Test
    void documentoSemValoresAvisaEMostraPrevia() {
        String reply = advisor.analyzeDocument("nota.docx", "Texto sem dinheiro");

        assertThat(reply).contains("não encontrei valores").contains("Texto sem dinheiro");
    }

    @Test
    void previaDeDocumentoLongoETruncada() {
        String reply = advisor.analyzeDocument("a.pdf", "x".repeat(1000));

        assertThat(reply).contains("…").doesNotContain("x".repeat(401));
    }
}
