package com.example.data.model

import com.example.viewmodel.ContentRegion

data class ProductionCompanyInfo(
    val id: Int,
    val name: String,
    val arabicName: String = "",
    val description: String = "",
    val logoPath: String? = null,
    val originCountry: String = "",
    val regions: List<ContentRegion> = listOf(ContentRegion.GLOBAL)
) {
    val fullLogoUrl: String? get() = logoPath?.let {
        if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w500$it"
    }
}

object CompaniesCatalog {

    fun getCompaniesForRegion(region: ContentRegion): List<ProductionCompanyInfo> {
        val arabicCountries = setOf("EG", "SA", "AE", "SY", "LB", "MA", "TN", "JO", "KW", "QA", "OM", "BH", "IQ", "LY", "SD", "DZ")
        val asianCountries = setOf("JP", "KR", "CN", "HK", "TW", "IN", "TH", "PH", "SG", "ID")
        val euroLatinCountries = setOf("GB", "FR", "ES", "IT", "DE", "MX", "BR", "AR", "CO", "CL", "TR")

        return when (region) {
            ContentRegion.ARABIC -> allCompanies.filter { 
                it.originCountry.uppercase() in arabicCountries || it.regions.contains(ContentRegion.ARABIC)
            }
            ContentRegion.HOLLYWOOD -> allCompanies.filter { 
                it.originCountry.uppercase() == "US" || it.regions.contains(ContentRegion.HOLLYWOOD)
            }
            ContentRegion.ASIAN -> allCompanies.filter { 
                it.originCountry.uppercase() in asianCountries || it.regions.contains(ContentRegion.ASIAN)
            }
            ContentRegion.EURO_LATIN -> allCompanies.filter { 
                it.originCountry.uppercase() in euroLatinCountries || it.regions.contains(ContentRegion.EURO_LATIN)
            }
            ContentRegion.GLOBAL -> allCompanies
        }
    }

