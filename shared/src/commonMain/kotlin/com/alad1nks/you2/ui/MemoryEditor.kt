package com.alad1nks.you2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alad1nks.you2.feature.designsystem.components.AppTextField
import com.alad1nks.you2.feature.designsystem.components.PrimaryButton
import com.alad1nks.you2.feature.designsystem.theme.AppDimensions
import com.alad1nks.you2.feature.designsystem.theme.AppShapes
import com.alad1nks.you2.feature.designsystem.theme.AppSpacing
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.cancel
import com.alad1nks.you2.resources.generated.edit_memory
import com.alad1nks.you2.resources.generated.little_memory
import com.alad1nks.you2.resources.generated.memory_hint
import com.alad1nks.you2.resources.generated.save_changes
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoryEditor(wishId: String, initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var draft by rememberSaveable(wishId) { mutableStateOf(initial) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            Modifier.safeDrawingPadding().imePadding().padding(AppSpacing.page)
                .widthIn(max = AppDimensions.contentWidth).fillMaxWidth(),
            shape = AppShapes.card,
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.large),
            ) {
                Text(stringResource(Res.string.edit_memory), style = MaterialTheme.typography.headlineMedium)
                AppTextField(
                    draft,
                    { draft = it },
                    stringResource(Res.string.little_memory),
                    Modifier.testTag("memory-draft"),
                    maxLength = 500,
                    singleLine = false,
                    minLines = 4,
                    helperText = stringResource(Res.string.memory_hint),
                )
                PrimaryButton(stringResource(Res.string.save_changes), { onSave(draft) }, enabled = draft.length <= 500)
                TextButton(onDismiss, Modifier.fillMaxWidth()) { Text(stringResource(Res.string.cancel)) }
            }
        }
    }
}
