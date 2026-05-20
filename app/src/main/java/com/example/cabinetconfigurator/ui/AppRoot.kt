package com.example.cabinetconfigurator.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter
import com.example.cabinetconfigurator.domain.engine.CalculationKeys
import com.example.cabinetconfigurator.domain.model.Accessory
import com.example.cabinetconfigurator.domain.model.AccessoryCatalog
import com.example.cabinetconfigurator.domain.model.AccessoryType
import com.example.cabinetconfigurator.domain.model.CabinetType
import com.example.cabinetconfigurator.domain.model.CalculationLine
import com.example.cabinetconfigurator.domain.model.ElementType
import com.example.cabinetconfigurator.domain.model.FurnitureElement
import com.example.cabinetconfigurator.domain.model.FurniturePiece
import com.example.cabinetconfigurator.domain.model.Quote
import com.example.cabinetconfigurator.ui.viewmodel.AppTab
import com.example.cabinetconfigurator.ui.viewmodel.AppUiState
import com.example.cabinetconfigurator.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(vm: AppViewModel) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.generatedPdfFile) {
        state.generatedPdfFile?.let { file ->
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Otwórz PDF"))
            vm.clearGeneratedPdf()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cabinet Configurator") },
                actions = {
                    IconButton(onClick = vm::openSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ustawienia")
                    }
                }
            )
        }
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
                AppTab.PRICING      -> PricingTab(state, vm)
                AppTab.CONFIGURATOR -> ConfiguratorTab(state, vm)
                AppTab.HISTORY      -> HistoryTab(state, vm)
            }
        }

        if (state.isSettingsOpen) {
            SettingsDialog(state, vm)
        }
    }
}

@Composable
private fun PricingTab(state: AppUiState, vm: AppViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { Text("Profil: ${state.profileName}") }
            items(state.pricingFields, key = { it.key }) { field ->
                OutlinedTextField(
                    value = field.value,
                    onValueChange = { vm.updatePricingField(field.key, it) },
                    label = { Text(field.label) },
                    supportingText = { Text(field.unit ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Button(onClick = vm::openAccessoryPriceCatalog, modifier = Modifier.fillMaxWidth()) {
                    Text("Edytuj ceny akcesoriów")
                }
            }
        }

        if (state.isAccessoryPriceCatalogOpen) AccessoryPriceCatalogDialog(state, vm)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AccessoryPriceCatalogDialog(state: AppUiState, vm: AppViewModel) {
    val manufacturers = AccessoryCatalog.manufacturers
    val pagerState = rememberPagerState(pageCount = { manufacturers.size })
    val scope = rememberCoroutineScope()
    val priceMap = state.accessoryPriceFields.associate { it.key to it.value }

    Dialog(onDismissRequest = vm::closeAccessoryPriceCatalog) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ceny akcesoriów", style = MaterialTheme.typography.titleMedium)

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    manufacturers.forEachIndexed { i, mfr ->
                        FilterChip(
                            selected = pagerState.currentPage == i,
                            onClick = { scope.launch { pagerState.animateScrollToPage(i) } },
                            label = { Text(mfr) }
                        )
                    }
                }

                HorizontalDivider()

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(360.dp)
                ) { page ->
                    val mfr = manufacturers[page]
                    val grouped = AccessoryCatalog.entriesFor(mfr).groupBy { it.type }
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        grouped.forEach { (type, entries) ->
                            Text(type.displayName, style = MaterialTheme.typography.titleSmall)
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    entries.forEach { entry ->
                                        val key = CalculationKeys.accessoryPriceKey(mfr, entry.model)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(entry.model, modifier = Modifier.weight(1f))
                                            OutlinedTextField(
                                                value = priceMap[key] ?: "0",
                                                onValueChange = { vm.updatePricingField(key, it) },
                                                label = { Text("zł/szt") },
                                                modifier = Modifier.width(120.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Button(onClick = vm::closeAccessoryPriceCatalog, modifier = Modifier.fillMaxWidth()) {
                    Text("Zamknij")
                }
            }
        }
    }
}

@Composable
private fun ConfiguratorTab(state: AppUiState, vm: AppViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (state.editingQuoteId != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            "Edytujesz wycenę - zapisanie nadpisze ją",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
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
                Button(onClick = vm::openAddFurniture, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, contentDescription = null)
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
            item { state.message?.let { Text(it) } }
        }

        if (state.isAddFurnitureOpen) AddFurnitureDialog(state, vm)
        if (state.isTemplatePickerOpen) TemplatePickerDialog(state, vm)
        if (state.isTemplateEditorOpen) TemplateEditorDialog(state, vm)
    }
}

@Composable
private fun AddFurnitureDialog(state: AppUiState, vm: AppViewModel) {
    Dialog(onDismissRequest = vm::closeAddFurniture) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Dodaj mebel", style = MaterialTheme.typography.titleMedium)
                Button(onClick = vm::openTemplatePicker, modifier = Modifier.fillMaxWidth()) {
                    Text("Gotowy mebel")
                }
                Button(onClick = vm::openTemplateEditor, modifier = Modifier.fillMaxWidth()) {
                    Text("Nowy szablon")
                }
                Button(
                    onClick = { vm.addFurniturePiece(); vm.closeAddFurniture() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Pusty mebel") }
                Button(onClick = vm::closeAddFurniture, modifier = Modifier.fillMaxWidth()) {
                    Text("Anuluj")
                }
            }
        }
    }
}

@Composable
private fun TemplatePickerDialog(state: AppUiState, vm: AppViewModel) {
    Dialog(onDismissRequest = vm::closeTemplatePicker) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Wybierz gotowy mebel", style = MaterialTheme.typography.titleMedium)
                if (state.templateCatalog.isEmpty()) {
                    Text("Brak zapisanych szablonów. Dodaj nowy szablon.")
                } else {
                    Box(modifier = Modifier.heightIn(max = 400.dp)) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            state.templateCatalog.groupBy { it.cabinetType }.forEach { (type, templates) ->
                                Text(type, style = MaterialTheme.typography.titleSmall)
                                templates.forEach { template ->
                                    Button(
                                        onClick = { vm.addFurniturePieceFromTemplate(template.id) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text(template.name) }
                                }
                            }
                        }
                    }
                }
                Button(onClick = vm::closeTemplatePicker, modifier = Modifier.fillMaxWidth()) {
                    Text("Anuluj")
                }
            }
        }
    }
}

