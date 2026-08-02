package com.example.service

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val regionName: String,
    val flagEmoji: String,
    val sampleGreeting: String
) {
    ENGLISH(
        code = "en",
        displayName = "English",
        regionName = "Standard / Official",
        flagEmoji = "🇱🇷",
        sampleGreeting = "Welcome to AgriCoop Liberia"
    ),
    KOLOQUA(
        code = "lr_koloqua",
        displayName = "Liberian Koloqua",
        regionName = "Vernacular / Kreyol",
        flagEmoji = "🗣️",
        sampleGreeting = "Welcome to AgriCoop, my people!"
    ),
    KPELLE(
        code = "lr_kpelle",
        displayName = "Kpelle Dialect",
        regionName = "Bong & Lofa Counties",
        flagEmoji = "🌾",
        sampleGreeting = "Yaane - Welcome to Agricultural Co-op"
    ),
    BASSA(
        code = "lr_bassa",
        displayName = "Bassa Dialect",
        regionName = "Grand Bassa & Margibi",
        flagEmoji = "🌴",
        sampleGreeting = "Mbiu - Welcome Co-op Members"
    ),
    LORMA(
        code = "lr_lorma",
        displayName = "Lorma Dialect",
        regionName = "Lofa County (Voinjama/Zorzor)",
        flagEmoji = "⛰️",
        sampleGreeting = "Ba-ya - Welcome to Lofa Agribusiness"
    ),
    GBANDI(
        code = "lr_gbandi",
        displayName = "Gbandi Dialect",
        regionName = "Lofa County (Kolahun District)",
        flagEmoji = "🌿",
        sampleGreeting = "Bia-ka - Welcome Lofa Farmers Co-op"
    ),
    MANO(
        code = "lr_mano",
        displayName = "Mano Dialect",
        regionName = "Nimba County (Sanniquellie)",
        flagEmoji = "🌱",
        sampleGreeting = "A-seh - Welcome Nimba Tuber Guild"
    ),
    GIO(
        code = "lr_gio",
        displayName = "Gio (Dan) Dialect",
        regionName = "Nimba County (Ganta & Tappita)",
        flagEmoji = "🍃",
        sampleGreeting = "Na-woh - Welcome Nimba Produce Union"
    )
}

data class LocalizedText(
    val appTitle: String,
    val appSubtitle: String,
    val tabDashboard: String,
    val tabWeigh: String,
    val tabFarmers: String,
    val tabProcessing: String,
    val tabFinancials: String,
    val tabSync: String,
    val weighScaleTitle: String,
    val weighScaleConnected: String,
    val weighScaleDisconnected: String,
    val createBatchButton: String,
    val farmerSearchPlaceholder: String,
    val totalTonnageLabel: String,
    val starchContentLabel: String,
    val mobilePayoutLabel: String,
    val offlineSyncLabel: String,
    val geminiForecastTitle: String,
    val lowBatteryWarning: String,
    val languageSettingsTitle: String
)

object LiberianLanguageLocalizer {

