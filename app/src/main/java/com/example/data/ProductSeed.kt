package com.example.data

object ProductSeed {
    
    val initialProducts = listOf(
        // MEN
        ProductEntity(
            name = "Premium Heavyweight Oversized Tee",
            brand = "Pooshak Studio",
            description = "Crafted from tight-knit 280 GSM premium cotton, this item offers an elegant drape, a beefy feel, and structured shoulders. Perfect for contemporary high-street stylings.",
            fabric = "100% Long-Staple Organic Cotton",
            price = 1999.00,
            discountPrice = 1299.00,
            rating = 4.7f,
            reviewsCount = 124,
            isPremium = true,
            isBestseller = true,
            isTrending = true,
            gender = "Men",
            category = "T-Shirts",
            imageUrlsStr = "men_tshirt_black,men_tshirt_white,men_tshirt_olive",
            videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-fashion-man-wearing-casual-oversized-black-t-shirt-41982-large.mp4"
        ),
        ProductEntity(
            name = "Structured Oxford Cotton Shirt",
            brand = "Zara",
            description = "A foundational menswear piece styled in a modern streamlined fit. Features a refined soft button-down collar, meticulous tone-on-tone single-needle tailoring, and organic chalk buttons.",
            fabric = "100% Pure Giza Oxford Cotton",
            price = 2499.00,
            discountPrice = 1799.00,
            rating = 4.5f,
            reviewsCount = 89,
            isPremium = false,
            isBestseller = true,
            isTrending = false,
            gender = "Men",
            category = "Shirts",
            imageUrlsStr = "men_shirt_blue,men_shirt_white",
        ),
        ProductEntity(
            name = "Heavy French Terry Hoodie",
            brand = "H&M Premium",
            description = "Engineered with loopback French Terry fabric (420 GSM) for a substantial, cozy feel. Designed in a boxy slouchy fit with clean seamless side pockets and double-lined hood.",
            fabric = "85% Cotton, 15% recycled Fleece",
            price = 3999.00,
            discountPrice = 2499.00,
            rating = 4.8f,
            reviewsCount = 205,
            isPremium = true,
            isBestseller = false,
            isTrending = true,
            gender = "Men",
            category = "Hoodies",
            imageUrlsStr = "men_hoodie_charcoal,men_hoodie_cream",
        ),
        ProductEntity(
            name = "Sleek Water-Resistant Coach Jacket",
            brand = "AJIO Luxe",
            description = "Minimalist lightweight windbreaker jacket with premium matte metallic snap buttons, elasticized cuffs, and adjustable hem drawcords. Versatile underlayer for light drizzle.",
            fabric = "Water repellant Nylon Taslan shell with satin liner",
            price = 4500.00,
            discountPrice = 3299.00,
            rating = 4.4f,
            reviewsCount = 42,
            isPremium = true,
            isBestseller = false,
            isTrending = false,
            gender = "Men",
            category = "Jackets",
            imageUrlsStr = "men_jacket_black,men_jacket_navy",
        ),
        ProductEntity(
            name = "Selvedge Slim Fit Denim Jeans",
            brand = "Myntra Premium",
            description = "Classic dark indigo slim-cut jeans. Standard 13oz Japanese selvedge denim woven meticulously on vintage looms. Develops incredible personalized fades over time.",
            fabric = "99% Cotton, 1% Comfort Elastane",
            price = 3499.00,
            discountPrice = 2299.00,
            rating = 4.6f,
            reviewsCount = 156,
            isBudget = false,
            isTrending = true,
            gender = "Men",
            category = "Jeans",
            imageUrlsStr = "men_jeans_dark,men_jeans_mid",
        ),
        ProductEntity(
            name = "Tailored Cropped Pleated Pants",
            brand = "Zara",
            description = "Infuse a sartorial edge into your daily outfits. These pants feature sharp center creases, relaxed thighs tapering down gracefully to slightly cropped ankles, and double belt loops.",
            fabric = "Rayon-Viscose poly fabric blend for clean fall",
            price = 2999.00,
            discountPrice = 1999.00,
            rating = 4.3f,
            reviewsCount = 61,
            isPremium = false,
            isBestseller = false,
            isTrending = true,
            gender = "Men",
            category = "Pants",
            imageUrlsStr = "men_pants_beige,men_pants_black",
        ),

        // WOMEN
        ProductEntity(
            name = "Minimalist Ribbed Knit Midi Dress",
            brand = "Zara Studio",
            description = "An ultra-chic form-fitting midi dress featuring an asymmetrical neckline and cozy vertical ribbed textures. Extremely elegant for transition from office to high-end lounges.",
            fabric = "76% Fine EcoVero Viscose, 24% Recycled Nylon",
            price = 3499.00,
            discountPrice = 2499.00,
            rating = 4.8f,
            reviewsCount = 143,
            isPremium = true,
            isBestseller = true,
            isTrending = true,
            gender = "Women",
            category = "Dresses",
            imageUrlsStr = "women_dress_rust,women_dress_black",
        ),
        ProductEntity(
            name = "Elegant Handcrafted Chikankari Kurti",
            brand = "Pooshak Classics",
            description = "Intricate floral hand embroidery across the front neckline, detailed with fine shadow-work. Breathable material keep you cool and graceful through tropical summer afternoons.",
            fabric = "Premium Georgette with super soft cotton slip",
            price = 1999.00,
            discountPrice = 1199.00,
            rating = 4.6f,
            reviewsCount = 312,
            isBudget = true,
            isBestseller = true,
            gender = "Women",
            category = "Kurtis",
            imageUrlsStr = "women_kurti_white,women_kurti_rose",
        ),
        ProductEntity(
            name = "Pure Organza Silk Saree with Zari",
            brand = "AJIO Luxe",
            description = "An archival silk saree of gossamer thin handwoven organza silk. Embellished with fine metallic gold threads woven beautifully on borders. Includes matching unstitched tissue blouse piece.",
            fabric = "100% Handloom Organza Mulberry Silk",
            price = 8999.00,
            discountPrice = 5999.00,
            rating = 4.9f,
            reviewsCount = 32,
            isPremium = true,
            isTrending = true,
            gender = "Women",
            category = "Sarees",
            imageUrlsStr = "women_saree_peach,women_saree_lilac",
        ),
        ProductEntity(
            name = "Flowy Balloon Sleeve Top",
            brand = "H&M",
            description = "Liven up your style with this airy top featuring gathered dynamic balloon sleeves, simple tie details at the collar, and subtle floral leaf self-jacquard weaves.",
            fabric = "Lightweight semi-crisp Cotton Tencel blend",
            price = 1499.00,
            discountPrice = 999.00,
            rating = 4.3f,
            reviewsCount = 188,
            isBudget = true,
            isTrending = true,
            gender = "Women",
            category = "Tops",
            imageUrlsStr = "women_top_cream,women_top_lilac",
        ),

        // KIDS
        ProductEntity(
            name = "Cotton Denim Dungaree Set",
            brand = "Zara Kids",
            description = "Adorable kids matching set detailing lightweight, ultra-comfy blue denim dungarees paired with a soft striped crew neck knit inner jersey tee.",
            fabric = "100% Fine Baby-Safe Spun Cotton",
            price = 1899.00,
            discountPrice = 1299.00,
            rating = 4.5f,
            reviewsCount = 55,
            gender = "Kids",
            category = "Boys clothing",
            imageUrlsStr = "kids_boy_denim",
        ),
        ProductEntity(
            name = "Tiered Pastel Tulle Party Dress",
            brand = "H&M Kids",
            description = "Make her look magical. This dress features fluffy tiered fine tulle layers in pastel gradient colors, lined in organic cotton voile for extreme skin-comfort and friction prevention.",
            fabric = "Fine premium tulle with 100% combed Cotton lining",
            price = 2299.00,
            discountPrice = 1499.00,
            rating = 4.6f,
            reviewsCount = 74,
            gender = "Kids",
            category = "Girls clothing",
            imageUrlsStr = "kids_girl_pink",
        ),
        ProductEntity(
            name = "Cozy Kids Light Up Sneakers",
            brand = "Myntra Kids",
            description = "Flexible and highly cushioned everyday kids sneakers with fun multi-color LEDs built into the soft, durable rubber outsoles. Equipped with velcro straps for rapid closure.",
            fabric = "Breathable mesh knit upper with premium EVA footbed",
            price = 1799.00,
            discountPrice = 1199.00,
            rating = 4.4f,
            reviewsCount = 92,
            isBudget = true,
            gender = "Kids",
            category = "Shoes",
            imageUrlsStr = "kids_shoes_blue",
        ),

        // ACCESSORIES
        ProductEntity(
            name = "Titanium Chronograph Slate Watch",
            brand = "Pooshak Accents",
            description = "An elegant 41mm matte grey pure titanium casing watch, housing a precise Swiss quartz core, sapphire scratch-resistant crystal face, and deep brown genuine calfskin strap.",
            fabric = "Pure Titanium class-4 casing, Swiss movement",
            price = 9999.00,
            discountPrice = 6999.00,
            rating = 4.8f,
            reviewsCount = 41,
            isPremium = true,
            isTrending = true,
            gender = "Accessories",
            category = "Watches",
            imageUrlsStr = "acc_watch_titanium",
        ),
        ProductEntity(
            name = "Handmade Premium Saffiano Leather Wallet",
            brand = "AJIO Luxe",
            description = "Sleek bifold pocket organizer handcrafted from scratch-resistant Italian Saffiano cross-hatch leather. Houses 8 separate credit card slots and deep dual money divider compartments.",
            fabric = "105% Full-Grain Vegetable Tanned Leather",
            price = 2499.00,
            discountPrice = 1499.00,
            rating = 4.7f,
            reviewsCount = 110,
            isBestseller = true,
            gender = "Accessories",
            category = "Wallets",
            imageUrlsStr = "acc_wallet_brown",
        ),
        ProductEntity(
            name = "Vintage Tortoiseshell Acetate Sunglasses",
            brand = "Zara Accents",
            description = "Revive the retro aesthetic with polished acetate tortoiseshell thick frames, durable 5-barrel hinge hinges, and fully protective Category 3 UV400 grey polarized lenses.",
            fabric = "Polished Acetate frame with Polarized lenses",
            price = 1890.00,
            discountPrice = 1290.00,
            rating = 4.5f,
            reviewsCount = 82,
            gender = "Accessories",
            category = "Sunglasses",
            imageUrlsStr = "acc_sunglass_brown",
        )
    )

