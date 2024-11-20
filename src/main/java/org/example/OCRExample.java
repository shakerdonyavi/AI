package org.example;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.BORDER_CONSTANT;

public class OCRExample {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        String imagePath = "src/main/resources/sample2.jpg";
        rotateImage(imagePath);
//        resizeImage();
        cropImage();
        // Step 1: Preprocess the image
//        Mat image = Imgcodecs.imread(imagePath);
//        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.GaussianBlur(image, image, new Size(3, 3), 0);
//        Imgproc.resize(image, image, new Size(image.width() * 2, image.height() * 2)); // Increase image size

        // Additional Morphological operation
//        Imgproc.dilate(image, image, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));

//        Imgproc.threshold(image, image, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
//        Imgcodecs.imwrite("sample2_processed.jpg", image);

        // Step 2: Run OCR
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("E:\\tesdata\\tessdata-main"); // Path to Tesseract language files
        tesseract.setLanguage("fas"); // Set language to Persian
        tesseract.setOcrEngineMode(1); // Use LSTM OCR engine for better accuracy
        tesseract.setPageSegMode(6); // Automatic page segmentation

        // Add whitelist to recognize Persian numbers only
//        tesseract.setTessVariable("tessedit_char_whitelist", "۰۱۲۳۴۵۶۷۸۹");
//        tesseract.setTessVariable("user_defined_dpi", "300");
        try {
            String text = tesseract.doOCR(new java.io.File("cropped_image.jpg"));

            // Step 3: Extract data to JSON
            JSONObject jsonObject = extractDataToJson(text);
            System.out.println(jsonObject.toString(2));

        } catch (TesseractException e) {
            System.err.println("Error in OCR: " + e.getMessage());
        }
    }

    private static void resizeImage() {
        // Input image path
        String inputPath = "src/main/resources/rotated.jpg";  // Replace with your input image path
        // Output image path
        String outputPath = "src/main/resources/resized.jpg";  // Replace with your output image path

        // Load the reference image (to get the target size)
        String referencePath = "src/main/resources/sample2.jpg"; // Replace with your reference image path
        Mat referenceImage = Imgcodecs.imread(referencePath);
        if (referenceImage.empty()) {
            System.out.println("Could not load the reference image.");
            return;
        }

        // Get the dimensions of the reference image
        int targetWidth = referenceImage.cols();
        int targetHeight = referenceImage.rows();

        // Load the input image
        Mat inputImage = Imgcodecs.imread(inputPath);
        if (inputImage.empty()) {
            System.out.println("Could not load the input image.");
            return;
        }

        // Create a new Mat for the resized image
        Mat resizedImage = new Mat();

        // Resize the input image to the reference size
        Imgproc.resize(inputImage, resizedImage, new Size(targetWidth, targetHeight));

        // Save the resized image
        Imgcodecs.imwrite(outputPath, resizedImage);
        System.out.println("Resized image saved at: " + outputPath);
    }


    private static void rotateImage(String imagePath) {
        String outputPath = "src/main/resources/rotated.jpg";

        // Load the image
        Mat src = Imgcodecs.imread(imagePath, Imgcodecs.IMREAD_COLOR);
        if (src.empty()) {
            System.out.println("Could not load the image.");
            return;
        }

        // Convert the image to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply threshold to get binary image
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find the largest contour
        double maxArea = -1;
        MatOfPoint largestContour = null;
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                largestContour = contour;
            }
        }

        if (largestContour == null) {
            System.out.println("Could not find any contours.");
            return;
        }

// Get the minimum bounding box for the largest contour
        MatOfPoint2f contour2f = new MatOfPoint2f(largestContour.toArray());
        RotatedRect rotatedRect = Imgproc.minAreaRect(contour2f);

// Calculate the correct rotation angle
        double angle = rotatedRect.angle;
        if (rotatedRect.size.width < rotatedRect.size.height) {
            angle = 90 + angle; // Adjust for vertical orientation
        } else if (angle < -45) {
            angle += 90; // Handle rotated bounding boxes
        }
        System.out.println("Final rotation angle: " + angle);

// Rotate the image to deskew
        Point center = new Point(src.width() / 2.0, src.height() / 2.0);
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        Mat deskewed = new Mat();
        Imgproc.warpAffine(src, deskewed, rotationMatrix, src.size(), Imgproc.INTER_CUBIC);

// Save the deskewed image
        Imgcodecs.imwrite(outputPath, deskewed);
        System.out.println("Deskewed image saved at: " + outputPath);

    }


    private static void cropImage() {
        String imagePath = "src/main/resources/rotated.jpg";
        // Load the image
        Mat img = Imgcodecs.imread(imagePath);

        // Define the region of interest (ROI) for "شماره مرجع"
        // Adjust the values (x, y, width, height) as necessary
        int x = 180; // starting x-coordinate of the cropping area
        int y = 235; // starting y-coordinate of the cropping area
        int width = 205; // width of the cropping area
        int height = 40; // height of the cropping area

        // Create a rectangle for the ROI
        Rect roi = new Rect(x, y, width, height);

        // Crop the image
        Mat croppedImg = new Mat(img, roi);

        // Save the cropped image
        Imgcodecs.imwrite("cropped_image.jpg", croppedImg);
    }

    private static JSONObject extractDataToJson(String text) {
        // Implement your logic to parse text and structure it into JSON format
        JSONObject jsonObject = new JSONObject();
        // Example parsing (customize based on your requirements)
        jsonObject.put("text", text);
        return jsonObject;
    }
}
