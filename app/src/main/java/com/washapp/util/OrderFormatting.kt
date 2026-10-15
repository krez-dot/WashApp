package com.washapp.util

import android.content.Context
import android.content.res.ColorStateList
import android.widget.TextView
import com.washapp.R
import com.washapp.data.model.Order
import com.washapp.data.model.ServiceStage
import com.washapp.data.model.ServiceType

fun Context.serviceTypeLabel(type: ServiceType): String = getString(
    when (type) {
        ServiceType.COLORED -> R.string.title_service_colored
        ServiceType.NON_COLORED -> R.string.title_service_non_colored
    }
)

fun Context.orderServiceLabel(order: Order): String {
    val type = serviceTypeLabel(order.serviceType)
    return if (order.folding) "$type · ${getString(R.string.label_with_folding)}" else type
}

/** Shows the stage name on a pill tinted to match the stage, as used on the tracking cards. */
fun TextView.bindStageChip(stage: ServiceStage) {
    text = stage.name
    val colorRes = when (stage) {
        ServiceStage.QUEUED -> R.color.washapp_stage_queued
        ServiceStage.WASHING -> R.color.washapp_stage_washing
        ServiceStage.DRYING -> R.color.washapp_stage_drying
        ServiceStage.FOLDING -> R.color.washapp_stage_folding
        ServiceStage.COMPLETED -> R.color.washapp_stage_completed
    }
    backgroundTintList = ColorStateList.valueOf(context.getColor(colorRes))
}
