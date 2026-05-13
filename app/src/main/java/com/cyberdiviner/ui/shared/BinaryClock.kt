package com.cyberdiviner.ui.shared
import com.cyberdiviner.ui.theme.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.CyberPrimary
import com.cyberdiviner.ui.theme.TextMuted
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun BinaryClock(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            time = System.currentTimeMillis()
            delay(1000)
        }
    }
    val cal = remember(time) {
        Calendar.getInstance().apply { this.timeInMillis = time }
    }
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BinaryByte(value = hour, bits = 5)
        Text(":", color = CyberPrimary, fontSize = 16.sp, fontFamily = MonoFontFamily)
        BinaryByte(value = minute, bits = 6)
    }
}

@Composable
private fun BinaryByte(value: Int, bits: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in (bits - 1) downTo 0) {
            val bit = (value shr i) and 1
            Text(
                text = "$bit",
                color = if (bit == 1) CyberPrimary else TextMuted,
                fontSize = 14.sp,
                fontFamily = MonoFontFamily
            )
        }
    }
}
