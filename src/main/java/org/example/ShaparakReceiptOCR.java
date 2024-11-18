package org.example;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.json.JSONObject;

public class ShaparakReceiptOCR {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        String imagePath = "src/main/resources/sample2.jpg";

        // Load and preprocess the full image
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            System.err.println("Failed to load image!");
            return;
        }

        // Print image dimensions for debugging
        System.out.println("Image dimensions: " + image.cols() + " x " + image.rows());

        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);

        // Detect and extract specific regions like amount, card number, etc.
        String amountText = extractAmount(image);
        String cardNumberText = extractCardNumber(image);
        String referenceCodeText = extractReferenceCode(image);

        // Create a JSON object to hold extracted data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", amountText);
        jsonObject.put("card_number", cardNumberText);
        jsonObject.put("reference_code", referenceCodeText);

        System.out.println(jsonObject.toString(2));
    }

    private static String extractAmount(Mat image) {
        // Define the area containing the amount text (adjusted for 900x393 image)
        Rect amountRegion = new Rect(100, 150, 300, 50); // Set coordinates based on receipt layout
        if (!isRegionValid(image, amountRegion)) return "Invalid region";

        Mat amountImage = new Mat(image, amountRegion);

        // Apply preprocessing
        Imgproc.resize(amountImage, amountImage, new Size(amountImage.width() * 2, amountImage.height() * 2));
        Imgproc.threshold(amountImage, amountImage, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        // Run OCR on amount
        return runOCR(amountImage);
    }

    private static String extractCardNumber(Mat image) {
        // Define the area containing the card number text (adjusted for 900x393 image)
        Rect cardNumberRegion = new Rect(50, 220, 400, 50); // Adjust coordinates as needed
        if (!isRegionValid(image, cardNumberRegion)) return "Invalid region";

        Mat cardNumberImage = new Mat(image, cardNumberRegion);

        // Preprocessing
        Imgproc.resize(cardNumberImage, cardNumberImage, new Size(cardNumberImage.width() * 2, cardNumberImage.height() * 2));
        Imgproc.threshold(cardNumberImage, cardNumberImage, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        return runOCR(cardNumberImage);
    }

    private static String extractReferenceCode(Mat image) {
        // Define the area containing the reference code text (adjusted for 900x393 image)
        Rect referenceCodeRegion = new Rect(50, 300, 400, 50); // Adjust coordinates as needed
        if (!isRegionValid(image, referenceCodeRegion)) return "Invalid region";

        Mat referenceCodeImage = new Mat(image, referenceCodeRegion);

        // Preprocessing
        Imgproc.resize(referenceCodeImage, referenceCodeImage, new Size(referenceCodeImage.width() * 2, referenceCodeImage.height() * 2));
        Imgproc.threshold(referenceCodeImage, referenceCodeImage, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        return runOCR(referenceCodeImage);
    }

    private static boolean isRegionValid(Mat image, Rect region) {
        // Ensure that the region is within the image bounds
        return region.x >= 0 && region.y >= 0 && region.x + region.width <= image.cols() && region.y + region.height <= image.rows();
    }

    private static String runOCR(Mat image) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("E:\\tesdata\\tessdata-main"); // Path to Tesseract language files
        tesseract.setLanguage("fas+ara"); // Set language to Persian and Arabic

        // Save the Mat image to file for Tesseract OCR
        Imgcodecs.imwrite("temp.jpg", image);

        try {
            return tesseract.doOCR(new java.io.File("temp.jpg"));
        } catch (TesseractException e) {
            System.err.println("Error in OCR: " + e.getMessage());
            return "";
        }
    }
}
