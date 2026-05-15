package com.example.cabinetconfigurator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cabinetconfigurator.domain.model.CalculationLine
import com.example.cabinetconfigurator.ui.viewmodel.AppTab
import com.example.cabinetconfigurator.ui.viewmodel.AppUiState
import com.example.cabinetconfigurator.ui.viewmodel.AppViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.cabinetconfigurator.domain.model.ElementType
import com.example.cabinetconfigurator.domain.model.FurniturePiece
import com.example.cabinetconfigurator.domain.model.FurnitureElement
import androidx.compose.foundation.layout.width

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(vm: AppViewModel) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cabinet Configurator") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                AppTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { vm.selectTab(tab) },
                        text = { Text(tab.label) }
                    )
                }
            }
            when (state.selectedTab) {
                AppTab.PRICING -> PricingTab(state, vm)
                AppTab.CONFIGURATOR -> ConfiguratorTab(state, vm)
                AppTab.HISTORY -> HistoryTab(state)
            }
        }
    }
}

@Composable
private fun PricingTab(state: AppUiState, vm: AppViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text("Profil: ${state.profileName}")
        }
        items(state.pricingFields, key = { it.key }) { field ->
            OutlinedTextField(
                value = field.value,
                onValueChange = { vm.updatePricingField(field.key, it) },
                label = { Text(field.label) },
                supportingText = { Text(field.unit ?: "") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ConfiguratorTab(state: AppUiState, vm: AppViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            OutlinedTextField(
                value = state.draftName,
                onValueChange = vm::updateDraftName,
                label = { Text("Nazwa wyceny") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        items(state.furniture, key = { it.id }) { piece ->
            FurniturePieceCard(piece, vm)
        }

        item {
            Button(
                onClick = vm::addFurniturePiece,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Dodaj mebel")
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = vm::calculate, modifier = Modifier.weight(1f)) { Text("Przelicz") }
                Button(onClick = vm::saveQuote, modifier = Modifier.weight(1f)) { Text("Zapisz") }
            }
        }

        item {
            state.calculation?.let { result ->
                Text("Netto: ${result.totalNet}")
                Text("Brutto: ${result.totalGross}")
                Spacer(Modifier.height(8.dp))
                result.lines.forEach { line -> CalculationLineView(line) }
                if (result.warnings.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Ostrzeżenia:")
                    result.warnings.forEach { Text("- $it") }
                }
            } ?: Text("Brak kalkulacji")
        }
        item {
            state.message?.let { Text(it) }
        }
    }
}

@Composable
private fun FurniturePieceCard(piece: FurniturePiece, vm: AppViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mebel", modifier = Modifier.weight(1f))
                IconButton(onClick = { vm.removeFurniturePiece(piece.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Usuń")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = piece.widthMm.toString(),
                    onValueChange = { vm.updateFurnitureWidth(piece.id, it) },
                    label = { Text("Szer.") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = piece.heightMm.toString(),
                    onValueChange = { vm.updateFurnitureHeight(piece.id, it) },
                    label = { Text("Wys.") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = piece.depthMm.toString(),
                    onValueChange = { vm.updateFurnitureDepth(piece.id, it) },
                    label = { Text("Głęb.") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Elementy:")
            piece.elements.forEach { element ->
                FurnitureElementRow(piece.id, element, vm)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.addElementToFurniture(piece.id, ElementType.FRONT) }) {
                    Text("+ Front")
                }
                Button(onClick = { vm.addElementToFurniture(piece.id, ElementType.DRAWER) }) {
                    Text("+ Szuflada")
                }
            }
        }
    }
}

@Composable
private fun FurnitureElementRow(pieceId: String, element: FurnitureElement, vm: AppViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(element.type.name, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = element.quantity.toString(),
            onValueChange = { vm.updateElementQuantity(pieceId, element.id, it.toIntOrNull() ?: 1) },
            label = { Text("Ilość") },
            modifier = Modifier.width(70.dp)
        )
        if (element.type == ElementType.FRONT) {
            OutlinedTextField(
                value = element.hingeCount.toString(),
                onValueChange = { vm.updateElementHinges(pieceId, element.id, it.toIntOrNull() ?: 0) },
                label = { Text("Zaw.") },
                modifier = Modifier.width(70.dp)
            )
        }
        if (element.type == ElementType.DRAWER) {
            OutlinedTextField(
                value = element.drawerRunnerCount.toString(),
                onValueChange = { vm.updateElementRunners(pieceId, element.id, it.toIntOrNull() ?: 0) },
                label = { Text("Prov.") },
                modifier = Modifier.width(70.dp)
            )
        }
        IconButton(onClick = { vm.removeElementFromFurniture(pieceId, element.id) }) {
            Icon(Icons.Default.Delete, contentDescription = "Usuń element")
        }
    }
}

@Composable
private fun CalculationLineView(line: CalculationLine) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(line.label)
            Text("Kwota: ${line.amount}")
            if (line.quantity != null) Text("Ilość: ${line.quantity} ${line.unit.orEmpty()}")
            if (line.unitPrice != null) Text("Cena jedn.: ${line.unitPrice}")
        }
    }
}

@Composable
private fun HistoryTab(state: AppUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(state.history, key = { it.id }) { quote ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(quote.name)
                    Text("${quote.widthMm}x${quote.heightMm}x${quote.depthMm} mm")
                    Text("Netto: ${quote.totalNet} | Brutto: ${quote.totalGross}")
                }
            }
        }
    }
}
