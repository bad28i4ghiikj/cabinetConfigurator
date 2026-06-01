package com.example.cabinetconfigurator.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cabinetconfigurator.data.repository.UserSettings
import com.example.cabinetconfigurator.di.AppModule
import com.example.cabinetconfigurator.domain.engine.CalculationKeys
import com.example.cabinetconfigurator.domain.model.Accessory
import com.example.cabinetconfigurator.domain.model.AccessoryCatalog
import com.example.cabinetconfigurator.domain.model.AccessoryType
import com.example.cabinetconfigurator.domain.model.CabinetTemplate
import com.example.cabinetconfigurator.domain.model.CabinetType
import com.example.cabinetconfigurator.domain.model.PricingProfile
import com.example.cabinetconfigurator.domain.model.Quote
import com.example.cabinetconfigurator.domain.model.QuoteCalculationResult
import com.example.cabinetconfigurator.domain.model.QuoteDraft
import com.example.cabinetconfigurator.domain.model.FurnitureElement
import com.example.cabinetconfigurator.domain.model.ElementType
import com.example.cabinetconfigurator.domain.model.FurniturePiece
import com.example.cabinetconfigurator.domain.model.ParameterDefinition
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab(val label: String) { PRICING("Cennik"), CONFIGURATOR("Wycena"), HISTORY("Historia") }

data class PricingFieldUi(val key: String, val label: String, val value: String, val unit: String?)

data class TemplateEditorState(
    val name: String = "",
    val cabinetType: String = "Komin",
    val widthMm: String = "600",
    val heightMm: String = "720",
    val depthMm: String = "560",
    val elements: List<FurnitureElement> = emptyList()
)

data class AppUiState(
    val selectedTab: AppTab = AppTab.PRICING,
    val profileName: String = "",
    val pricingFields: List<PricingFieldUi> = emptyList(),
    val accessoryPriceFields: List<PricingFieldUi> = emptyList(),
    val draftName: String = "",
    val furniture: List<FurniturePiece> = listOf(FurniturePiece()),
    val calculation: QuoteCalculationResult? = null,
    val history: List<Quote> = emptyList(),
    val templateCatalog: List<CabinetTemplate> = emptyList(),
    val isAddFurnitureOpen: Boolean = false,
    val isTemplatePickerOpen: Boolean = false,
    val isTemplateEditorOpen: Boolean = false,
    val isAccessoryPriceCatalogOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val templateEditorState: TemplateEditorState = TemplateEditorState(),
    val userSettings: UserSettings = UserSettings(),
    val generatedPdfFile: File? = null,
    val isGeneratingPdf: Boolean = false,
    val message: String? = null,
    val editingQuoteId: Long? = null
)

