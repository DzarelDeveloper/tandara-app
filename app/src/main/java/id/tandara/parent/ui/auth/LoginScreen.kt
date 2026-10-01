package id.tandara.parent.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.core.designsystem.AccentBlue
import id.tandara.parent.core.designsystem.AppBackground
import id.tandara.parent.core.designsystem.BorderColor
import id.tandara.parent.core.designsystem.PrimaryBlue
import id.tandara.parent.core.designsystem.PrimarySurface
import id.tandara.parent.core.designsystem.PrimaryText
import id.tandara.parent.core.designsystem.SecondaryText
import id.tandara.parent.core.designsystem.SuccessGreen
import id.tandara.parent.core.designsystem.SurfaceWhite
import id.tandara.parent.core.network.ServerConfig
import id.tandara.parent.ui.components.TandaraButton
import id.tandara.parent.ui.components.TandaraLogo
import id.tandara.parent.ui.components.TandaraOutlinedButton
import id.tandara.parent.ui.components.TandaraTextField
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.serverChangeNoticeMessage) {
        val message = uiState.serverChangeNoticeMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            delay(4000)
            viewModel.clearServerChangeNotice()
        }
    }

    if (uiState.showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissForgotPasswordDialog() },
            title = {
                Text(
                    text = "Lupa Kata Sandi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
            },
            text = {
                Text(
                    text = "Silakan hubungi bagian Tata Usaha sekolah atau wali kelas untuk verifikasi dan pemulihan akses akun orang tua.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.dismissForgotPasswordDialog() },
                    modifier = Modifier.testTag("dialog_ok_button")
                ) {
                    Text(
                        text = "Mengerti",
                        color = AccentBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = PrimarySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (uiState.showServerSettingsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissServerSettingsDialog() },
            title = {
                Text(
                    text = "Pengaturan Server",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TandaraTextField(
                        value = uiState.serverHostInput,
                        onValueChange = viewModel::onServerHostChanged,
                        label = "Alamat Server",
                        placeholder = "contoh: 192.168.110.101",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        errorMessage = uiState.serverHostError,
                        testTag = "input_server_host"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TandaraTextField(
                        value = uiState.serverPortInput,
                        onValueChange = viewModel::onServerPortChanged,
                        label = "Port",
                        placeholder = "contoh: 8000",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        errorMessage = uiState.serverPortError,
                        testTag = "input_server_port"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.serverHostInput.isNotBlank()) {
                        val portDisplay = uiState.serverPortInput.trim().ifBlank { ServerConfig.DEFAULT_PORT.toString() }
                        Text(
                            text = "Endpoint: http://${uiState.serverHostInput.trim()}:$portDisplay",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (uiState.serverConnectionTestState) {
                        ConnectionTestState.TESTING -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = PrimaryBlue,
                                    trackColor = PrimarySurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = uiState.serverConnectionTestMessage ?: "Memeriksa koneksi...",
                                    color = SecondaryText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        ConnectionTestState.SUCCESS -> {
                            Text(
                                text = uiState.serverConnectionTestMessage ?: "✓ Server Tandara terhubung",
                                color = SuccessGreen,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        ConnectionTestState.FAILURE -> {
                            Text(
                                text = uiState.serverConnectionTestMessage ?: "Tidak dapat terhubung ke server.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        ConnectionTestState.IDLE -> Unit
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissServerSettingsDialog() }
                ) {
                    Text(text = "Batal", color = SecondaryText)
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TandaraOutlinedButton(
                        onClick = viewModel::testConnection,
                        enabled = uiState.serverConnectionTestState != ConnectionTestState.TESTING && !uiState.serverSaveLoading,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(text = "Tes Koneksi", fontSize = 13.sp)
                    }
                    TandaraButton(
                        onClick = viewModel::saveServer,
                        isLoading = uiState.serverSaveLoading,
                        enabled = uiState.serverConnectionTestState != ConnectionTestState.TESTING,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(text = "Simpan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            containerColor = PrimarySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { /* single tap no-op */ },
                        onDoubleTap = { viewModel.onLogoDoubleTap() }
                    )
                }
            ) {
                TandaraLogo(size = 64.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tandara",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Akses Orang Tua/Wali • SMK Taman Harapan",
                fontSize = 13.5.sp,
                color = SecondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimarySurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(22.dp)
            ) {
                Column {
                    TandaraTextField(
                        value = uiState.phoneNumber,
                        onValueChange = viewModel::onPhoneNumberChanged,
                        label = "Username",
                        placeholder = "Masukkan username dari sekolah",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Username",
                                tint = SecondaryText
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        testTag = "input_username"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TandaraTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = "Kata Sandi",
                        placeholder = "Masukkan kata sandi",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Kata Sandi",
                                tint = SecondaryText
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = viewModel::togglePasswordVisibility,
                                modifier = Modifier.testTag("button_toggle_password")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPasswordVisible) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = if (uiState.isPasswordVisible) {
                                        "Sembunyikan kata sandi"
                                    } else {
                                        "Tampilkan kata sandi"
                                    },
                                    tint = SecondaryText
                                )
                            }
                        },
                        visualTransformation = if (uiState.isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login()
                            }
                        ),
                        errorMessage = uiState.errorMessage,
                        testTag = "input_password"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = viewModel::showForgotPasswordDialog,
                            modifier = Modifier.testTag("button_forgot_password")
                        ) {
                            Text(
                                text = "Lupa kata sandi?",
                                color = AccentBlue,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    TandaraButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login()
                        },
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "button_login"
                    ) {
                        Text(
                            text = "Masuk",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Akses khusus orang tua/wali terdaftar.",
                    fontSize = 12.5.sp,
                    color = SecondaryText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Hubungi tata usaha sekolah jika memerlukan bantuan akses.",
                    fontSize = 11.5.sp,
                    color = SecondaryText.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = PrimarySurface,
                contentColor = PrimaryText,
                actionColor = AccentBlue,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
