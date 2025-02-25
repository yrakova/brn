package com.epam.brn.service.statistic.progress.status.impl

import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistic.progress.status.ExercisingStatusRetriever
import com.epam.brn.service.statistic.progress.status.ProgressStatusManager
import org.springframework.stereotype.Service

@Service
class StudyHistoriesProgressStatusManager(
    private val retrievers: List<ExercisingStatusRetriever<List<*>>>
) : ProgressStatusManager<List<StudyHistory>> {

    override fun getStatus(periodType: UserExercisingPeriod, progress: List<StudyHistory>): UserExercisingProgressStatus? {
        val allStatuses: ArrayList<UserExercisingProgressStatus?> = ArrayList()
        retrievers.filter {
            it.getSupportedPeriods().contains(periodType)
        }.forEach {
            allStatuses.add(it.getWorstStatus(progress))
        }
        return allStatuses.filterNotNull().minByOrNull { it!!.ordinal }
    }
}
