package de.frederikkohler.authapi.errors

sealed class ProjectException(override val message: String? = null) : Exception(message) {
    data object ProjectNotFound : ProjectException("Das angeforderte Projekt wurde nicht gefunden.")
    data object ProjectNameAlreadyExists : ProjectException("Ein Projekt mit diesem Namen existiert bereits.")
    data object UserNotAuthorized : ProjectException("Dieser Benutzer ist nicht berechtigt, diese Aktion auszuführen.")
    data class InvalidProjectData(override val message: String) : ProjectException(message)
    data object ProjectCreationFailed : ProjectException("Fehler beim Erstellen des Projekts.")
    data object UserNotFound : ProjectException("Benutzer nicht gefunden.") 
}