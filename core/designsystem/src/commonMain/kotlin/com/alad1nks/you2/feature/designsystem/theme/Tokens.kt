package com.alad1nks.you2.feature.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alad1nks.you2.resources.generated.Res
import com.alad1nks.you2.resources.generated.roboto_bold
import com.alad1nks.you2.resources.generated.roboto_medium
import com.alad1nks.you2.resources.generated.roboto_regular
import org.jetbrains.compose.resources.Font

object AppSpacing {
    val xs = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val page = 24.dp
    val section = 32.dp
}

object AppDimensions {
    val contentWidth = 412.dp
    val buttonHeight = 56.dp
    val icon = 24.dp
    val avatar = 44.dp
    val headerHeight = 64.dp
    val navigationHeight = 76.dp
}

object AppShapes {
    val field = RoundedCornerShape(16.dp)
    val card = RoundedCornerShape(18.dp)
    val image = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(50)
}

@Composable
internal fun appTypography(): Typography {
    val roboto =
        FontFamily(
            Font(Res.font.roboto_regular, FontWeight.Normal),
            Font(Res.font.roboto_medium, FontWeight.Medium),
            Font(Res.font.roboto_bold, FontWeight.Bold),
        )

    fun style(size: Int, height: Int, weight: FontWeight) =
        TextStyle(
            fontFamily = roboto,
            fontWeight = weight,
            fontSize = size.sp,
            lineHeight = height.sp,
            letterSpacing = 0.sp,
        )
    val title = style(32, 38, FontWeight.Bold)
    val heading = style(20, 26, FontWeight.Bold)
    val body = style(14, 20, FontWeight.Normal)
    val label = style(14, 20, FontWeight.Medium)
    val small = style(12, 16, FontWeight.Normal)
    return Typography(
        displayLarge = title,
        displayMedium = title,
        displaySmall = title,
        headlineLarge = title,
        headlineMedium = heading,
        headlineSmall = heading,
        titleLarge = heading,
        titleMedium = label,
        titleSmall = label,
        bodyLarge = body,
        bodyMedium = body,
        bodySmall = small,
        labelLarge = label,
        labelMedium = label,
        labelSmall = small,
    )
}
