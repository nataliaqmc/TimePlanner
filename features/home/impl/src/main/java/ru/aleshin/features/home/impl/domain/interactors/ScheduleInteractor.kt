/*
 * Copyright 2023 Stanislav Aleshin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.aleshin.features.home.impl.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.aleshin.core.utils.extensions.daysToMillis
import ru.aleshin.core.utils.extensions.mapToDate
import ru.aleshin.core.utils.functional.Constants.Date.NEXT_REPEAT_LIMIT
import ru.aleshin.core.utils.functional.Either
import ru.aleshin.core.utils.managers.DateManager
import ru.aleshin.core.utils.managers.TimeOverlayManager
import ru.aleshin.features.home.api.domain.common.ScheduleStatusChecker
import ru.aleshin.features.home.api.domain.entities.schedules.Schedule
import ru.aleshin.features.home.api.domain.entities.schedules.TimeTask
import ru.aleshin.features.home.api.domain.entities.template.Template
import ru.aleshin.features.home.api.domain.repository.ScheduleRepository
import ru.aleshin.features.home.api.domain.repository.TemplatesRepository
import ru.aleshin.features.home.impl.domain.common.HomeEitherWrapper
import ru.aleshin.features.home.impl.domain.common.convertToTimeTask
import ru.aleshin.features.home.impl.domain.entities.HomeFailures
import java.util.*
import javax.inject.Inject

/**
 * @author Stanislav Aleshin on 25.02.2023.
 */
internal interface ScheduleInteractor {

    suspend fun fetchScheduleByDate(date: Long): Flow<Either<HomeFailures, Schedule?>>

    suspend fun createSchedule(requiredDay: Date): Either<HomeFailures, Unit>

    suspend fun updateSchedule(schedule: Schedule): Either<HomeFailures, Unit>

    class Base @Inject constructor(
        private val scheduleRepository: ScheduleRepository,
        private val templatesRepository: TemplatesRepository,
        private val statusChecker: ScheduleStatusChecker,
        private val dateManager: DateManager,
        private val overlayManager: TimeOverlayManager,
        private val eitherWrapper: HomeEitherWrapper,
    ) : ScheduleInteractor {

        override suspend fun fetchScheduleByDate(date: Long) = eitherWrapper.wrapFlow {
            val currentDate = dateManager.fetchBeginningCurrentDay()
            val limit = NEXT_REPEAT_LIMIT.daysToMillis()
            scheduleRepository.fetchScheduleByDate(date).map { schedule ->
                if (schedule != null) {
                    val sortedTasks = schedule.timeTasks.sortedBy { timeTask -> timeTask.timeRanges.to }
                    schedule.copy(timeTasks = sortedTasks)
                } else if (date >= currentDate.time && date - currentDate.time <= limit) {
                    createRecurringSchedule(date.mapToDate(), currentDate)
                } else {
                    null
                }
            }
        }

        override suspend fun createSchedule(requiredDay: Date) = eitherWrapper.wrap {
            val currentDate = dateManager.fetchBeginningCurrentDay()
            val status = statusChecker.fetchState(requiredDay, currentDate)
            val schedule = Schedule(date = requiredDay.time, status = status)
            scheduleRepository.createSchedules(listOf(schedule))
        }

        override suspend fun updateSchedule(schedule: Schedule) = eitherWrapper.wrap {
            scheduleRepository.updateSchedule(schedule)
        }

        private suspend fun createRecurringSchedule(target: Date, current: Date): Schedule? {
            val templates = foundPlannedTemplates(target).apply { if (this.isEmpty()) return null }
            val templatesTimeTasks = templates.map { it.convertToTimeTask(target) }
            val correctTimeTasks = mutableListOf<TimeTask>().apply {
                templatesTimeTasks.sortedBy { it.timeRanges.from }.forEach { timeTask ->
                    val allTimeRanges = map { it.timeRanges }
                    val overlayResult = overlayManager.isOverlay(timeTask.timeRanges, allTimeRanges)
                    if (!overlayResult.isOverlay) add(timeTask)
                }
            }

            val status = statusChecker.fetchState(target, current)
            return Schedule(date = target.time, status = status, timeTasks = correctTimeTasks).apply {
                scheduleRepository.createSchedules(listOf(this))
            }
        }

        private suspend fun foundPlannedTemplates(date: Date) = mutableListOf<Template>().apply {
            templatesRepository.fetchAllTemplates().first().filter { template ->
                template.repeatEnabled
            }.forEach { template ->
                template.repeatTimes.forEach { repeatTime ->
                    if (repeatTime.checkDateIsRepeat(date)) add(template)
                }
            }
        }
    }
}
