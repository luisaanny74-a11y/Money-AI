package com.moneyai.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/** Extrai texto de PDF, DOCX e PNG (via OCR). */
@Service
public class DocumentReaderService {

    private final String ocrDataPath;
    private final String ocrLanguage;

    public DocumentReaderService(@Value("${moneyai.ocr.datapath}") String ocrDataPath,
                                 @Value("${moneyai.ocr.language}") String ocrLanguage) {
        this.ocrDataPath = ocrDataPath;
        this.ocrLanguage = ocrLanguage;
    }

    public String read(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        try (InputStream in = file.getInputStream()) {
            if (name.endsWith(".pdf"))  return readPdf(in);
            if (name.endsWith(".docx")) return readDocx(in);
            if (name.endsWith(".png"))  return readImage(in);
        }
        throw new IllegalArgumentException("Formato não suportado. Envie PDF, DOCX ou PNG.");
    }

    private String readPdf(InputStream in) throws IOException {
        try (PDDocument doc = Loader.loadPDF(in.readAllBytes())) {
            return new PDFTextStripper().getText(doc).trim();
        }
    }

    private String readDocx(InputStream in) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(in); XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText().trim();
        }
    }

    private String readImage(InputStream in) throws IOException {
        BufferedImage image = ImageIO.read(in);
        if (image == null) throw new IllegalArgumentException("Não consegui abrir a imagem.");
        Tesseract ocr = new Tesseract();
        ocr.setDatapath(ocrDataPath);
        ocr.setLanguage(ocrLanguage);
        try {
            return ocr.doOCR(image).trim();
        } catch (TesseractException e) {
            throw new IllegalStateException("Falha no OCR. Verifique a instalação do Tesseract e o caminho tessdata.", e);
        }
    }
}
