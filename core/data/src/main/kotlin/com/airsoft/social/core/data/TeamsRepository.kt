package com.airsoft.social.core.data

import com.airsoft.social.core.model.Team
import com.airsoft.social.core.model.TeamRecruitingPostPreview
import com.airsoft.social.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

interface TeamsRepository {
    fun observeMyTeam(): Flow<Team?>
    fun observeTeam(teamId: String): Flow<Team?>
    fun observeRoster(teamId: String): Flow<List<User>>
    fun observeRecruitingFeed(): Flow<List<TeamRecruitingPostPreview>>
}

class PreviewTeamsRepository(
    previewRepository: SocialPreviewRepository,
) : TeamsRepository {
    private val teams = MutableStateFlow(previewRepository.listTeams())
    private val users = MutableStateFlow(previewRepository.listUsers())
    private val recruitingFeed = MutableStateFlow(seedRecruitingFeed(previewRepository))

    override fun observeMyTeam(): Flow<Team?> = teams.map { list ->
        list.firstOrNull { it.id == DEFAULT_MY_TEAM_ID } ?: list.firstOrNull()
    }

    override fun observeTeam(teamId: String): Flow<Team?> = teams.map { list ->
        list.firstOrNull { it.id == teamId }
    }

    override fun observeRoster(teamId: String): Flow<List<User>> = users.map { list ->
        list.filter { it.teamId == teamId }.sortedBy { it.callsign.lowercase() }
    }

    override fun observeRecruitingFeed(): Flow<List<TeamRecruitingPostPreview>> = recruitingFeed

    private fun seedRecruitingFeed(
        previewRepository: SocialPreviewRepository,
    ): List<TeamRecruitingPostPreview> {
        val teams = previewRepository.listTeams()
        if (teams.isEmpty()) return emptyList()
        val primary = teams.firstOrNull { it.id == DEFAULT_MY_TEAM_ID } ?: teams.first()
        return buildList {
            add(
                TeamRecruitingPostPreview(
                    id = "recruit-${primary.id}-medic",
                    teamId = primary.id,
                    title = "[${primary.shortName}] Нужен медик",
                    subtitle = "${primary.region} | Тренировки по выходным",
                    actionLabel = "Отклик",
                    tags = setOf("Медик", "Выходные"),
                ),
            )
            add(
                TeamRecruitingPostPreview(
                    id = "recruit-${primary.id}-storm",
                    teamId = primary.id,
                    title = "[${primary.shortName}] Нужен штурм",
                    subtitle = "${primary.region} | CQB / лес",
                    actionLabel = "Отклик",
                    tags = setOf("Штурм", "CQB"),
                ),
            )
            teams
                .filterNot { it.id == primary.id }
                .forEachIndexed { index, team ->
                    add(
                        TeamRecruitingPostPreview(
                            id = "recruit-${team.id}-$index",
                            teamId = team.id,
                            title = "[${team.shortName}] Набор в команду",
                            subtitle = "${team.region} | ${team.description ?: "Без описания"}",
                            actionLabel = "Смотреть",
                        ),
                    )
                }
        }
    }

    private companion object {
        const val DEFAULT_MY_TEAM_ID = "ew-easy-winner"
    }
}

internal class FakeTeamsRepository(
    teams: List<Team> = emptyList(),
    users: List<User> = emptyList(),
    recruitingFeed: List<TeamRecruitingPostPreview> = emptyList(),
) : TeamsRepository {
    private val teamsFlow = MutableStateFlow(teams)
    private val usersFlow = MutableStateFlow(users)
    private val recruitingFlow = MutableStateFlow(recruitingFeed)

    override fun observeMyTeam(): Flow<Team?> = teamsFlow.map { it.firstOrNull() }

    override fun observeTeam(teamId: String): Flow<Team?> =
        teamsFlow.map { list -> list.firstOrNull { it.id == teamId } }

    override fun observeRoster(teamId: String): Flow<List<User>> =
        usersFlow.map { list -> list.filter { it.teamId == teamId } }

    override fun observeRecruitingFeed(): Flow<List<TeamRecruitingPostPreview>> = recruitingFlow
}
