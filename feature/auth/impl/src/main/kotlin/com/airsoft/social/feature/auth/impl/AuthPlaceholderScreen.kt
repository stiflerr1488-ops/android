package com.airsoft.social.feature.auth.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airsoft.social.core.auth.AuthResult
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.feature.auth.api.AuthFeatureApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val AUTH_GUEST_BUTTON_TAG = "auth_guest_button"

data class AuthUiState(
    val callsign: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isGuestSession: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface AuthAction {
    data class CallsignChanged(val value: String) : AuthAction
    data class EmailChanged(val value: String) : AuthAction
    data class PasswordChanged(val value: String) : AuthAction
    data object SignInClicked : AuthAction
    data object RegisterClicked : AuthAction
    data object SignInGuestClicked : AuthAction
    data object UpgradeGuestClicked : AuthAction
}

sealed interface AuthEffect {
    data object Authorized : AuthEffect
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AuthEffect>()
    val effects = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            sessionRepository.authState.collect { authState ->
                val isGuest = (authState as? AuthState.SignedIn)?.session?.isGuest == true
                _uiState.update { it.copy(isGuestSession = isGuest) }
                if (authState is AuthState.SignedIn) {
                    _effects.emit(AuthEffect.Authorized)
                }
            }
        }
    }

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.CallsignChanged -> _uiState.update { it.copy(callsign = action.value) }
            is AuthAction.EmailChanged -> _uiState.update { it.copy(email = action.value) }
            is AuthAction.PasswordChanged -> _uiState.update { it.copy(password = action.value) }
            AuthAction.SignInClicked -> signInWithEmail()
            AuthAction.RegisterClicked -> registerWithEmail()
            AuthAction.SignInGuestClicked -> signInGuest()
            AuthAction.UpgradeGuestClicked -> upgradeGuest()
        }
    }

    private fun signInWithEmail() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Введите email и пароль") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = sessionRepository.signInWithEmail(state.email, state.password)
            _uiState.update { it.copy(isLoading = false) }
            consumeResult(result)
        }
    }

    private fun registerWithEmail() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Введите email и пароль") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = sessionRepository.registerWithEmail(
                email = state.email,
                password = state.password,
                callsign = state.callsign.ifBlank { null },
            )
            _uiState.update { it.copy(isLoading = false) }
            consumeResult(result)
        }
    }

    private fun signInGuest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = sessionRepository.signInGuest(callsign = _uiState.value.callsign.ifBlank { null })
            _uiState.update { it.copy(isLoading = false) }
            consumeResult(result)
        }
    }

    private fun upgradeGuest() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Введите email и пароль для апгрейда") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = sessionRepository.upgradeGuestToEmail(
                email = state.email,
                password = state.password,
                callsign = state.callsign.ifBlank { null },
            )
            _uiState.update { it.copy(isLoading = false) }
            consumeResult(result)
        }
    }

    private fun consumeResult(result: AuthResult) {
        if (result is AuthResult.Failure) {
            _uiState.update { it.copy(errorMessage = result.reason) }
        }
    }
}

@Composable
fun AuthPlaceholderScreen(
    onAuthenticated: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.effects.collect { onAuthenticated() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = AuthFeatureApi.contract.title,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Email / guest auth",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = uiState.callsign,
            onValueChange = { authViewModel.onAction(AuthAction.CallsignChanged(it)) },
            label = { Text("Позывной") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { authViewModel.onAction(AuthAction.EmailChanged(it)) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { authViewModel.onAction(AuthAction.PasswordChanged(it)) },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )

        Button(
            onClick = { authViewModel.onAction(AuthAction.SignInClicked) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
        ) {
            Text("Войти")
        }
        Button(
            onClick = { authViewModel.onAction(AuthAction.RegisterClicked) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
        ) {
            Text("Создать аккаунт")
        }

        if (uiState.isGuestSession) {
            OutlinedButton(
                onClick = { authViewModel.onAction(AuthAction.UpgradeGuestClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
            ) {
                Text("Апгрейд guest -> email")
            }
        } else {
            OutlinedButton(
                onClick = { authViewModel.onAction(AuthAction.SignInGuestClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(AUTH_GUEST_BUTTON_TAG),
                enabled = !uiState.isLoading,
            ) {
                Text("Продолжить как гость")
            }
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
