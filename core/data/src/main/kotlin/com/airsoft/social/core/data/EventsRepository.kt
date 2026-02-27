package com.airsoft.social.core.data

import com.airsoft.social.core.model.GameEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

interface EventsRepository {
    fun observeEvents(): Flow<List<GameEvent>>
    fun observeEvent(eventId: String): Flow<GameEvent?>
}

class PreviewEventsRepository(
    previewRepository: SocialPreviewRepository,
) : EventsRepository {
    private val events = MutableStateFlow(previewRepository.listEvents())

    override fun observeEvents(): Flow<List<GameEvent>> = events

    override fun observeEvent(eventId: String): Flow<GameEvent?> =
        events.map { list -> list.firstOrNull { it.id == eventId } }
}
