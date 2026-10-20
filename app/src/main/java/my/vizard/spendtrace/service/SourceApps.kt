package my.vizard.spendtrace.service

/**
 * Package names of apps whose notifications we parse. These are best-effort and need
 * checking against the apps actually installed on the phone (see the "Unknown apps" log in README).
 */
object SourceApps {
    val packages: Set<String> = setOf(
        "com.maybank2u.life",              // MAE
        "my.com.maybank2u.m2umobile",      // Maybank2u
        "com.cimbmalaysia",                // CIMB OCTO
        "my.com.rhbgroup.rhbmobilebanking",
        "com.pbebank.pbengage",            // PB engage
        "my.com.bankislam.go",
        "my.com.tngdigital.ewallet",       // Touch 'n Go eWallet
        "my.com.myboost",                  // Boost
        "com.grab.passenger",              // GrabPay lives inside Grab
        "com.shopee.my",                   // ShopeePay lives inside Shopee
    )
}
