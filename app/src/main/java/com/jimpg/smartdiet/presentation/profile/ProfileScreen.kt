package com.jimpg.smartdiet.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jimpg.smartdiet.SmartDietApp
import com.jimpg.smartdiet.data.repository.MealRepositoryImpl

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    application: SmartDietApp,
    onNavigateBack: () -> Unit
) {
    val repository = MealRepositoryImpl(
        application.database.mealDao(),
        application.database.userProfileDao()
    )
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(repository)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Το Προφίλ μου") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nickname
            OutlinedTextField(
                value = uiState.nickname,
                onValueChange = viewModel::onNicknameChange,
                label = { Text("Όνομα / Ψευδώνυμο") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Φύλο:", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.selectable(
                        selected = uiState.gender == "Male",
                        onClick = { viewModel.onGenderChange("Male") }
                    )
                ) {
                    RadioButton(
                        selected = uiState.gender == "Male",
                        onClick = { viewModel.onGenderChange("Male") }
                    )
                    Text("Άνδρας")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.selectable(
                        selected = uiState.gender == "Female",
                        onClick = { viewModel.onGenderChange("Female") }
                    )
                ) {
                    RadioButton(
                        selected = uiState.gender == "Female",
                        onClick = { viewModel.onGenderChange("Female") }
                    )
                    Text("Γυναίκα")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row: Weight, Height, Age
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedTextField(
                    value = uiState.weight,
                    onValueChange = viewModel::onWeightChange,
                    label = { Text("Βάρος (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                )

                OutlinedTextField(
                    value = uiState.height,
                    onValueChange = viewModel::onHeightChange,
                    label = { Text("Ύψος (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )

                OutlinedTextField(
                    value = uiState.age,
                    onValueChange = viewModel::onAgeChange,
                    label = { Text("Ηλικία") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity Level Dropdown
            var expanded by remember { mutableStateOf(false) }
            // Find key for current value
            val currentActivityLabel = viewModel.activityLevels.entries.find { it.value == uiState.activityLevel }?.key ?: "Επιλέξτε δραστηριότητα"

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = currentActivityLabel,
                    onValueChange = { },
                    label = { Text("Επίπεδο Δραστηριότητας") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    viewModel.activityLevels.forEach { (label, value) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.onActivityLevelChange(value)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Results: BMI & Calorie Target
            if (uiState.bmi > 0) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Δείκτης Μάζας Σώματος (BMI): ${String.format("%.1f", uiState.bmi)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ημερήσιος Στόχος (Απώλεια): ${uiState.calorieTarget} kcal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "(Συντήρηση - 500 θερμίδες)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = "Θέματα Υγείας / Διατροφικές Προτιμήσεις",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Custom Condition Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.customConditionInput,
                    onValueChange = viewModel::onCustomConditionInputChange,
                    label = { Text("Προσθήκη άλλου (π.χ. Χασιμότο)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = viewModel::addCustomCondition,
                    enabled = uiState.customConditionInput.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Condition")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Common Suggestions
            Text(
                text = "Συχνές επιλογές:",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                viewModel.availableConditions.forEach { condition ->
                    val isSelected = uiState.selectedConditions.contains(condition)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleCondition(condition) },
                        label = { Text(condition) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Close, contentDescription = "Remove") }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // All Selected Conditions Display
            if (uiState.selectedConditions.isNotEmpty()) {
                 Text(
                    text = "Επιλεγμένα:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.selectedConditions.forEach { condition ->
                         InputChip(
                            selected = true,
                            onClick = { viewModel.removeCondition(condition) },
                            label = { Text(condition) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::saveProfile,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.nickname.isNotBlank() && uiState.weight.isNotBlank() && uiState.height.isNotBlank() && uiState.age.isNotBlank()
            ) {
                Text("Αποθήκευση Προφίλ")
            }
        }
    }
}
