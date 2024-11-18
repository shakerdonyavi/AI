package org.example;

public record ReceiptData(String totalAmount, String date) {

    @Override
    public String toString() {
        return "ReceiptData{" +
                "totalAmount='" + totalAmount + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
