package voz_do_povo_api.Utils

enum class ReportCategory {
    URBAN_INFRASTRUCTURE,
    ESSENTIAL_PUBLIC_SERVICES,
    WATER,
    ELECTRICITY,
    BASIC_SANITATION,
    MOBILITY,
    TRANSPORT,
    DISTURBANCE_OF_THE_PEACE,
    PUBLIC_SAFETY_AND_ORDER,
    ANIMAL_HEALTH_AND_ZOONOSES,
    ENVIRONMENT,
    CLEANING
}

fun sendMessageByCategoryReport (category: ReportCategory) : String {
    return when (category){
        ReportCategory.CLEANING-> "LIMPEZA"
        ReportCategory.ENVIRONMENT-> "MEIO AMBIENTE"
        ReportCategory.URBAN_INFRASTRUCTURE -> "INFRAESTRUTURA"
        ReportCategory.TRANSPORT -> "TRANSPORTE "
        ReportCategory.MOBILITY -> "MOBILIDADE"
        ReportCategory.ESSENTIAL_PUBLIC_SERVICES -> "SERVIÇOS"
        ReportCategory.WATER -> " ÁGUA"
        ReportCategory.ELECTRICITY -> " ENERGIA ELÉTRICA"
        ReportCategory.BASIC_SANITATION -> " SANEAMENTO BÁSICO"
        ReportCategory.DISTURBANCE_OF_THE_PEACE -> "PERTURBAÇÃO DO SOSSEGO"
        ReportCategory.PUBLIC_SAFETY_AND_ORDER -> "SEGURANÇA"
        ReportCategory.ANIMAL_HEALTH_AND_ZOONOSES -> "ANIMAIS E ZOONOSES"
    }
}