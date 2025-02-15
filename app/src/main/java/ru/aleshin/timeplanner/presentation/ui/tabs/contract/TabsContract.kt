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
package ru.aleshin.timeplanner.presentation.ui.tabs.contract

import kotlinx.parcelize.Parcelize
import ru.aleshin.core.utils.platform.screenmodel.contract.*
import ru.aleshin.timeplanner.presentation.ui.tabs.views.TabsBottomBarItems

/**
 * @author Stanislav Aleshin on 18.02.2023.
 */
@Parcelize
data class TabsViewState(
    val bottomBarItem: TabsBottomBarItems = TabsBottomBarItems.HOME,
) : BaseViewState

sealed class TabsEvent : BaseEvent {
    object Init : TabsEvent()
    object SelectedHomeTab : TabsEvent()
    object SelectedMainScreen : TabsEvent()
    object SelectedTemplateScreen : TabsEvent()
    object SelectedCategoriesScreen : TabsEvent()
    object SelectedAnalyticsTab : TabsEvent()
    object SelectedSettingsTab : TabsEvent()
}

sealed class TabsEffect : EmptyUiEffect

sealed class TabsAction : TabsEffect(), BaseAction {
    data class ChangeNavItems(val item: TabsBottomBarItems) : TabsAction()
}
