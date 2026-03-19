//package com.makers.memoir.service;
//
//import org.springframework.stereotype.Service;
//import org.xhtmlrenderer.pdf.ITextRenderer;
//import java.io.ByteArrayOutputStream;
//
//@Service
//public class PdfService {
//
//    public byte[] generatePdf(String htmlContent) throws Exception {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        ITextRenderer renderer = new ITextRenderer();
//        renderer.setDocumentFromString(htmlContent);
//        renderer.layout();
//        renderer.createPDF(outputStream);
//        return outputStream.toByteArray();
//    }
//}
package com.makers.memoir.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class PdfService {

    private final RestTemplate restTemplate = new RestTemplate();

    public byte[] generatePdf(String htmlContent) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);
        return outputStream.toByteArray();
    }

    public String convertImageUrlToBase64(String imageUrl) {
        try {
            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = getMimeType(imageUrl);
            return "data:" + mimeType + ";base64," + base64;
        } catch (Exception e) {
            System.err.println("Failed to convert image to base64: " + imageUrl + " - " + e.getMessage());
            return imageUrl; // fall back to URL if conversion fails
        }
    }

    private String getMimeType(String imageUrl) {
        if (imageUrl.contains(".png")) return "image/png";
        if (imageUrl.contains(".jpg") || imageUrl.contains(".jpeg")) return "image/jpeg";
        if (imageUrl.contains(".webp")) return "image/webp";
        return "image/jpeg";
    }
}