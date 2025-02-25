package com.epam.brn.dto

import com.epam.brn.model.Resource

data class AudiometryMatrixTaskDto(
    val id: Long?,
    val count: Int = 10,
    val answerOptions: MutableSet<Resource>
)