class AppViewModel(private val module: AppModule) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var activeProfile: PricingProfile? = null
    private var parameterDefinitions: Map<String, ParameterDefinition> = emptyMap()

    init {
        viewModelScope.launch {
            module.ensureSeedDataUseCase()
            module.ensureSeedTemplatesUseCase()
            val defs = module.getParameterDefinitionsUseCase()
            parameterDefinitions = defs.associateBy { it.key }
            // Collect profile only after parameterDefinitions is populated to avoid empty labels
            module.getActivePricingProfileUseCase().collect { profile ->
                activeProfile = profile
                val allFields = profile.values.entries.mapNotNull { entry ->
                    val def = parameterDefinitions[entry.key] ?: return@mapNotNull null
                    PricingFieldUi(entry.key, def.label, entry.value, def.unit)
                }
                _uiState.value = _uiState.value.copy(
                    profileName = profile.name,
                    pricingFields = allFields
                        .sortedBy { parameterDefinitions[it.key]?.sortOrder ?: Int.MAX_VALUE },
                    accessoryPriceFields = AccessoryCatalog.manufacturers.flatMap { mfr ->
                        AccessoryCatalog.modelsFor(mfr).map { mdl ->
                            val key = CalculationKeys.accessoryPriceKey(mfr, mdl)
                            PricingFieldUi(key, "$mfr \u2013 $mdl", profile.values[key] ?: "0", "zł/szt")
                        }
                    }
                )
            }
        }

        viewModelScope.launch {
            module.observeQuoteHistoryUseCase().collect { history ->
                _uiState.value = _uiState.value.copy(history = history)
            }
        }

        viewModelScope.launch {
            module.observeCabinetTemplatesUseCase().collect { templates ->
                _uiState.value = _uiState.value.copy(templateCatalog = templates)
            }
        }

        viewModelScope.launch {
            module.userSettingsRepository.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(userSettings = settings)
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun updatePricingField(key: String, value: String) {
        val profileId = activeProfile?.id ?: return
        _uiState.value = _uiState.value.copy(
            pricingFields = _uiState.value.pricingFields.map {
                if (it.key == key) it.copy(value = value) else it
            },
            accessoryPriceFields = _uiState.value.accessoryPriceFields.map {
                if (it.key == key) it.copy(value = value) else it
            }
        )
        viewModelScope.launch {
            module.updatePricingParameterUseCase(profileId, key, value)
        }
    }

    fun updateDraftName(value: String) {
        _uiState.value = _uiState.value.copy(draftName = value)
    }

    fun openAddFurniture() {
        _uiState.value = _uiState.value.copy(isAddFurnitureOpen = true)
    }

    fun closeAddFurniture() {
        _uiState.value = _uiState.value.copy(isAddFurnitureOpen = false)
    }

    fun openTemplatePicker() {
        _uiState.value = _uiState.value.copy(isTemplatePickerOpen = true, isAddFurnitureOpen = false)
    }

    fun closeTemplatePicker() {
        _uiState.value = _uiState.value.copy(isTemplatePickerOpen = false)
    }

    fun openTemplateEditor() {
        _uiState.value = _uiState.value.copy(
            isTemplateEditorOpen = true,
            isTemplatePickerOpen = false,
            isAddFurnitureOpen = false,
            templateEditorState = TemplateEditorState()
        )
    }

    fun closeTemplateEditor() {
        _uiState.value = _uiState.value.copy(
            isTemplateEditorOpen = false,
            templateEditorState = TemplateEditorState()
        )
    }

    fun openAccessoryPriceCatalog() {
        _uiState.value = _uiState.value.copy(isAccessoryPriceCatalogOpen = true)
    }

    fun closeAccessoryPriceCatalog() {
        _uiState.value = _uiState.value.copy(isAccessoryPriceCatalogOpen = false)
    }

    fun updateTemplateEditorName(value: String) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(name = value)
        )
    }

    fun updateTemplateEditorCabinetType(value: String) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(cabinetType = value)
        )
    }

    fun updateTemplateEditorWidth(value: String) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(widthMm = value)
        )
    }

    fun updateTemplateEditorHeight(value: String) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(heightMm = value)
        )
    }

    fun updateTemplateEditorDepth(value: String) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(depthMm = value)
        )
    }

    fun addTemplateEditorElement(type: ElementType) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(
                elements = _uiState.value.templateEditorState.elements + FurnitureElement(type = type)
            )
        )
    }

    fun removeTemplateEditorElement(elementId: String) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(
                elements = _uiState.value.templateEditorState.elements.filter { it.id != elementId }
            )
        )
    }

    fun updateTemplateEditorElementQuantity(elementId: String, quantity: Int) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(
                elements = _uiState.value.templateEditorState.elements.map {
                    if (it.id == elementId) it.copy(quantity = maxOf(1, quantity)) else it
                }
            )
        )
    }

    fun addTemplateAccessory(elementId: String, type: AccessoryType) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(
                elements = _uiState.value.templateEditorState.elements.map { elem ->
                    if (elem.id == elementId) {
                        elem.copy(accessories = elem.accessories + Accessory(type = type))
                    } else elem
                }
            )
        )
    }

    fun removeTemplateAccessory(elementId: String, accessoryId: String) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(
                elements = _uiState.value.templateEditorState.elements.map { elem ->
                    if (elem.id == elementId) {
                        elem.copy(accessories = elem.accessories.filter { it.id != accessoryId })
                    } else elem
                }
            )
        )
    }

    fun updateTemplateAccessoryQuantity(elementId: String, accessoryId: String, quantity: Int) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(
                elements = _uiState.value.templateEditorState.elements.map { elem ->
                    if (elem.id == elementId) {
                        elem.copy(accessories = elem.accessories.map { acc ->
                            if (acc.id == accessoryId) acc.copy(quantity = maxOf(1, quantity)) else acc
                        })
                    } else elem
                }
            )
        )
    }

    fun updateTemplateAccessoryManufacturer(elementId: String, accessoryId: String, manufacturer: String) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(
                elements = _uiState.value.templateEditorState.elements.map { elem ->
                    if (elem.id == elementId) {
                        elem.copy(accessories = elem.accessories.map { acc ->
                            if (acc.id == accessoryId) acc.copy(manufacturer = manufacturer, model = "") else acc
                        })
                    } else elem
                }
            )
        )
    }

    fun updateTemplateAccessoryModel(elementId: String, accessoryId: String, model: String) {
        _uiState.value = _uiState.value.copy(
            templateEditorState = _uiState.value.templateEditorState.copy(
                elements = _uiState.value.templateEditorState.elements.map { elem ->
                    if (elem.id == elementId) {
                        elem.copy(accessories = elem.accessories.map { acc ->
                            if (acc.id == accessoryId) acc.copy(model = model) else acc
                        })
                    } else elem
                }
            )
        )
    }

    fun saveTemplate() {
        val templateState = _uiState.value.templateEditorState
        val template = CabinetTemplate(
            name = templateState.name.ifBlank { "Nowy szablon" },
            cabinetType = templateState.cabinetType.ifBlank { "Komin" },
            widthMm = templateState.widthMm.toIntOrNull() ?: 600,
            heightMm = templateState.heightMm.toIntOrNull() ?: 720,
            depthMm = templateState.depthMm.toIntOrNull() ?: 560,
            elements = templateState.elements
        )

        viewModelScope.launch {
            runCatching {
                module.saveCabinetTemplateUseCase(template)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    message = "Zapisano szablon",
                    isTemplateEditorOpen = false,
                    templateEditorState = TemplateEditorState()
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(message = it.message)
            }
        }
    }

    fun addFurniturePieceFromTemplate(templateId: Long) {
        val template = _uiState.value.templateCatalog.find { it.id == templateId } ?: return
        val piece = FurniturePiece(
            name = template.name,
            widthMm = template.widthMm,
            heightMm = template.heightMm,
            depthMm = template.depthMm,
            elements = template.elements
        )
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture + piece,
            isTemplatePickerOpen = false
        )
    }

    fun addFurniturePiece() {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture + FurniturePiece()
        )
    }

    fun removeFurniturePiece(pieceId: String) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.filter { it.id != pieceId }
        )
    }

    fun updateFurnitureCabinetType(pieceId: String, type: CabinetType) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map {
                if (it.id == pieceId) it.copy(cabinetType = type) else it
            }
        )
    }

    fun updateFurnitureName(pieceId: String, name: String) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map {
                if (it.id == pieceId) it.copy(name = name) else it
            }
        )
    }

    fun updateFurnitureWidth(pieceId: String, value: String) {
        val width = value.toIntOrNull() ?: return
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map {
                if (it.id == pieceId) it.copy(widthMm = width) else it
            }
        )
    }

    fun updateFurnitureHeight(pieceId: String, value: String) {
        val height = value.toIntOrNull() ?: return
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map {
                if (it.id == pieceId) it.copy(heightMm = height) else it
            }
        )
    }

    fun updateFurnitureDepth(pieceId: String, value: String) {
        val depth = value.toIntOrNull() ?: return
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map {
                if (it.id == pieceId) it.copy(depthMm = depth) else it
            }
        )
    }

    fun addElementToFurniture(pieceId: String, elementType: ElementType) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map { piece ->
                if (piece.id == pieceId) {
                    piece.copy(elements = piece.elements + FurnitureElement(type = elementType))
                } else piece
            }
        )
    }

    fun removeElementFromFurniture(pieceId: String, elementId: String) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map { piece ->
                if (piece.id == pieceId) {
                    piece.copy(elements = piece.elements.filter { it.id != elementId })
                } else piece
            }
        )
    }

    fun updateElementQuantity(pieceId: String, elementId: String, quantity: Int) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map { piece ->
                if (piece.id == pieceId) {
                    piece.copy(elements = piece.elements.map { elem ->
                        if (elem.id == elementId) elem.copy(quantity = maxOf(1, quantity)) else elem
                    })
                } else piece
            }
        )
    }

    fun updateElementHeight(pieceId: String, elementId: String, height: Int?) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map { piece ->
                if (piece.id == pieceId) {
                    piece.copy(elements = piece.elements.map { elem ->
                        if (elem.id == elementId) elem.copy(heightMm = height) else elem
                    })
                } else piece
            }
        )
    }

    fun toggleElementCustomHeight(pieceId: String, elementId: String, useCustom: Boolean) {
        val piece = _uiState.value.furniture.find { it.id == pieceId } ?: return
        val element = piece.elements.find { it.id == elementId } ?: return
        val frontsCount = piece.elements.filter { it.type == ElementType.FRONT }.sumOf { it.quantity }
        val defaultHeight = if (frontsCount > 0) piece.heightMm / frontsCount else piece.heightMm

        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map { p ->
                if (p.id == pieceId) {
                    p.copy(elements = p.elements.map { elem ->
                        if (elem.id == elementId) {
                            elem.copy(heightMm = if (useCustom) defaultHeight else null)
                        } else elem
                    })
                } else p
            }
        )
    }

    fun addAccessory(pieceId: String, elementId: String, type: AccessoryType) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map { piece ->
                if (piece.id == pieceId) {
                    piece.copy(elements = piece.elements.map { elem ->
                        if (elem.id == elementId) {
                            elem.copy(accessories = elem.accessories + Accessory(type = type))
                        } else elem
                    })
                } else piece
            }
        )
    }

    fun removeAccessory(pieceId: String, elementId: String, accessoryId: String) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map { piece ->
                if (piece.id == pieceId) {
                    piece.copy(elements = piece.elements.map { elem ->
                        if (elem.id == elementId) {
                            elem.copy(accessories = elem.accessories.filter { it.id != accessoryId })
                        } else elem
                    })
                } else piece
            }
        )
    }

    fun updateAccessoryQuantity(pieceId: String, elementId: String, accessoryId: String, quantity: Int) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map { piece ->
                if (piece.id == pieceId) {
                    piece.copy(elements = piece.elements.map { elem ->
                        if (elem.id == elementId) {
                            elem.copy(accessories = elem.accessories.map { acc ->
                                if (acc.id == accessoryId) acc.copy(quantity = maxOf(1, quantity)) else acc
                            })
                        } else elem
                    })
                } else piece
            }
        )
    }

    fun updateAccessoryManufacturer(pieceId: String, elementId: String, accessoryId: String, manufacturer: String) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map { piece ->
                if (piece.id == pieceId) {
                    piece.copy(elements = piece.elements.map { elem ->
                        if (elem.id == elementId) {
                            elem.copy(accessories = elem.accessories.map { acc ->
                                if (acc.id == accessoryId) acc.copy(manufacturer = manufacturer, model = "") else acc
                            })
                        } else elem
                    })
                } else piece
            }
        )
    }

    fun updateAccessoryModel(pieceId: String, elementId: String, accessoryId: String, model: String) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map { piece ->
                if (piece.id == pieceId) {
                    piece.copy(elements = piece.elements.map { elem ->
                        if (elem.id == elementId) {
                            elem.copy(accessories = elem.accessories.map { acc ->
                                if (acc.id == accessoryId) acc.copy(model = model) else acc
                            })
                        } else elem
                    })
                } else piece
            }
        )
    }

    fun calculate() {
        val draft = currentDraft()
        viewModelScope.launch {
            runCatching {
                val (_, result) = module.calculateQuoteUseCase(draft)
                result
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    calculation = it,
                    message = null,
                    furniture = draft.furniture
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(message = it.message)
            }
        }
    }

    fun saveQuote() {
        viewModelScope.launch {
            runCatching {
                val editingId = _uiState.value.editingQuoteId
                if (editingId != null) {
                    module.deleteQuoteUseCase(editingId)
                }
                val id = module.saveQuoteUseCase(currentDraft())
                id
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    message = "Zapisano wycenę #$it",
                    editingQuoteId = null,
                    draftName = "",
                    furniture = listOf(FurniturePiece()),
                    calculation = null
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(message = it.message)
            }
        }
    }

    fun loadQuoteForEditing(quote: Quote) {
        _uiState.value = _uiState.value.copy(
            draftName = quote.name,
            furniture = quote.furniture.toMutableList(),
            selectedTab = AppTab.CONFIGURATOR,
            editingQuoteId = quote.id,
            message = "Wczytano wycenę do edycji"
        )
    }

    fun openSettings() {
        _uiState.value = _uiState.value.copy(isSettingsOpen = true)
    }

    fun closeSettings() {
        _uiState.value = _uiState.value.copy(isSettingsOpen = false)
    }

    fun updateCompanyName(name: String) {
        module.userSettingsRepository.updateCompanyName(name)
    }

    fun updateLogoUri(uri: Uri?) {
        module.userSettingsRepository.updateLogoUri(uri)
    }

    fun exportQuoteToPdf(quote: Quote) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingPdf = true)
            runCatching {
                val (_, result) = module.calculateQuoteUseCase(
                    QuoteDraft(name = quote.name, furniture = quote.furniture)
                )
                val settings = module.userSettingsRepository.getSettings()
                module.pdfGenerator.generateQuotePdf(
                    quote = quote,
                    calculation = result,
                    logoUri = settings.logoUri,
                    companyName = settings.companyName.ifBlank { null }
                )
            }.onSuccess { file ->
                _uiState.value = _uiState.value.copy(
                    generatedPdfFile = file,
                    isGeneratingPdf = false,
                    message = "PDF wygenerowany"
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isGeneratingPdf = false,
                    message = "Błąd generowania PDF: ${e.message}"
                )
            }
        }
    }

    fun clearGeneratedPdf() {
        _uiState.value = _uiState.value.copy(generatedPdfFile = null)
    }

    private fun currentDraft(): QuoteDraft = QuoteDraft(
        name = _uiState.value.draftName,
        furniture = _uiState.value.furniture
    )
}

class AppViewModelFactory(private val module: AppModule) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(AppViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        @Suppress("UNCHECKED_CAST")
        return AppViewModel(module) as T
    }
}
