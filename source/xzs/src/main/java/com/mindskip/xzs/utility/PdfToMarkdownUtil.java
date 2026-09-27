package com.mindskip.xzs.utility;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PdfToMarkdownUtil {

    public static String convertPdfToMarkdown(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return cleanAndFormatToMarkdown(text);
        }
    }

    public static String convertPdfToMarkdown(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return cleanAndFormatToMarkdown(text);
        }
    }

    private static String cleanAndFormatToMarkdown(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        StringBuilder markdown = new StringBuilder();
        String[] lines = text.split("\n");

        String previousLine = "";
        boolean inList = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.isEmpty()) {
                if (!previousLine.isEmpty()) {
                    markdown.append("\n");
                }
                previousLine = "";
                continue;
            }

            // Remove page numbers and footers
            if (line.matches("^\\d+$") || line.matches("^-\\s*\\d+\\s*-$")) {
                continue;
            }

            // Detect potential headings (short lines, often uppercase or followed by blank line)
            if (line.length() < 50 && !line.matches(".*[a-z].*") && !previousLine.isEmpty() && previousLine.length() > line.length()) {
                markdown.append("\n## ").append(line).append("\n");
                previousLine = line;
                continue;
            }

            // Detect list items
            if (line.matches("^[\\d]+[.、]\\s.*") || line.matches("^[\\(（][\\d]+[\\)）]\\s.*")) {
                if (!inList) {
                    markdown.append("\n");
                    inList = true;
                }
                markdown.append("- ").append(line.replaceFirst("^[\\d]+[.、]\\s", "").replaceFirst("^[\\(（][\\d]+[\\)）]\\s", "")).append("\n");
                previousLine = line;
                continue;
            } else {
                inList = false;
            }

            // Regular paragraph text
            markdown.append(line).append(" ");
            previousLine = line;
        }

        return markdown.toString().trim();
    }
}
