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
package ru.aleshin.features.editor.impl.presentation.models.template

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.aleshin.features.editor.impl.presentation.models.categories.MainCategoryUi
import ru.aleshin.features.editor.impl.presentation.models.categories.SubCategoryUi
import ru.aleshin.features.home.api.domain.entities.template.RepeatTime
import java.util.Date

/**
 * @author Stanislav Aleshin on 30.07.2023.
 */
@Parcelize
internal data class TemplateUi(
    val templateId: Int,
    val startTime: Date,
    val endTime: Date,
    val category: MainCategoryUi,
    val subCategory: SubCategoryUi? = null,
    val isImportant: Boolean = false,
    val isEnableNotification: Boolean = true,
    val isConsiderInStatistics: Boolean = true,
    val repeatEnabled: Boolean = true,
    val repeatTimes: List<RepeatTime> = emptyList(),
) : Parcelable
