package com.company.cabinetConfigurator.data.repository

import com.company.cabinetConfigurator.domain.model.Accessory
import com.company.cabinetConfigurator.domain.model.AccessoryType
import org.json.JSONArray
import org.json.JSONObject

fun List<Accessory>.toJson(): String {
    val array = JSONArray()
    forEach { acc ->
        array.put(JSONObject().apply {
            put("id", acc.id)
            put("type", acc.type.name)
            put("quantity", acc.quantity)
            put("manufacturer", acc.manufacturer)
            put("model", acc.model)
        })
    }
    return array.toString()
}

fun String.toAccessoryList(): List<Accessory> {
    return runCatching {
        val array = JSONArray(this)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            Accessory(
                id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                type = AccessoryType.valueOf(obj.getString("type")),
                quantity = obj.optInt("quantity", 1),
                manufacturer = obj.optString("manufacturer", ""),
                model = obj.optString("model", "")
            )
        }
    }.getOrDefault(emptyList())
}
