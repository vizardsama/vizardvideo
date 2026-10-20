package my.vizard.spendtrace.parser

object Categorizer {
    const val OTHERS = "Lain-lain"

    val categories = listOf(
        "Makan & Minum", "Transport", "Bil & Utiliti", "Runcit", "Shopping",
        "Hiburan", "Kesihatan", "Pindahan", OTHERS,
    )

    private val rules = listOf(
        "Makan & Minum" to listOf("mcdonald", "kfc", "starbucks", "grabfood", "foodpanda", "shopeefood", "restoran", "restaurant", "cafe", "kopitiam", "mamak", "nasi", "zus", "tealive", "texas chicken", "pizza"),
        "Transport" to listOf("grab", "petronas", "shell", "petron", "caltex", "bhp", "touch 'n go", "tng", "rapidkl", "mrt", "lrt", "parking", "toll", "setel", "mybas"),
        "Bil & Utiliti" to listOf("tnb", "tenaga", "syabas", "air selangor", "indah water", "unifi", "maxis", "celcom", "digi", "u mobile", "umobile", "astro", "time dotcom", "insurans", "insurance"),
        "Runcit" to listOf("99 speedmart", "99speedmart", "mydin", "aeon", "tesco", "lotus", "giant", "econsave", "jaya grocer", "7-eleven", "7eleven", "kk mart", "family mart", "familymart"),
        "Shopping" to listOf("shopee", "lazada", "zalora", "uniqlo", "mr diy", "mrdiy", "ikea", "tiktok shop", "watsons", "guardian"),
        "Hiburan" to listOf("netflix", "spotify", "disney", "tgv", "gsc", "youtube", "steam", "playstation"),
        "Kesihatan" to listOf("klinik", "clinic", "hospital", "farmasi", "pharmacy", "caring", "dental", "gigi"),
        "Pindahan" to listOf("transfer", "duitnow", "pindahan", "ibg", "tabung", "zakat", "sedekah"),
    )

    fun categorize(merchant: String?, rawText: String): String {
        val haystack = ((merchant ?: "") + " " + rawText).lowercase()
        return rules.firstOrNull { (_, kws) -> kws.any { haystack.contains(it) } }?.first ?: OTHERS
    }
}
