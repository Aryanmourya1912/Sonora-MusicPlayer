package com.example.music

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSheet(
    isDarkTheme: Boolean = true,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isEnabled = EqualizerManager.isEnabled
    val bands = EqualizerManager.bands
    val presets = EqualizerManager.presets
    val currentPresetIdx = EqualizerManager.currentPresetIndex
    val minLevel = EqualizerManager.minLevelMb
    val maxLevel = EqualizerManager.maxLevelMb

    val sheetBgColor = if (isDarkTheme) Color(0xFF0F151C) else Color(0xFFFFFFFF)
    val cardBgColor = if (isDarkTheme) Color(0xFF141C24) else Color(0xFFF1F5F9)
    val textPrimaryColor = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val textSecondaryColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBgColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header: Title, Reset Button, and Toggle Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) Color(0xFF1E2836) else Color(0xFFEDE9FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = if (isEnabled) MaterialTheme.colorScheme.primary else (if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Equalizer",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor
                        )
                        Text(
                            text = if (isEnabled) "Audio FX Active" else "Bypassed",
                            fontSize = 12.sp,
                            color = if (isEnabled) Color(0xFF10B981) else textSecondaryColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { EqualizerManager.resetToFlat(context) },
                        enabled = isEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = "Reset to Flat",
                            tint = if (isEnabled) textPrimaryColor else (if (isDarkTheme) Color(0xFF475569) else Color(0xFFCBD5E1))
                        )
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { EqualizerManager.setEnabled(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8),
                            uncheckedTrackColor = if (isDarkTheme) Color(0xFF1E2631) else Color(0xFFE2E8F0)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets Horizontal Carousel
            Text(
                text = "PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondaryColor,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(presets) { index, name ->
                    val isSelected = index == currentPresetIdx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    !isEnabled -> if (isDarkTheme) Color(0xFF161E28) else Color(0xFFF1F5F9)
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    else -> if (isDarkTheme) Color(0xFF1E2836) else Color(0xFFE2E8F0)
                                }
                            )
                            .clickable(enabled = isEnabled) {
                                EqualizerManager.applyPreset(context, index)
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                !isEnabled -> if (isDarkTheme) Color(0xFF475569) else Color(0xFF94A3B8)
                                isSelected -> Color.White
                                else -> if (isDarkTheme) Color(0xFFD1D5DB) else Color(0xFF334155)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Graphic Equalizer Channel Strip (5 Vertical Sliders)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FREQUENCY BANDS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondaryColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "+15 dB / -15 dB",
                            fontSize = 10.sp,
                            color = if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bands.forEach { band ->
                            VerticalBandColumn(
                                band = band,
                                minMb = minLevel,
                                maxMb = maxLevel,
                                isEnabled = isEnabled,
                                isDarkTheme = isDarkTheme,
                                onLevelChange = { newLvl ->
                                    EqualizerManager.setBandLevel(context, band.index, newLvl)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sound Effects: Bass Boost & 3D Virtualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bass Boost Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Headphones,
                                    contentDescription = null,
                                    tint = if (isEnabled && EqualizerManager.isBassBoostSupported) MaterialTheme.colorScheme.primary else (if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Bass Boost",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimaryColor
                                )
                            }
                            val bassPercent = (EqualizerManager.bassBoostStrength / 10f).roundToInt()
                            Text(
                                text = "$bassPercent%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isEnabled) MaterialTheme.colorScheme.primary else textSecondaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Slider(
                            value = EqualizerManager.bassBoostStrength,
                            onValueChange = { EqualizerManager.setBassBoost(context, it) },
                            valueRange = 0f..1000f,
                            enabled = isEnabled && EqualizerManager.isBassBoostSupported,
                            colors = SliderDefaults.colors(
                                thumbColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = if (isDarkTheme) Color(0xFF1E2836) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 3D Virtualizer Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.SurroundSound,
                                    contentDescription = null,
                                    tint = if (isEnabled && EqualizerManager.isVirtualizerSupported) MaterialTheme.colorScheme.primary else (if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Virtualizer",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimaryColor
                                )
                            }
                            val virtPercent = (EqualizerManager.virtualizerStrength / 10f).roundToInt()
                            Text(
                                text = "$virtPercent%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isEnabled) MaterialTheme.colorScheme.primary else textSecondaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Slider(
                            value = EqualizerManager.virtualizerStrength,
                            onValueChange = { EqualizerManager.setVirtualizer(context, it) },
                            valueRange = 0f..1000f,
                            enabled = isEnabled && EqualizerManager.isVirtualizerSupported,
                            colors = SliderDefaults.colors(
                                thumbColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = if (isDarkTheme) Color(0xFF1E2836) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun VerticalBandColumn(
    band: EqualizerBand,
    minMb: Short,
    maxMb: Short,
    isEnabled: Boolean,
    isDarkTheme: Boolean,
    onLevelChange: (Short) -> Unit
) {
    val totalRange = (maxMb - minMb).toFloat().coerceAtLeast(1f)
    val fraction = ((band.levelMb - minMb) / totalRange).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(targetValue = fraction, label = "bandFrac")

    val dbVal = (band.levelMb / 100f).roundToInt()
    val dbLabel = if (dbVal > 0) "+$dbVal" else "$dbVal"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(52.dp)
    ) {
        Text(
            text = dbLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                !isEnabled -> if (isDarkTheme) Color(0xFF475569) else Color(0xFF94A3B8)
                dbVal > 0 -> MaterialTheme.colorScheme.primary
                dbVal < 0 -> if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)
                else -> if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .width(28.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isDarkTheme) Color(0xFF1B232D) else Color(0xFFE2E8F0))
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput
                    detectTapGestures { offset ->
                        val newFraction = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                        val newMb = (minMb + newFraction * totalRange).toInt().toShort()
                        onLevelChange(newMb)
                    }
                }
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput
                    detectDragGestures { change, _ ->
                        change.consume()
                        val newFraction = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                        val newMb = (minMb + newFraction * totalRange).toInt().toShort()
                        onLevelChange(newMb)
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Zero-line reference
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.Center)
                    .background(if (isDarkTheme) Color(0x30FFFFFF) else Color(0x30000000))
            )

            // Dynamic Fill Column from Zero Line
            if (animatedFraction >= 0.5f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .fillMaxHeight(animatedFraction - 0.5f)
                        .align(Alignment.Center)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    if (isEnabled) MaterialTheme.colorScheme.primary else (if (isDarkTheme) Color(0xFF475569) else Color(0xFF94A3B8)),
                                    if (isEnabled) (if (isDarkTheme) Color(0xFF9F75FF) else Color(0xFF8B5CF6)) else (if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1))
                                )
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .fillMaxHeight(0.5f - animatedFraction)
                        .align(Alignment.Center)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    if (isEnabled) (if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)) else (if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)),
                                    if (isEnabled) (if (isDarkTheme) Color(0xFF0284C7) else Color(0xFF0369A1)) else (if (isDarkTheme) Color(0xFF475569) else Color(0xFF94A3B8))
                                )
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }

            // Draggable Thumb Knob
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = (animatedFraction * 106).dp)
                        .clip(CircleShape)
                        .background(
                            if (isEnabled) Color.White 
                            else (if (isDarkTheme) Color(0xFF64748B) else Color(0xFFCBD5E1))
                        )
                        .then(
                            if (!isDarkTheme && isEnabled) {
                                Modifier.border(1.dp, Color(0xFFCBD5E1), CircleShape)
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isEnabled) MaterialTheme.colorScheme.primary 
                                else (if (isDarkTheme) Color(0xFF1E2836) else Color(0xFF94A3B8))
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = band.centerFreqLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isEnabled) (if (isDarkTheme) Color.White else Color(0xFF0F172A)) else (if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}