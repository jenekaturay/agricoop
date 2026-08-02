package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFD0D7D4), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.size(size - 24.dp)) {
            val grid = 15
            val cellSize = this.size.width / grid

            val hash = abs(data.hashCode())

            // Corner finder patterns (top-left, top-right, bottom-left)
            drawRect(
                color = Color.Black,
                topLeft = Offset(0f, 0f),
                size = Size(cellSize * 4, cellSize * 4)
            )
            drawRect(
                color = Color.White,
                topLeft = Offset(cellSize, cellSize),
                size = Size(cellSize * 2, cellSize * 2)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(cellSize * 1.5f, cellSize * 1.5f),
                size = Size(cellSize, cellSize)
            )

            drawRect(
                color = Color.Black,
                topLeft = Offset(cellSize * 11, 0f),
                size = Size(cellSize * 4, cellSize * 4)
            )
            drawRect(
                color = Color.White,
                topLeft = Offset(cellSize * 12, cellSize),
                size = Size(cellSize * 2, cellSize * 2)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(cellSize * 12.5f, cellSize * 1.5f),
                size = Size(cellSize, cellSize)
            )

            drawRect(
                color = Color.Black,
                topLeft = Offset(0f, cellSize * 11),
                size = Size(cellSize * 4, cellSize * 4)
            )
            drawRect(
                color = Color.White,
                topLeft = Offset(cellSize, cellSize * 12),
                size = Size(cellSize * 2, cellSize * 2)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(cellSize * 1.5f, cellSize * 12.5f),
                size = Size(cellSize, cellSize)
            )

            // Data modules
            for (r in 0 until grid) {
                for (c in 0 until grid) {
                    // Skip corners
                    if ((r < 5 && c < 5) || (r < 5 && c > 9) || (r > 9 && c < 5)) continue

                    val bit = ((hash xor (r * 17 + c * 31)) % 3 == 0)
                    if (bit) {
                        drawRect(
                            color = Color(0xFF0B3D2E),
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.9f, cellSize * 0.9f)
                        )
                    }
                }
            }
        }
    }
}