    val globalCompanies = listOf(
        ProductionCompanyInfo(
            id = 420,
            name = "Marvel Studios",
            arabicName = "مارفل ستوديوز",
            description = "Marvel Cinematic Universe films and series.",
            logoPath = "/hUzeosd33nzE5MCNsZxCGEKTXaQ.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL, ContentRegion.HOLLYWOOD)
        ),
        ProductionCompanyInfo(
            id = 128064,
            name = "DC Studios",
            arabicName = "دي سي ستوديوز",
            description = "DC superhero blockbusters and universe series.",
            logoPath = "/13A2r0i8j7jNcr0s7hF5L7m7i5k.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL, ContentRegion.HOLLYWOOD)
        ),
        ProductionCompanyInfo(
            id = 33,
            name = "Universal Pictures",
            arabicName = "يونيفرسال بيكشرز",
            description = "Legendary film studio behind Fast & Furious, Jurassic World, and Oppenheimer.",
            logoPath = "/8lvHyhjr8oUKOOy2dKXo3hub4ba.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL, ContentRegion.HOLLYWOOD)
        ),
        ProductionCompanyInfo(
            id = 174,
            name = "Warner Bros. Pictures",
            arabicName = "وارنر براذرز",
            description = "Iconic studio behind Harry Potter, The Dark Knight, and Dune.",
            logoPath = "/zhD3hhtKB5qyv7ZeLbgSCrwNsxF.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL, ContentRegion.HOLLYWOOD)
        ),
        ProductionCompanyInfo(
            id = 2,
            name = "Walt Disney Pictures",
            arabicName = "والت ديزني بيكشرز",
            description = "World renowned family films, animation, and live-action fantasies.",
            logoPath = "/wdrCwmR5YrOR9STOKUcwipaq1NW.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL, ContentRegion.HOLLYWOOD)
        ),
        ProductionCompanyInfo(
            id = 4,
            name = "Paramount Pictures",
            arabicName = "باراماونت بيكتشرز",
            description = "Creators of Mission: Impossible, Transformers, Top Gun, and Yellowstone.",
            logoPath = "/fycMZt242LVjagMByZOLUGbCvv3.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL, ContentRegion.HOLLYWOOD)
        ),
        ProductionCompanyInfo(
            id = 5,
            name = "Columbia Pictures",
            arabicName = "كولومبيا بيكتشرز",
            description = "Sony Pictures studio behind Spider-Man, Jumanji, and Ghostbusters.",
            logoPath = "/71BqEFAF4V3qjjMPCpLuyVFB9A.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL, ContentRegion.HOLLYWOOD)
        ),
        ProductionCompanyInfo(
            id = 178464,
            name = "Netflix",
            arabicName = "نتفليكس",
            description = "Global streaming giant producing originals worldwide.",
            logoPath = "/wwemzKWzjKYJFfCeiB57q3r4Bcm.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL)
        ),
        ProductionCompanyInfo(
            id = 3,
            name = "Pixar Animation Studios",
            arabicName = "بيكسار أنيميشن",
            description = "Masterful animated classics like Toy Story, Coco, and Inside Out.",
            logoPath = "/1T521KgFar5W12ISjC8VqgN45ox.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL, ContentRegion.HOLLYWOOD)
        ),
        ProductionCompanyInfo(
            id = 41077,
            name = "A24",
            arabicName = "إيه ٢٤",
            description = "Award-winning independent film powerhouse behind Everything Everywhere.",
            logoPath = "/1ZXsGaFPGrgUEiGZTv9mhu90Y2Q.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL, ContentRegion.HOLLYWOOD)
        ),
        ProductionCompanyInfo(
            id = 3268,
            name = "HBO",
            arabicName = "إتش بي أو",
            description = "Premium series titan behind Game of Thrones, Succession, and The Last of Us.",
            logoPath = "/tuomPhY2UtuPTqqFnKMVHvSb724.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL)
        ),
        ProductionCompanyInfo(
            id = 25,
            name = "20th Century Studios",
            arabicName = "تونتيث سينشري ستوديوز",
            description = "Avatar, Planet of the Apes, and Alien franchises.",
            logoPath = "/qZCc1lty5FzX329aJ24T1N1L501.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL, ContentRegion.HOLLYWOOD)
        ),
        ProductionCompanyInfo(
            id = 1,
            name = "Lucasfilm Ltd.",
            arabicName = "لوكاس فيلم",
            description = "Star Wars and Indiana Jones cinematic epics.",
            logoPath = "/o86DbpburjxrqAzEDhXZcyE8pDb.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL, ContentRegion.HOLLYWOOD)
        ),
        ProductionCompanyInfo(
            id = 521,
            name = "DreamWorks Animation",
            arabicName = "دريم ووركس أنيميشن",
            description = "Shrek, How to Train Your Dragon, and Kung Fu Panda.",
            logoPath = "/kP796YKaGbtcuZuRBCZmMZ37z5g.png",
            originCountry = "US",
            regions = listOf(ContentRegion.GLOBAL, ContentRegion.HOLLYWOOD)
        )
    )

    val hollywoodCompanies = globalCompanies

