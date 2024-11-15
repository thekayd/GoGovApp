package com.kayodedaniel.gogovmobile.models

data class Payment(
    val id: String,
    val userId: String,
    val amount: Double,
    val cardNumber: String,
    val cardHolder: String,
    val expiryDate: String,
    val cvv: String,
    val paymentStatus: String,
    val paymentDate: String,
    val referenceNumber: String,
    val applicationId: String?,
    val receiptUrl: String?,
    val createdAt: String
)

// Extension function to create receipt content
fun Payment.generateReceiptContent(): String {
    return """
        Payment Receipt
        ==============
        
        Reference Number: $referenceNumber
        Date: $paymentDate
        Amount: R ${String.format("%.2f", amount)}
        
        Payment Details
        --------------
        Status: $paymentStatus
        Card Holder: ${cardHolder.uppercase()}
        Card Number: XXXX-XXXX-XXXX-${cardNumber.takeLast(4)}
        
        This is an automatically generated receipt.
        Please retain this for your records.
        
        Thank you for your payment!
    """.trimIndent()
}