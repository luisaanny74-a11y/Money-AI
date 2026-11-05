package com.moneyai.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentValueExtractorTest {

    @Test
    void extraiValoresComMilharEDecimais() {
        List<BigDecimal> values = DocumentValueExtractor.extract("Total R$ 1.234,56 e R$ 10,00");

        assertThat(values).hasSize(2);
        assertThat(values.get(0)).isEqualByComparingTo("1234.56");
        assertThat(values.get(1)).isEqualByComparingTo("10.00");
    }

    @Test
    void aceitaSimboloSemEspaco() {
        assertThat(DocumentValueExtractor.extract("R$10,50")).hasSize(1);
    }

    @Test
    void valoresMaioresQueMilhar() {
        assertThat(DocumentValueExtractor.extract("R$ 1.234.567,89").get(0)).isEqualByComparingTo("1234567.89");
    }

    @Test
    void ignoraNumerosSemSimboloDeReais() {
        assertThat(DocumentValueExtractor.extract("Quantidade 12,00 e código 4455")).isEmpty();
    }

    @Test
    void textoVazioNaoGeraValores() {
        assertThat(DocumentValueExtractor.extract("")).isEmpty();
    }
}