    val arabicCompanies = listOf(
        ProductionCompanyInfo(
            id = 120935,
            name = "Shahid (MBC Group)",
            arabicName = "شاهد - مجموعة إم بي سي",
            description = "The premier Arabic digital streaming and drama production network.",
            logoPath = "/wwemzKWzjKYJFfCeiB57q3r4Bcm.png",
            originCountry = "SA",
            regions = listOf(ContentRegion.ARABIC)
        ),
        ProductionCompanyInfo(
            id = 143890,
            name = "Synergy Productions",
            arabicName = "سينرجي للإنتاج الفني",
            description = "Leading Egyptian cinema and television production powerhouse.",
            logoPath = null,
            originCountry = "EG",
            regions = listOf(ContentRegion.ARABIC)
        ),
        ProductionCompanyInfo(
            id = 101833,
            name = "El Sobky Film",
            arabicName = "السبكي للإنتاج السينمائي",
            description = "Renowned Egyptian cinema production company behind biggest box-office hits.",
            logoPath = null,
            originCountry = "EG",
            regions = listOf(ContentRegion.ARABIC)
        ),
        ProductionCompanyInfo(
            id = 153022,
            name = "United Media Services (UMS)",
            arabicName = "الشركة المتحدة للخدمات الإعلامية",
            description = "Major Egyptian television and cinema conglomerate.",
            logoPath = null,
            originCountry = "EG",
            regions = listOf(ContentRegion.ARABIC)
        ),
        ProductionCompanyInfo(
            id = 117765,
            name = "Al Adl Group",
            arabicName = "العدل جروب",
            description = "Top Egyptian prestige drama and classic series producers.",
            logoPath = null,
            originCountry = "EG",
            regions = listOf(ContentRegion.ARABIC)
        ),
        ProductionCompanyInfo(
            id = 122485,
            name = "Eagle Films",
            arabicName = "إيجل فيلمز",
            description = "Leading Pan-Arab and Lebanese series and film production company.",
            logoPath = null,
            originCountry = "LB",
            regions = listOf(ContentRegion.ARABIC)
        ),
        ProductionCompanyInfo(
            id = 145902,
            name = "Cedars Art Production (Sabbah Brothers)",
            arabicName = "سيدرز آرت برودكشن (الصباح إخوان)",
            description = "Al Hayba, prestige drama and premium Arabic productions.",
            logoPath = null,
            originCountry = "LB",
            regions = listOf(ContentRegion.ARABIC)
        ),
        ProductionCompanyInfo(
            id = 111166,
            name = "Image Nation Abu Dhabi",
            arabicName = "إيمج نيشن أبوظبي",
            description = "Award-winning Emirati international and regional film production studio.",
            logoPath = null,
            originCountry = "AE",
            regions = listOf(ContentRegion.ARABIC)
        ),
        ProductionCompanyInfo(
            id = 178783,
            name = "Watch It",
            arabicName = "واتش إت",
            description = "Egyptian digital original series and cinema streaming platform.",
            logoPath = null,
            originCountry = "EG",
            regions = listOf(ContentRegion.ARABIC)
        ),
        ProductionCompanyInfo(
            id = 181363,
            name = "Muvi Cinemas & Studios",
            arabicName = "استوديوهات موفي",
            description = "Pioneering Saudi film financing and production company.",
            logoPath = null,
            originCountry = "SA",
            regions = listOf(ContentRegion.ARABIC)
        )
    )

