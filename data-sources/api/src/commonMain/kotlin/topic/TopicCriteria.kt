package me.him188.ani.datasources.api.topic

import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.source.MediaFetchRequest

class TopicCriteria(
    val episodeSort: EpisodeSort?,
    val episodeEp: EpisodeSort?,
    val fallback: Boolean = false,
) {
    companion object {
        val ANY = TopicCriteria(null, null, fallback = true)
    }
}

fun TopicCriteria.matches(topic: Topic, allowEpMatch: Boolean): Boolean {
    val details = topic.details ?: return fallback
    if (isEpisodeSortMatch(details)) return true
    if (allowEpMatch && isEpisodeEpMatch(details)) return true
    return fallback
}

private fun TopicCriteria.isEpisodeSortMatch(details: TopicDetails): Boolean {
    episodeSort?.let { expected ->
        val ep = details.episodeRange ?: return false
        return expected in ep
    }
    return false
}

private fun TopicCriteria.isEpisodeEpMatch(details: TopicDetails): Boolean {
    episodeEp?.let { expected ->
        val ep = details.episodeRange ?: return false
        return expected in ep
    }
    return false
}


fun MediaFetchRequest.toTopicCriteria(): TopicCriteria {
    return TopicCriteria(
        episodeSort = episodeSort,
        episodeEp = episodeEp,
    )
}