    fun getLocalizedText(language: AppLanguage): LocalizedText {
        return when (language) {
            AppLanguage.ENGLISH -> LocalizedText(
                appTitle = "AgriCoop Liberia",
                appSubtitle = "Lofa & Nimba Agribusiness Venture",
                tabDashboard = "Dashboard",
                tabWeigh = "Weigh Tuber",
                tabFarmers = "Co-op Farmers",
                tabProcessing = "Processing Hubs",
                tabFinancials = "Grants & Payouts",
                tabSync = "Offline Sync",
                weighScaleTitle = "Bluetooth Scale Weighmaster",
                weighScaleConnected = "Scale Connected (Bluetooth IP67)",
                weighScaleDisconnected = "Scale Disconnected",
                createBatchButton = "Record Weighing & Print Receipt",
                farmerSearchPlaceholder = "Search farmer name or ID...",
                totalTonnageLabel = "Total Tonnage Collected",
                starchContentLabel = "Average Starch Content",
                mobilePayoutLabel = "MoMo Digital Payouts",
                offlineSyncLabel = "2G/3G Packet Sync Protocol",
                geminiForecastTitle = "Gemini AI Harvest Volume Predictor",
                lowBatteryWarning = "Low Battery Warning (Solar Backup Active)",
                languageSettingsTitle = "Language & Dialect Accessibility"
            )

            AppLanguage.KOLOQUA -> LocalizedText(
                appTitle = "AgriCoop Liberia",
                appSubtitle = "Lofa & Nimba Farm People Venture",
                tabDashboard = "Main Center",
                tabWeigh = "Weigh Tuber Dem",
                tabFarmers = "Farmer Dem",
                tabProcessing = "Factory Hubs",
                tabFinancials = "Money & Grants",
                tabSync = "Send Data SMS",
                weighScaleTitle = "Bluetooth Scale Weighing Place",
                weighScaleConnected = "Scale Connected Fine Fine",
                weighScaleDisconnected = "Scale Off / No Connection",
                createBatchButton = "Save Tuber Weight & Print Paper",
                farmerSearchPlaceholder = "Look for farmer name or ID...",
                totalTonnageLabel = "All Tuber Weight Total",
                starchContentLabel = "Starch Quality %",
                mobilePayoutLabel = "MoMo Mobile Money Pay",
                offlineSyncLabel = "Low Data Auto-Sync",
                geminiForecastTitle = "Gemini AI Crop Harvest Forecast",
                lowBatteryWarning = "Battery Low! Plug Solar Panel Fast!",
                languageSettingsTitle = "Talk & Dialect Settings"
            )

            AppLanguage.KPELLE -> LocalizedText(
                appTitle = "AgriCoop Liberia (Kpelle)",
                appSubtitle = "Bong & Lofa Farm Co-op Network",
                tabDashboard = "Kpelle Dashboard",
                tabWeigh = "Scale Cassava/Yam",
                tabFarmers = "Farmers Guild",
                tabProcessing = "Processing Center",
                tabFinancials = "Grant Treasury",
                tabSync = "Data Sync Network",
                weighScaleTitle = "Bluetooth Scale Unit (Kpelle)",
                weighScaleConnected = "Scale Active & Calibrated",
                weighScaleDisconnected = "Scale Disconnected",
                createBatchButton = "Record Harvest & Generate Ticket",
                farmerSearchPlaceholder = "Find farmer member record...",
                totalTonnageLabel = "Total Harvest Volume (Kg)",
                starchContentLabel = "Starch Yield Quality",
                mobilePayoutLabel = "Mobile Money Disbursement",
                offlineSyncLabel = "Cellular Packet Transfer",
                geminiForecastTitle = "Gemini AI Tuber Volume Predictor",
                lowBatteryWarning = "Battery Low - Connect Solar Power",
                languageSettingsTitle = "Kpelle Dialect & Interface Language"
            )

            AppLanguage.BASSA -> LocalizedText(
                appTitle = "AgriCoop Liberia (Bassa)",
                appSubtitle = "Grand Bassa & Margibi Produce Union",
                tabDashboard = "Bassa Dashboard",
                tabWeigh = "Scale Tuber Harvest",
                tabFarmers = "Farmers Registry",
                tabProcessing = "Processing Mills",
                tabFinancials = "Coop Payouts & Grants",
                tabSync = "Offline Network Sync",
                weighScaleTitle = "Bluetooth Scale Station",
                weighScaleConnected = "Scale Online & Ready",
                weighScaleDisconnected = "Scale Connection Interrupted",
                createBatchButton = "Save Tuber Lot & Print Receipt",
                farmerSearchPlaceholder = "Search Bassa coop member...",
                totalTonnageLabel = "Aggregated Tuber Yield",
                starchContentLabel = "Starch Density Percentage",
                mobilePayoutLabel = "Direct MoMo Payouts",
                offlineSyncLabel = "Low-Bandwidth Packet Sync",
                geminiForecastTitle = "Gemini AI Harvest Forecast Model",
                lowBatteryWarning = "Battery Critical - Solar Charging Needed",
                languageSettingsTitle = "Bassa Dialect & Language Options"
            )

            AppLanguage.LORMA -> LocalizedText(
                appTitle = "AgriCoop Lofa (Lorma)",
                appSubtitle = "Voinjama & Zorzor Agricultural Guild",
                tabDashboard = "Lorma Center",
                tabWeigh = "Scale Cassava Lot",
                tabFarmers = "Lofa Farmers Guild",
                tabProcessing = "Voinjama Processing Hub",
                tabFinancials = "Lofa Treasury Grants",
                tabSync = "Packet Data Sync",
                weighScaleTitle = "Lofa Scale Unit (Bluetooth IP67)",
                weighScaleConnected = "Scale Connected (Zorzor Station)",
                weighScaleDisconnected = "Scale Disconnected",
                createBatchButton = "Save Lorma Tuber Batch & Print Ticket",
                farmerSearchPlaceholder = "Search Lofa farmer name or ID...",
                totalTonnageLabel = "Total Lofa Tonnage Collected",
                starchContentLabel = "Starch Yield Quality %",
                mobilePayoutLabel = "Lofa MoMo Digital Payouts",
                offlineSyncLabel = "2G Packet Sync Protocol",
                geminiForecastTitle = "Gemini AI Lofa Crop Predictor",
                lowBatteryWarning = "Low Battery - Plug Solar System",
                languageSettingsTitle = "Lorma Dialect & Regional Interface"
            )

            AppLanguage.GBANDI -> LocalizedText(
                appTitle = "AgriCoop Kolahun (Gbandi)",
                appSubtitle = "Kolahun & Foya District Farmers Union",
                tabDashboard = "Gbandi Hub",
                tabWeigh = "Weigh Cassava & Yam",
                tabFarmers = "Kolahun Co-op Members",
                tabProcessing = "Foya Cassava Mill",
                tabFinancials = "District Grant Fund",
                tabSync = "Packet Sync Network",
                weighScaleTitle = "Kolahun Bluetooth Scale Station",
                weighScaleConnected = "Scale Connected (Kolahun Hub)",
                weighScaleDisconnected = "Scale Off",
                createBatchButton = "Record Weight & Issue Receipt",
                farmerSearchPlaceholder = "Search Kolahun member...",
                totalTonnageLabel = "Total Tuber Weight (Kg)",
                starchContentLabel = "Starch Quality Index",
                mobilePayoutLabel = "MoMo Mobile Payment",
                offlineSyncLabel = "Low-Data Sync Protocol",
                geminiForecastTitle = "Gemini AI Kolahun Harvest Model",
                lowBatteryWarning = "Solar Battery Low!",
                languageSettingsTitle = "Gbandi Dialect & Language Options"
            )

            AppLanguage.MANO -> LocalizedText(
                appTitle = "AgriCoop Nimba (Mano)",
                appSubtitle = "Sanniquellie & Sagleipie Agribusiness",
                tabDashboard = "Mano Dashboard",
                tabWeigh = "Scale Tuber Batch",
                tabFarmers = "Mano Farmers Union",
                tabProcessing = "Sanniquellie Mill",
                tabFinancials = "Nimba Grant Fund",
                tabSync = "Offline Data Sync",
                weighScaleTitle = "Nimba Scale Station (Mano)",
                weighScaleConnected = "Scale Online (Bluetooth IP67)",
                weighScaleDisconnected = "Scale Disconnected",
                createBatchButton = "Record Tuber Weighing & Print Slip",
                farmerSearchPlaceholder = "Search Mano farmer member...",
                totalTonnageLabel = "Total Nimba Tonnage (Kg)",
                starchContentLabel = "Starch Quality Density %",
                mobilePayoutLabel = "Nimba MoMo Payments",
                offlineSyncLabel = "2G/3G Packet Sync",
                geminiForecastTitle = "Gemini AI Nimba Harvest Predictor",
                lowBatteryWarning = "Low Battery - Connect Solar Panel",
                languageSettingsTitle = "Mano Dialect & Language Settings"
            )

            AppLanguage.GIO -> LocalizedText(
                appTitle = "AgriCoop Nimba (Gio / Dan)",
                appSubtitle = "Ganta & Tappita Produce Guild",
                tabDashboard = "Gio Center",
                tabWeigh = "Scale Cassava Lot",
                tabFarmers = "Ganta Farmers Guild",
                tabProcessing = "Tappita Starch Mill",
                tabFinancials = "Nimba Produce Treasury",
                tabSync = "Packet Data Protocol",
                weighScaleTitle = "Ganta Bluetooth Scale Station",
                weighScaleConnected = "Scale Active (Ganta Hub)",
                weighScaleDisconnected = "Scale Offline",
                createBatchButton = "Save Tuber Lot & Print Slip",
                farmerSearchPlaceholder = "Search Ganta/Tappita member...",
                totalTonnageLabel = "Total Tuber Weight Collected",
                starchContentLabel = "Average Starch Index %",
                mobilePayoutLabel = "Ganta MoMo Payments",
                offlineSyncLabel = "Packet Transmission Network",
                geminiForecastTitle = "Gemini AI Ganta Harvest Predictor",
                lowBatteryWarning = "Solar Battery Warning!",
                languageSettingsTitle = "Gio / Dan Dialect & Accessibility"
            )
        }
    }
}
