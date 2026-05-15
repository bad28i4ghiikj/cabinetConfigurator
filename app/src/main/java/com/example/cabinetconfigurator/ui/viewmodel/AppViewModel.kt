package com.example.cabinetconfigurator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cabinetconfigurator.di.AppModule
import com.example.cabinetconfigurator.domain.model.PricingProfile
import com.example.cabinetconfigurator.domain.model.Quote
import com.example.cabinetconfigurator.domain.model.QuoteCalculationResult
import com.example.cabinetconfigurator.domain.model.QuoteDraft
import com.example.cabinetconfigurator.domain.model.FurnitureElement
import com.example.cabinetconfigurator.domain.model.ElementType
import com.example.cabinetconfigurator.domain.model.FurniturePiece
import com.example.cabinetconfigurator.domain.model.ParameterDefinition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab(val label: String) { PRICING("Cennik"), CONFIGURATOR("Wycena"), HISTORY("Historia") }

data class PricingFieldUi(val key: String, val label: String, val value: String, val unit: String?)
data class ZoneUi(val name: String, val orderIndex: Int, val quantity: Int, val hingeCount: Int, val drawerRunnerCount: Int)

data class AppUiState(
    val selectedTab: AppTab = AppTab.PRICING,
    val profileName: String = "",
    val pricingFields: List<PricingFieldUi> = emptyList(),
    val draftName: String = "",
    val furniture: List<FurniturePiece> = listOf(FurniturePiece()),
    val calculation: QuoteCalculationResult? = null,
    val history: List<Quote> = emptyList(),
    val message: String? = null
)

class AppViewModel(private val module: AppModule) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var activeProfile: PricingProfile? = null
    private var parameterDefinitions: Map<String, ParameterDefinition> = emptyMap()

    init {
        viewModelScope.launch {
            module.ensureSeedDataUseCase()
            val defs = module.getParameterDefinitionsUseCase()
            parameterDefinitions = defs.associateBy { it.key }
        }

        viewModelScope.launch {
            module.getActivePricingProfileUseCase().collect { profile ->
                activeProfile = profile
                _uiState.value = _uiState.value.copy(
                    profileName = profile.name,
                    pricingFields = profile.values.entries.map { entry ->
                        val def = parameterDefinitions[entry.key]
                        PricingFieldUi(entry.key, def?.label ?: entry.key, entry.value, def?.unit)
                    }
                )
            }
        }

        viewModelScope.launch {
            module.observeQuoteHistoryUseCase().collect { history ->
                _uiState.value = _uiState.value.copy(history = history)
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
            }
        )
        viewModelScope.launch {
            module.updatePricingParameterUseCase(profileId, key, value)
        }
    }

    fun updateDraftName(value: String) {
        _uiState.value = _uiState.value.copy(draftName = value)
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

    fun updateElementHinges(pieceId: String, elementId: String, hinges: Int) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map { piece ->
                if (piece.id == pieceId) {
                    piece.copy(elements = piece.elements.map { elem ->
                        if (elem.id == elementId && elem.type == ElementType.FRONT) {
                            elem.copy(hingeCount = maxOf(0, hinges))
                        } else elem
                    })
                } else piece
            }
        )
    }

    fun updateElementRunners(pieceId: String, elementId: String, runners: Int) {
        _uiState.value = _uiState.value.copy(
            furniture = _uiState.value.furniture.map { piece ->
                if (piece.id == pieceId) {
                    piece.copy(elements = piece.elements.map { elem ->
                        if (elem.id == elementId && elem.type == ElementType.DRAWER) {
                            elem.copy(drawerRunnerCount = maxOf(0, runners))
                        } else elem
                    })
                } else piece
            }
        )
    }

    fun calculate() {
        viewModelScope.launch {
            runCatching {
                val (_, result) = module.calculateQuoteUseCase(currentDraft())
                result
            }.onSuccess {
                _uiState.value = _uiState.value.copy(calculation = it, message = null)
            }.onFailure {
                _uiState.value = _uiState.value.copy(message = it.message)
            }
        }
    }

    fun saveQuote() {
        viewModelScope.launch {
            runCatching {
                val id = module.saveQuoteUseCase(currentDraft())
                id
            }.onSuccess {
                _uiState.value = _uiState.value.copy(message = "Zapisano wycenę #$it")
            }.onFailure {
                _uiState.value = _uiState.value.copy(message = it.message)
            }
        }
    }

    private fun currentDraft(): QuoteDraft = QuoteDraft(
        name = _uiState.value.draftName,
        furniture = _uiState.value.furniture
    )
}

class AppViewModelFactory(private val module: AppModule) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(module) as T
}
