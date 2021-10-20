/*
 * Copyright (c) 2020 Proton Technologies AG
 *
 * This file is part of ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail. If not, see https://www.gnu.org/licenses/.
 */

package ch.protonmail.android.labels.data.mapper

import ch.protonmail.android.labels.data.remote.model.LabelRequestBody
import ch.protonmail.android.labels.domain.model.Label
import me.proton.core.domain.arch.Mapper
import javax.inject.Inject

class LabelRequestMapper @Inject constructor() : Mapper<Label, LabelRequestBody> {

    fun toRequest(labelEntity: Label) = LabelRequestBody(
        name = labelEntity.name,
        color = labelEntity.color,
        type = labelEntity.type.typeInt,
        parentId = if (labelEntity.parentId.isBlank()) null else labelEntity.parentId,
        notify = null,
        expanded = null,
        sticky = null
    )
}