    val asianCompanies = listOf(
        ProductionCompanyInfo(
            id = 10342,
            name = "Studio Ghibli",
            arabicName = "استوديو جيبلي",
            description = "Hayao Miyazaki's legendary Japanese animation studio (Spirited Away).",
            logoPath = "/lY2Z3bL8j3K7l3G7h3F3p2T5v.png",
            originCountry = "JP",
            regions = listOf(ContentRegion.ASIAN)
        ),
        ProductionCompanyInfo(
            id = 5822,
            name = "Toei Animation",
            arabicName = "توي أنيميشن",
            description = "Anime titans behind Dragon Ball, One Piece, and Sailor Moon.",
            logoPath = "/qyXvNf2vWq5n8Z2vQx8jN5vK3qL.png",
            originCountry = "JP",
            regions = listOf(ContentRegion.ASIAN)
        ),
        ProductionCompanyInfo(
            id = 1614,
            name = "Studio Pierrot",
            arabicName = "استوديو بيرو",
            description = "Creators of Naruto, Bleach, and Tokyo Ghoul.",
            logoPath = null,
            originCountry = "JP",
            regions = listOf(ContentRegion.ASIAN)
        ),
        ProductionCompanyInfo(
            id = 2883,
            name = "MAPPA",
            arabicName = "استوديو مابا",
            description = "Jujutsu Kaisen, Attack on Titan Final Season, Chainsaw Man.",
            logoPath = null,
            originCountry = "JP",
            regions = listOf(ContentRegion.ASIAN)
        ),
        ProductionCompanyInfo(
            id = 7030,
            name = "CJ Entertainment",
            arabicName = "سي جيه إنترتينمنت",
            description = "South Korea's premier studio behind Parasite, Snowpiercer, and K-Dramas.",
            logoPath = "/v0T3R6Lp3y1M0r5N2k3w8X.png",
            originCountry = "KR",
            regions = listOf(ContentRegion.ASIAN)
        ),
        ProductionCompanyInfo(
            id = 127928,
            name = "Studio Dragon",
            arabicName = "استوديو دراجون",
            description = "Top South Korean drama production powerhouse (Crash Landing on You, Vincenzo).",
            logoPath = null,
            originCountry = "KR",
            regions = listOf(ContentRegion.ASIAN)
        ),
        ProductionCompanyInfo(
            id = 882,
            name = "Toho Company",
            arabicName = "توهو",
            description = "Godzilla, Your Name, and Akira Kurosawa classics.",
            logoPath = "/fycMZt242LVjagMByZOLUGbCvv3.png",
            originCountry = "JP",
            regions = listOf(ContentRegion.ASIAN)
        )
    )

    val euroLatinCompanies = listOf(
        ProductionCompanyInfo(
            id = 10163,
            name = "Working Title Films",
            arabicName = "وركينج تايتل فيلمز",
            description = "Top British film production company (Notting Hill, Darkest Hour).",
            logoPath = null,
            originCountry = "GB",
            regions = listOf(ContentRegion.EURO_LATIN)
        ),
        ProductionCompanyInfo(
            id = 9,
            name = "Gaumont",
            arabicName = "جومون",
            description = "Oldest French film company in the world (The Fifth Element, Intouchables, Lupin).",
            logoPath = null,
            originCountry = "FR",
            regions = listOf(ContentRegion.EURO_LATIN)
        ),
        ProductionCompanyInfo(
            id = 104,
            name = "Canal+",
            arabicName = "كنال بلوس",
            description = "Major European television and cinema production group.",
            logoPath = null,
            originCountry = "FR",
            regions = listOf(ContentRegion.EURO_LATIN)
        ),
        ProductionCompanyInfo(
            id = 83,
            name = "StudioCanal",
            arabicName = "استوديو كانال",
            description = "Paddington, Terminator 2, and European international co-productions.",
            logoPath = null,
            originCountry = "FR",
            regions = listOf(ContentRegion.EURO_LATIN)
        ),
        ProductionCompanyInfo(
            id = 6125,
            name = "BBC Film",
            arabicName = "بي بي سي فيلمز",
            description = "British prestige drama and documentary cinema.",
            logoPath = null,
            originCountry = "GB",
            regions = listOf(ContentRegion.EURO_LATIN)
        ),
        ProductionCompanyInfo(
            id = 8806,
            name = "Televisa / Univision",
            arabicName = "تيليفيزا",
            description = "Latin America's biggest drama and telenovela production empire.",
            logoPath = null,
            originCountry = "MX",
            regions = listOf(ContentRegion.EURO_LATIN)
        ),
        ProductionCompanyInfo(
            id = 1007,
            name = "Atresmedia Cine",
            arabicName = "أتريسميديا ثيني",
            description = "Spanish powerhouse behind Money Heist (La Casa de Papel) and Spanish hits.",
            logoPath = null,
            originCountry = "ES",
            regions = listOf(ContentRegion.EURO_LATIN)
        )
    )

    val allCompanies: List<ProductionCompanyInfo> =
        (globalCompanies + arabicCompanies + asianCompanies + euroLatinCompanies).distinctBy { it.id }
}
