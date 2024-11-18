package org.example;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.json.JSONObject;

public class OCRExample {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        String imagePath = "src/main/resources/test3.png";

        // Step 1: Preprocess the image
        Mat image = Imgcodecs.imread(imagePath);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(image, image, new Size(3, 3), 0);
        Imgproc.resize(image, image, new Size(image.width() * 2, image.height() * 2)); // Increase image size

        // Additional Morphological operation
        Imgproc.dilate(image, image, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));

        Imgproc.threshold(image, image, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Imgcodecs.imwrite("sample2_processed.jpg", image);

        // Step 2: Run OCR
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("E:\\tesdata\\tessdata-main"); // Path to Tesseract language files
        tesseract.setLanguage("fas+ara"); // Set language to Persian
        tesseract.setOcrEngineMode(1); // Use LSTM OCR engine for better accuracy
        tesseract.setPageSegMode(6); // Automatic page segmentation

        // Add whitelist to recognize Persian numbers only
        tesseract.setTessVariable("tessedit_char_whitelist", "۰۱۲۳۴۵۶۷۸۹");
        tesseract.setTessVariable("user_defined_dpi", "300");
        try {
            String text = tesseract.doOCR(new java.io.File("sample2_processed.jpg"));

            // Step 3: Extract data to JSON
            JSONObject jsonObject = extractDataToJson(text);
            System.out.println(jsonObject.toString(2));

        } catch (TesseractException e) {
            System.err.println("Error in OCR: " + e.getMessage());
        }
    }

    private static JSONObject extractDataToJson(String text) {
        // Implement your logic to parse text and structure it into JSON format
        JSONObject jsonObject = new JSONObject();
        // Example parsing (customize based on your requirements)
        jsonObject.put("text", text);
        return jsonObject;
    }
}
