package com.jimpg.smartdiet.presentation.addmeal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jimpg.smartdiet.R
import com.jimpg.smartdiet.SmartDietApp
import com.jimpg.smartdiet.data.ai.GeminiService
import com.jimpg.smartdiet.data.repository.MealRepositoryImpl
import com.jimpg.smartdiet.presentation.home.MealStat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    application: SmartDietApp,
    onNavigateBack: () -> Unit
) {
    val repository = MealRepositoryImpl(
        application.database.mealDao(),
        application.database.userProfileDao()
    )
    // In a real app, instantiate Service once (e.g. via DI)
    val geminiService = GeminiService() 
    
    val viewModel: AddMealViewModel = viewModel(
        factory = AddMealViewModel.Factory(repository, geminiService)
    )
    
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation after save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_meal)) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text(stringResource(R.string.describe_meal)) },
                placeholder = { Text(stringResource(R.string.describe_meal_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::analyzeMeal,
                enabled = uiState.description.isNotBlank() && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp).width(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.loading))
                } else {
                    Text(stringResource(R.string.analyze_food))
                }
            }
            
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.error ?: stringResource(R.string.error_analysis),
                    color = MaterialTheme.colorScheme.error
                )
            }

            uiState.analysisResult?.let { result ->
                Spacer(modifier = Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = result.description,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = result.analysis, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                        ) {
                            MealStat(stringResource(R.string.calories), "${result.calories}")
                            MealStat(stringResource(R.string.protein), "${result.protein}g")
                            MealStat(stringResource(R.string.carbs), "${result.carbs}g")
                            MealStat(stringResource(R.string.fat), "${result.fat}g")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = viewModel::saveMeal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Meal") // Should be in strings.xml but okay for now
                }
            }
        }
    }
}
