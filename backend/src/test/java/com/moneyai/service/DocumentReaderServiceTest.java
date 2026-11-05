package com.moneyai.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Os arquivos de teste são gerados em memória; o OCR de PNG depende do Tesseract e não é coberto aqui. */
class DocumentReaderServiceTest {

    private final DocumentReaderService reader = new DocumentReaderService("/tmp", "por");

    @Test
    void lePdf() throws IOException {
        byte[] pdf;
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(50, 700);
                content.showText("Mercado R$ 50,00");
                content.endText();
            }
            doc.save(out);
            pdf = out.toByteArray();
        }

        String text = reader.read(new MockMultipartFile("file", "extrato.pdf", "application/pdf", pdf));

        assertThat(text).contains("Mercado R$ 50,00");
    }

    @Test
    void leDocx() throws IOException {
        byte[] docx;
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.createParagraph().createRun().setText("Aluguel R$ 1.200,00");
            doc.write(out);
            docx = out.toByteArray();
        }

        String text = reader.read(new MockMultipartFile("file", "contas.DOCX", "application/octet-stream", docx));

        assertThat(text).contains("Aluguel R$ 1.200,00");
    }

    @Test
    void rejeitaFormatoNaoSuportado() {
        MockMultipartFile txt = new MockMultipartFile("file", "notas.txt", "text/plain", "oi".getBytes());

        assertThatThrownBy(() -> reader.read(txt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PDF, DOCX ou PNG");
    }
}
