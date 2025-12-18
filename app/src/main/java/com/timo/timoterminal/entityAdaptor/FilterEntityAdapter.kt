package com.timo.timoterminal.entityAdaptor

import android.content.Context
import android.widget.ArrayAdapter

class FilterEntityAdapter(
    context: Context?,
    resource: Int,
    private val dateText: String,
    private val userText: String,
    private val projectText: String,
    private val taskText: String?,
    private val customerText: String?
) :
    ArrayAdapter<Any?>(
        context!!, resource,
        if (taskText == null || customerText == null)
            listOf(
                FilterEntity(1, dateText),
                FilterEntity(2, userText),
                FilterEntity(3, projectText)
            )
        else
            listOf(
                FilterEntity(1, dateText),
                FilterEntity(2, userText),
                FilterEntity(3, projectText),
                FilterEntity(4, taskText),
                FilterEntity(5, customerText)
            )
    ) {

    fun getIdByText(text: String?): Int {
        if (text == null) return 0
        return when (text) {
            dateText -> 1
            userText -> 2
            projectText -> 3
            taskText -> 4
            customerText -> 5
            else -> 0
        }
    }

    class FilterEntity(
        val id: Int,
        val name: String
    ) {
        override fun toString(): String {
            return name
        }
    }
}