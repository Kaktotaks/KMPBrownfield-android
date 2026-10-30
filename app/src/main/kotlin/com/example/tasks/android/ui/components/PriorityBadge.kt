package com.example.tasks.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tasks.shared.domain.Priority

/** Layout only: label and color come from [Priority.displayName] and [Priority.badgeColor]. */
@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val color = priority.badgeColor()
    Text(
        text = priority.displayName,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
