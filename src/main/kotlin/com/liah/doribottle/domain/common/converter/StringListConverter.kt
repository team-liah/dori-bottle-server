package com.liah.doribottle.domain.common.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {
    override fun convertToDatabaseColumn(attribute: List<String>?): String? {
        return attribute?.joinToString(",")
    }

    override fun convertToEntityAttribute(dbData: String?): List<String>? {
        if (dbData?.isBlank() == true) {
            return emptyList()
        }
        return dbData?.split(",")
    }
}
