package com.epam.brn.service.parsers.csv.secondSeries

import com.epam.brn.service.parsers.csv.CsvParser
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.InputStream
import org.springframework.stereotype.Service

@Service
class SecondSeriesCSVParserService : CsvParser<Map<String, Any>> {

    override fun parseCsvFile(file: InputStream): MappingIterator<Map<String, Any>> {
        val csvMapper = CsvMapper()

        val csvSchema = CsvSchema
            .emptySchema()
            .withHeader()
            .withColumnSeparator(',')
            .withColumnReordering(true)
            .withLineSeparator(",")
            .withArrayElementSeparator(";")

        return csvMapper
            .readerFor(Map::class.java)
            .with(csvSchema)
            .readValues<Map<String, Any>>(file)
    }
}
