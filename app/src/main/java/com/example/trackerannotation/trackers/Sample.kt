package com.example.trackerannotation.trackers

import com.example.trackerannotation.Tracker

object RecommendationTracker {

    fun sendTracker(model: Recommendation.WidgetImpressionModel) {

    }

    fun sendClickTracker(model: Recommendation.WidgetItemImpressionModel) {

    }
}

@Tracker
sealed interface Recommendation {

    data class WidgetImpressionModel(
        val userId: String,
        val userName: String,
        val widgetPosition: Int,
        val price: String
    ): Recommendation

    data class WidgetItemImpressionModel(
        val widgetId: Int,
        val title: String,
        val widget: WidgetImpressionModel
    ): Recommendation
}

object TrackApp {
    fun send(map: Map<*, *>) {

    }
}

