package com.company.cabinetConfigurator.data.repository

import com.example.cabinetconfigurator.data.local.CabinetTemplateElementEntity
import com.example.cabinetconfigurator.data.local.CabinetTemplateEntity
import com.example.cabinetconfigurator.data.local.TemplateDao
import com.example.cabinetconfigurator.domain.model.CabinetTemplate
import com.example.cabinetconfigurator.domain.model.ElementType
import com.example.cabinetconfigurator.domain.model.FurnitureElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TemplateRepository(private val dao: TemplateDao) {

    fun observeTemplates(): Flow<List<CabinetTemplate>> = dao.observeTemplates().map { list ->
        list.map { agg ->
            CabinetTemplate(
                id = agg.template.id,
                name = agg.template.name,
                cabinetType = agg.template.cabinetType,
                widthMm = agg.template.widthMm,
                heightMm = agg.template.heightMm,
                depthMm = agg.template.depthMm,
                elements = agg.elements.sortedBy { it.orderIndex }.map { el ->
                    FurnitureElement(
                        type = runCatching { ElementType.valueOf(el.type) }.getOrElse { ElementType.FRONT },
                        quantity = el.quantity,
                        accessories = el.accessoriesJson.toAccessoryList(),
                        widthMm = el.widthMm,
                        heightMm = el.heightMm,
                        depthMm = el.depthMm
                    )
                }
            )
        }
    }

    suspend fun saveTemplate(template: CabinetTemplate): Long {
        val templateId = dao.insertTemplate(
            CabinetTemplateEntity(
                id = template.id,
                name = template.name,
                cabinetType = template.cabinetType,
                widthMm = template.widthMm,
                heightMm = template.heightMm,
                depthMm = template.depthMm
            )
        )
        dao.deleteTemplateElements(templateId)
        dao.insertTemplateElements(
            template.elements.mapIndexed { idx, element ->
                CabinetTemplateElementEntity(
                    templateId = templateId,
                    type = element.type.name,
                    quantity = element.quantity,
                    accessoriesJson = element.accessories.toJson(),
                    widthMm = element.widthMm,
                    heightMm = element.heightMm,
                    depthMm = element.depthMm,
                    orderIndex = idx
                )
            }
        )
        return templateId
    }

    suspend fun ensureSeedTemplates() {
        if (dao.countTemplates() > 0) return
        saveTemplate(
            CabinetTemplate(
                name = "Szafa typ komin",
                cabinetType = "Słupek / Komin",
                widthMm = 600,
                heightMm = 2160,
                depthMm = 560,
                elements = listOf(
                    FurnitureElement(
                        type = ElementType.FRONT,
                        quantity = 2
                    )
                )
            )
        )
    }
}
