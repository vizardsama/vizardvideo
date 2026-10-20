package my.vizard.spendtrace.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationParserTest {

    @Test fun paymentWithMerchant() {
        val r = NotificationParser.parse("Payment successful", "You paid RM12.50 to KFC Bangsar on 20/10/2026")!!
        assertEquals(1250L, r.amountSen)
        assertEquals(Direction.DEBIT, r.direction)
        assertEquals("KFC Bangsar", r.merchant)
    }

    @Test fun thousandsSeparator() {
        val r = NotificationParser.parse(null, "Transfer to Ali RM1,234.5 berjaya")!!
        assertEquals(123450L, r.amountSen)
    }

    @Test fun malayCredit() {
        val r = NotificationParser.parse("MAE", "Anda telah diterima RM50.00 daripada Siti")!!
        assertEquals(Direction.CREDIT, r.direction)
        assertEquals(5000L, r.amountSen)
    }

    @Test fun purchaseAt() {
        val r = NotificationParser.parse(null, "Purchase at 99 Speedmart RM8.90 using card ending 1234")!!
        assertEquals("99 Speedmart", r.merchant)
    }

    @Test fun ignoresOtpAndPromo() {
        assertNull(NotificationParser.parse(null, "Your OTP for payment of RM100.00 is 123456"))
        assertNull(NotificationParser.parse("Promo", "Save up to RM20 on your next order"))
    }

    @Test fun ignoresNoAmount() {
        assertNull(NotificationParser.parse("Hello", "You paid something"))
    }

    @Test fun categorizer() {
        assertEquals("Makan & Minum", Categorizer.categorize("KFC Bangsar", ""))
        assertEquals(Categorizer.OTHERS, Categorizer.categorize("Unknown Co", "x"))
    }
}
