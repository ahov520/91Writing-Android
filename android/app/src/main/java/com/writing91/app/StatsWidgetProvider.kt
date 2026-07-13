package com.writing91.app

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import org.json.JSONObject

class StatsWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences("writing91_widget", Context.MODE_PRIVATE)
        val raw = prefs.getString("stats", "{}") ?: "{}"
        var today = 0
        var streak = 0
        var target = 0
        try {
            val o = JSONObject(raw)
            today = o.optInt("todayWords", 0)
            streak = o.optInt("streak", 0)
            target = o.optInt("target", 0)
        } catch (_: Exception) {
        }
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_stats)
            views.setTextViewText(R.id.widgetTitle, "91写作")
            views.setTextViewText(
                R.id.widgetBody,
                if (target > 0) "今日 $today / $target 字\n连续 $streak 天"
                else "今日 $today 字\n连续 $streak 天"
            )
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
