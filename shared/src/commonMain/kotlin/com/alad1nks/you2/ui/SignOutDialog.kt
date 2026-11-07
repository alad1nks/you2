package com.alad1nks.you2.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alad1nks.you2.feature.designsystem.theme.AppShapes
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.sign_out
import com.alad1nks.you2.resources.generated.sign_out_body
import com.alad1nks.you2.resources.generated.sign_out_saved
import com.alad1nks.you2.resources.generated.sign_out_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SignOutDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            Modifier.padding(AppSpacing.page).widthIn(max = 480.dp).fillMaxWidth().testTag("sign-out-dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(AppSpacing.page),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.large),
            ) {
                Text(stringResource(Res.string.sign_out_title), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(Res.string.sign_out_body), color = MaterialTheme.colorScheme.secondary)
                Text(
                    stringResource(Res.string.sign_out_saved),
                    Modifier.heightIn(min = 32.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                    OutlinedButton(
                        onDismiss,
                        Modifier.weight(1f).heightIn(min = 48.dp).testTag("sign-out-cancel"),
                        shape = AppShapes.pill,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    ) { Text(stringResource(Res.string.cancel)) }
                    Button(
                        onConfirm,
                        Modifier.weight(1f).heightIn(min = 48.dp).testTag("sign-out-confirm"),
                        shape = AppShapes.pill,
                    ) { Text(stringResource(Res.string.sign_out)) }
                }
            }
        }
    }
}