    suspend fun seedDatabase(db: AppDatabase) {
        val existingProducts = db.productDao.getAllProducts()
        if (existingProducts.isNotEmpty()) {
            return
        }

        // Insert products and fetch their auto-generated IDs to create proper variants
        db.productDao.insertProducts(initialProducts)
        val inserted = db.productDao.getAllProducts()

        val sampleSizes = listOf("S", "M", "L", "XL")
        val sampleAccessorySizes = listOf("One Size")
        
        val colorsMap = mapOf(
            "T-Shirts" to listOf("Midnight Black", "Pearl White", "Olive Green"),
            "Shirts" to listOf("Classic Blue", "Crisp White"),
            "Hoodies" to listOf("Charcoal Grey", "Cream White"),
            "Jackets" to listOf("Ink Black", "Deep Navy"),
            "Jeans" to listOf("Indigo", "Vintage Blue"),
            "Pants" to listOf("Earthy Sand", "Stealth Black"),
            "Dresses" to listOf("Rust Red", "Obsidian Black"),
            "Kurtis" to listOf("Ivory White", "Rose Pink"),
            "Sarees" to listOf("Peach Gold", "Soft Lilac"),
            "Tops" to listOf("Vanilla Cream", "Lavender"),
            "Boys clothing" to listOf("Chambray Blue"),
            "Girls clothing" to listOf("Rosy Pink"),
            "Shoes" to listOf("Carbon Blue", "Cherry Pink"),
            "Watches" to listOf("Sienna Brown"),
            "Wallets" to listOf("Saddle Brown"),
            "Sunglasses" to listOf("Tortoiseshell Vintage")
        )

        val variantsToInsert = mutableListOf<ProductVariantEntity>()

        for (product in inserted) {
            val colors = colorsMap[product.category] ?: listOf("Default Color")
            val sizes = if (product.gender == "Accessories" || product.category == "Sarees") {
                sampleAccessorySizes
            } else {
                sampleSizes
            }

            for (color in colors) {
                for (size in sizes) {
                    // Generate a high-quality variance in stock numbers
                    val baseStock = when {
                        color.contains("Black") || color.contains("Ivory") -> 12
                        color.contains("White") || size == "M" -> 10
                        size == "S" || size == "L" -> 8
                        else -> 4
                    }
                    variantsToInsert.add(
                        ProductVariantEntity(
                            productId = product.id,
                            color = color,
                            size = size,
                            stock = baseStock
                        )
                    )
                }
            }
        }

        db.productDao.insertVariants(variantsToInsert)
        
        // Add a verified buyer review to a couple of items to show on load
        inserted.firstOrNull()?.let { firstProduct ->
            db.reviewDao.insertReview(
                ReviewEntity(
                    productId = firstProduct.id,
                    userEmail = "verified_buyer@example.com",
                    userName = "Rohan Sharma",
                    rating = 5,
                    comment = "Stunning thick material. Feels extremely premium, exactly like Zara Studio collection. Heavy weight is perfect and fits oversized nicely. Worth every rupee!",
                    isVerifiedPurchase = true
                )
            )
            db.reviewDao.insertReview(
                ReviewEntity(
                    productId = firstProduct.id,
                    userEmail = "fashionista@example.com",
                    userName = "Ananya Sen",
                    rating = 4,
                    comment = "Super comfort, drape is heavy and smooth. Olive Green is a gorgeous pastel earthy tone.",
                    isVerifiedPurchase = true
                )
            )
        }
    }
}
