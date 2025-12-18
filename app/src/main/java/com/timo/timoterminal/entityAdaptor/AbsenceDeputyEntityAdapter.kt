package com.timo.timoterminal.entityAdaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.timo.timoterminal.R
import org.json.JSONArray
import org.json.JSONObject

class AbsenceDeputyEntityAdapter(
    private val context: Context,
    private val resource: Int,
    var deputyEntities: JSONArray
): BaseAdapter(), Filterable {
    var originalEntities: JSONArray = deputyEntities
    private val filter = StringFilter()

    override fun getCount(): Int {
        return deputyEntities.length()
    }

    override fun getItem(position: Int): JSONObject {
        return deputyEntities.getJSONObject(position)
    }

    override fun getItemId(position: Int): Long {
        return deputyEntities.getJSONObject(position).optLong("id", position.toLong())
    }

    fun getItemByName(name: String): JSONObject? {
        for (i in 0 until deputyEntities.length()) {
            val item = deputyEntities.getJSONObject(i)
            if (item.getString("name") == name) {
                return item
            }
        }
        return null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

        val entry : JSONObject = getItem(position)

        val text1 = listItemView!!.findViewById<TextView>(R.id.textAutoComplete)
        text1.text = entry.optString("name", "")

        return listItemView
    }

    inner class StringFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (constraint.isNullOrEmpty()) {
                val results = FilterResults()
                results.values = originalEntities
                results.count = originalEntities.length()
                return results
            }
            val filterString = constraint.toString().lowercase()
            val results = FilterResults()
            val list = originalEntities
            val filteredList = list.let {
                val tempList = JSONArray()
                for (i in 0 until it.length()) {
                    val item = it.getJSONObject(i)
                    if (item.getString("name").lowercase().contains(filterString)) {
                        tempList.put(item)
                    }
                }
                tempList
            }
            results.values = filteredList
            results.count = filteredList.length()
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            deputyEntities = results.values as JSONArray
            notifyDataSetChanged()
        }

        override fun convertResultToString(resultValue: Any?): CharSequence {
            return if (resultValue is JSONObject) {
                resultValue.optString("name", "")
            } else {
                super.convertResultToString(resultValue)
            }
        }
    }

    override fun getFilter(): Filter {
        return filter
    }
}