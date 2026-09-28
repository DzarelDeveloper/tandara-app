package id.tandara.parent.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tandara.parent.core.designsystem.AccentBlue
import id.tandara.parent.core.designsystem.AppBackground
import id.tandara.parent.core.designsystem.BorderColor
import id.tandara.parent.core.designsystem.DividerColor
import id.tandara.parent.core.designsystem.ElevatedSurface
import id.tandara.parent.core.designsystem.ErrorRed
import id.tandara.parent.core.designsystem.PrimaryBlue
import id.tandara.parent.core.designsystem.PrimaryBlueLight
import id.tandara.parent.core.designsystem.PrimarySurface
import id.tandara.parent.core.designsystem.PrimaryText
import id.tandara.parent.core.designsystem.SecondarySurface
import id.tandara.parent.core.designsystem.SecondaryText
import id.tandara.parent.core.designsystem.SuccessGreen
import id.tandara.parent.ui.components.TandaraButton
import id.tandara.parent.ui.components.TandaraTextField
import id.tandara.parent.ui.components.TandaraTopAppBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Change Password Screen (UI/UX V3 - Dark Navy Theme).
 */
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    val hasMinLength = newPassword.length >= 8
    val hasUpperAndLower = newPassword.any { it.isUpperCase() } && newPassword.any { it.isLowerCase() }
    val hasDigitOrSpecial = newPassword.any { it.isDigit() || !it.isLetterOrDigit() }
    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword

    val strengthScore = listOf(hasMinLength, hasUpperAndLower, hasDigitOrSpecial).count { it }
    val (strengthLabel, strengthColor, progress) = when {
        newPassword.isEmpty() -> Triple("Masukkan kata sandi", SecondaryText, 0f)
        strengthScore == 1 -> Triple("Lemah", ErrorRed, 0.33f)
        strengthScore == 2 -> Triple("Cukup", Color(0xFFF5B942), 0.66f)
        else -> Triple("Kuat", SuccessGreen, 1f)
    }

    val isFormValid = currentPassword.isNotBlank() && hasMinLength && passwordsMatch

    Scaffold(
        topBar = {
            TandaraTopAppBar(
                title = "Ubah Kata Sandi",
                subtitle = "Kelola keamanan akses akun Anda",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = AppBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 20.dp)
                .testTag("change_password_screen_content")
        ) {
            // Notice Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBlueLight)
                    .border(1.dp, AccentBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Gunakan kombinasi minimal 8 karakter dengan huruf besar, angka, atau simbol untuk keamanan maksimal akun Anda.",
                        fontSize = 12.5.sp,
                        color = PrimaryText,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Current Password Field
            TandaraTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = "Kata Sandi Saat Ini",
                placeholder = "Masukkan kata sandi lama",
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = SecondaryText)
                },
                trailingIcon = {
                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                        Icon(
                            imageVector = if (showCurrentPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (showCurrentPassword) "Sembunyikan" else "Tampilkan",
                            tint = SecondaryText
                        )
                    }
                },
                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                testTag = "input_current_password"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // New Password Field
            TandaraTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Kata Sandi Baru",
                placeholder = "Minimal 8 karakter",
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = SecondaryText)
                },
                trailingIcon = {
                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                        Icon(
                            imageVector = if (showNewPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (showNewPassword) "Sembunyikan" else "Tampilkan",
                            tint = SecondaryText
                        )
                    }
                },
                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                testTag = "input_new_password"
            )

            // Password Strength Indicator
            if (newPassword.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimarySurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kekuatan Sandi:",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                        Text(
                            text = strengthLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = strengthColor
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        color = strengthColor,
                        trackColor = SecondarySurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PasswordRequirementRow(label = "Minimal 8 karakter", satisfied = hasMinLength)
                    PasswordRequirementRow(label = "Kombinasi huruf besar & kecil", satisfied = hasUpperAndLower)
                    PasswordRequirementRow(label = "Terdapat angka atau simbol", satisfied = hasDigitOrSpecial)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            TandaraTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Konfirmasi Kata Sandi Baru",
                placeholder = "Ulangi kata sandi baru",
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = SecondaryText)
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (showConfirmPassword) "Sembunyikan" else "Tampilkan",
                            tint = SecondaryText
                        )
                    }
                },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                testTag = "input_confirm_password"
            )

            if (confirmPassword.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (passwordsMatch) Icons.Outlined.Check else Icons.Outlined.Close,
                        contentDescription = null,
                        tint = if (passwordsMatch) SuccessGreen else ErrorRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (passwordsMatch) "Kata sandi cocok" else "Kata sandi tidak cocok",
                        fontSize = 12.sp,
                        color = if (passwordsMatch) SuccessGreen else ErrorRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            TandaraButton(
                onClick = {
                    scope.launch {
                        isLoading = true
                        delay(600)
                        isLoading = false
                        snackbarHostState.showSnackbar("Kata sandi berhasil diperbarui!")
                        delay(500)
                        onBackClick()
                    }
                },
                enabled = isFormValid,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth(),
                testTag = "button_save_password"
            ) {
                Text(
                    text = "Simpan Kata Sandi",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PasswordRequirementRow(label: String, satisfied: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (satisfied) Icons.Outlined.Check else Icons.Outlined.Close,
            contentDescription = null,
            tint = if (satisfied) SuccessGreen else SecondaryText.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (satisfied) PrimaryText else SecondaryText
        )
    }
}
