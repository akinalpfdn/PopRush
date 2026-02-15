package com.akinalpfdn.poprush.core.domain.repository

import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import kotlinx.coroutines.flow.Flow

interface VisualSettingsRepository {

    suspend fun saveBubbleShape(shape: BubbleShape)

    suspend fun getBubbleShape(): BubbleShape

    fun getBubbleShapeFlow(): Flow<BubbleShape>

    suspend fun setZoomLevel(zoomLevel: Float)

    suspend fun getZoomLevel(): Float

    fun getZoomLevelFlow(): Flow<Float>

    suspend fun toggleHapticFeedback()

    suspend fun isHapticFeedbackEnabled(): Boolean

    fun getHapticFeedbackFlow(): Flow<Boolean>
}
