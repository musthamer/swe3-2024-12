package hbv.service;

import java.nio.file.Path;

public class BookingDocumentResult {
    private final Path qrPath;
    private final Path pdfPath;

    public BookingDocumentResult(Path qrPath, Path pdfPath) {
        this.qrPath = qrPath;
        this.pdfPath = pdfPath;
    }

    public Path getQrPath() {
        return qrPath;
    }

    public Path getPdfPath() {
        return pdfPath;
    }
}