@Composable
private fun TemplateEditorDialog(state: AppUiState, vm: AppViewModel) {
    Dialog(onDismissRequest = vm::closeTemplateEditor) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nowy szablon mebla", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = state.templateEditorState.name,
                    onValueChange = vm::updateTemplateEditorName,
                    label = { Text("Nazwa szablonu") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.templateEditorState.cabinetType,
                        onValueChange = vm::updateTemplateEditorCabinetType,
                        label = { Text("Typ szafy") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.templateEditorState.widthMm,
                        onValueChange = vm::updateTemplateEditorWidth,
                        label = { Text("Szer.") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.templateEditorState.heightMm,
                        onValueChange = vm::updateTemplateEditorHeight,
                        label = { Text("Wys.") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.templateEditorState.depthMm,
                        onValueChange = vm::updateTemplateEditorDepth,
                        label = { Text("Głęb.") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Elementy szablonu:")
                state.templateEditorState.elements.forEach { element ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(element.type.displayName, modifier = Modifier.weight(1f))
                                OutlinedTextField(
                                    value = element.quantity.toString(),
                                    onValueChange = { vm.updateTemplateEditorElementQuantity(element.id, it.toIntOrNull() ?: 1) },
                                    label = { Text("Ilość") },
                                    modifier = Modifier.width(80.dp)
                                )
                                IconButton(onClick = { vm.removeTemplateEditorElement(element.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Usuń element")
                                }
                            }
                            AccessoryListSection(
                                accessories = element.accessories,
                                allowedTypes = AccessoryCatalog.allowedAccessoryTypesFor(element.type),
                                onAddAccessory = { type -> vm.addTemplateAccessory(element.id, type) },
                                onRemoveAccessory = { accId -> vm.removeTemplateAccessory(element.id, accId) },
                                onQuantityChange = { accId, qty -> vm.updateTemplateAccessoryQuantity(element.id, accId, qty) },
                                onManufacturerChange = { accId, mfr -> vm.updateTemplateAccessoryManufacturer(element.id, accId, mfr) },
                                onModelChange = { accId, mdl -> vm.updateTemplateAccessoryModel(element.id, accId, mdl) }
                            )
                        }
                    }
                }
                ElementTypePickerRow { type -> vm.addTemplateEditorElement(type) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = vm::saveTemplate, modifier = Modifier.weight(1f)) {
                        Text("Zapisz szablon")
                    }
                    Button(onClick = vm::closeTemplateEditor, modifier = Modifier.weight(1f)) {
                        Text("Anuluj")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FurniturePieceCard(piece: FurniturePiece, vm: AppViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mebel", modifier = Modifier.weight(1f))
                IconButton(onClick = { vm.removeFurniturePiece(piece.id) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Usuń")
                }
            }
            CabinetTypeDropdown(
                value = piece.cabinetType,
                onSelect = { vm.updateFurnitureCabinetType(piece.id, it) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
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
            Text("Strefy:")
            piece.elements.forEach { element ->
                FurnitureElementRow(piece, element, vm)
            }
            ElementTypePickerRow { type -> vm.addElementToFurniture(piece.id, type) }
        }
    }
}

@Composable
private fun FurnitureElementRow(piece: FurniturePiece, element: FurnitureElement, vm: AppViewModel) {
    val pieceId = piece.id
    val isFront = element.type == ElementType.FRONT
    val hasCustomHeight = element.heightMm != null

    val autoHeight = if (isFront) {
        val frontsCount = piece.elements.filter { it.type == ElementType.FRONT }.sumOf { it.quantity }
        if (frontsCount > 0) piece.heightMm / frontsCount else piece.heightMm
    } else null

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(element.type.displayName, modifier = Modifier.weight(1f))
            OutlinedTextField(
                value = element.quantity.toString(),
                onValueChange = { vm.updateElementQuantity(pieceId, element.id, it.toIntOrNull() ?: 1) },
                label = { Text("Ilość") },
                modifier = Modifier.width(80.dp)
            )
            IconButton(onClick = { vm.removeElementFromFurniture(pieceId, element.id) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Usuń element")
            }
        }

        if (isFront) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = hasCustomHeight,
                    onCheckedChange = { vm.toggleElementCustomHeight(pieceId, element.id, it) }
                )
                Text("Własna wysokość", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(8.dp))
                if (hasCustomHeight) {
                    OutlinedTextField(
                        value = (element.heightMm ?: 0).toString(),
                        onValueChange = { vm.updateElementHeight(pieceId, element.id, it.toIntOrNull()) },
                        label = { Text("Wys. mm") },
                        modifier = Modifier.width(100.dp)
                    )
                } else {
                    Text(
                        "Auto: ${autoHeight ?: "-"} mm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        AccessoryListSection(
            accessories = element.accessories,
            allowedTypes = AccessoryCatalog.allowedAccessoryTypesFor(element.type),
            onAddAccessory = { type -> vm.addAccessory(pieceId, element.id, type) },
            onRemoveAccessory = { accId -> vm.removeAccessory(pieceId, element.id, accId) },
            onQuantityChange = { accId, qty -> vm.updateAccessoryQuantity(pieceId, element.id, accId, qty) },
            onManufacturerChange = { accId, mfr -> vm.updateAccessoryManufacturer(pieceId, element.id, accId, mfr) },
            onModelChange = { accId, mdl -> vm.updateAccessoryModel(pieceId, element.id, accId, mdl) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccessoryListSection(
    accessories: List<Accessory>,
    allowedTypes: List<AccessoryType>,
    onAddAccessory: (AccessoryType) -> Unit,
    onRemoveAccessory: (String) -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    onManufacturerChange: (String, String) -> Unit,
    onModelChange: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (accessories.isNotEmpty()) {
            Text("Elementy dodatkowe:", style = MaterialTheme.typography.labelMedium)
        }
        accessories.forEach { acc ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(acc.type.displayName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(
                            value = acc.quantity.toString(),
                            onValueChange = { onQuantityChange(acc.id, it.toIntOrNull() ?: 1) },
                            label = { Text("Ilość") },
                            modifier = Modifier.width(80.dp)
                        )
                        IconButton(onClick = { onRemoveAccessory(acc.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Usuń akcesorium")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ManufacturerDropdown(
                            value = acc.manufacturer,
                            onSelect = { onManufacturerChange(acc.id, it) },
                            modifier = Modifier.weight(1f)
                        )
                        ModelDropdown(
                            manufacturer = acc.manufacturer,
                            accessoryType = acc.type,
                            value = acc.model,
                            onSelect = { onModelChange(acc.id, it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        AccessoryTypePickerRow(allowedTypes = allowedTypes, onAdd = onAddAccessory)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccessoryTypePickerRow(allowedTypes: List<AccessoryType>, onAdd: (AccessoryType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.menuAnchor()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Dodaj element dodatkowy")
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            allowedTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.displayName) },
                    onClick = { onAdd(type); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ElementTypePickerRow(onAdd: (ElementType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.menuAnchor()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Dodaj strefę")
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ElementType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.displayName) },
                    onClick = { onAdd(type); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CabinetTypeDropdown(value: CabinetType, onSelect: (CabinetType) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = value.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Typ szafki") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CabinetType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.displayName) },
                    onClick = { onSelect(type); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManufacturerDropdown(value: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val display = value.ifBlank { "Producent" }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            label = { Text("Producent") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AccessoryCatalog.manufacturers.forEach { mfr ->
                DropdownMenuItem(
                    text = { Text(mfr) },
                    onClick = { onSelect(mfr); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelDropdown(manufacturer: String, accessoryType: AccessoryType, value: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val models = AccessoryCatalog.entriesFor(manufacturer)
        .filter { it.type == accessoryType }
        .map { it.model }
    val display = value.ifBlank { "Model" }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            label = { Text("Model") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            models.forEach { mdl ->
                DropdownMenuItem(
                    text = { Text(mdl) },
                    onClick = { onSelect(mdl); expanded = false }
                )
            }
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
private fun HistoryTab(state: AppUiState, vm: AppViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(state.history, key = { it.id }) { quote ->
            QuoteHistoryCard(quote, state, vm)
        }
    }
}

@Composable
private fun QuoteHistoryCard(quote: Quote, state: AppUiState, vm: AppViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(quote.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("${quote.widthMm}x${quote.heightMm}x${quote.depthMm} mm", style = MaterialTheme.typography.labelSmall)
            Text("Netto: ${quote.totalNet} | Brutto: ${quote.totalGross}")
            if (quote.furniture.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Meble: ${quote.furniture.size}", style = MaterialTheme.typography.labelSmall)
                quote.furniture.forEach { piece ->
                    Text(
                        "  - ${piece.widthMm}x${piece.heightMm}x${piece.depthMm} mm (${piece.elements.size} elem.)",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { vm.loadQuoteForEditing(quote) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edytuj")
                }
                OutlinedButton(
                    onClick = { vm.exportQuoteToPdf(quote) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isGeneratingPdf
                ) {
                    if (state.isGeneratingPdf) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("PDF")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsDialog(state: AppUiState, vm: AppViewModel) {
    val context = LocalContext.current
    var companyName by remember { mutableStateOf(state.userSettings.companyName) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some providers don't support persistable permissions
            }
            vm.updateLogoUri(it)
        }
    }

    Dialog(onDismissRequest = vm::closeSettings) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Ustawienia", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Nazwa firmy") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Logo firmy", style = MaterialTheme.typography.titleSmall)

                state.userSettings.logoUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Logo firmy",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { imagePickerLauncher.launch(arrayOf("image/*")) },
                        contentScale = ContentScale.Fit
                    )
                    TextButton(onClick = { vm.updateLogoUri(null) }) {
                        Text("Usuń logo")
                    }
                } ?: run {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Wybierz logo")
                    }
                }

                HorizontalDivider()

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = vm::closeSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Anuluj")
                    }
                    Button(
                        onClick = {
                            vm.updateCompanyName(companyName)
                            vm.closeSettings()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Zapisz")
                    }
                }
            }
        }
    }
}
