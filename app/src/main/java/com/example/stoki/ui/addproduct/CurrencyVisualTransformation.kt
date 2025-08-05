package com.example.stoki.ui.addproduct

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class CurrencyVisualTransformation(private val locale: Locale = Locale("pt", "BR")) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitsOnly = text.text.filter { it.isDigit() }
        val number = digitsOnly.toLongOrNull() ?: 0L

        val formattedText = NumberFormat.getCurrencyInstance(locale).format(number / 100.0)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return formattedText.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return digitsOnly.length
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}