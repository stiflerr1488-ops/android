package com.airsoft.social.core.model

enum class EditorMode(
    val routeValue: String,
    val label: String,
) {
    Create(routeValue = "create", label = "Создание"),
    Edit(routeValue = "edit", label = "Редактирование"),
    Draft(routeValue = "draft", label = "Черновик");

    companion object {
        fun fromRouteValue(value: String): EditorMode? {
            val normalized = value.trim().lowercase()
            return entries.firstOrNull { it.routeValue == normalized }
        }
    }
}
