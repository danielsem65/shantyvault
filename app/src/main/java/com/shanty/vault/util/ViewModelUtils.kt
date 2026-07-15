package com.shanty.vault.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shanty.vault.ShantyVaultApp

@Composable
inline fun <reified T : ViewModel> appViewModel(): T {
    val factory = (LocalContext.current.applicationContext as ShantyVaultApp).viewModelFactory
    return viewModel(factory = factory)
}
