//package com.webkorps.main;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.security.SecureRandom;
//import java.util.Base64;
//
//public class SecretKeyGenerator {
//
//    public static void main(String[] args) {
//        // SecureRandom to generate a random byte array
//        SecureRandom secureRandom = new SecureRandom();
//
//        // Create a byte array for a 512-bit key (64 bytes) - Required for HS512
//        byte[] key = new byte[64];
//        secureRandom.nextBytes(key); // Fill the byte array with random values
//
//        // Encode the byte array into a Base64 string
//        String encodedKey = Base64.getEncoder().encodeToString(key);
//
//        // Output the Base64-encoded key to the console
//        System.out.println("Generated Secret Key: " + encodedKey);
//
//        // Write the secret key to the application.properties file
//        writeSecretKeyToProperties(encodedKey);
//    }
//
//    private static void writeSecretKeyToProperties(String secretKey) {
//        String filePath = "src/main/resources/application.properties"; // Path to the application.properties file
//        File file = new File(filePath);
//
//        // Prepare the string to be written into the properties file
//        String propertyLine = "jwt.secret=" + secretKey + "\n";
//
//        // Write the secret key to the file
//        try (FileWriter writer = new FileWriter(file, true)) {
//            writer.append(propertyLine);
//
//            // Check if the property already exists and replace it
//            replacePropertyIfExist(file, "jwt.secret", secretKey);
//
//            System.out.println("Secret key written to application.properties");
//        } catch (IOException e) {
//            System.out.println("Error writing to application.properties: " + e.getMessage());
//        }
//    }
//
//    private static void replacePropertyIfExist(File file, String propertyName, String newValue) throws IOException {
//        java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
//        boolean replaced = false;
//
//        for (int i = 0; i < lines.size(); i++) {
//            if (lines.get(i).startsWith(propertyName + "=")) {
//                lines.set(i, propertyName + "=" + newValue);
//                replaced = true;
//                break;
//            }
//        }
//
//        if (replaced) {
//            java.nio.file.Files.write(file.toPath(), lines);
//        }
//    }
//}