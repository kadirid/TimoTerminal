package com.timo.timoterminal.entityAdaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.timo.timoterminal.R
import com.timo.timoterminal.entityClasses.TeamEntity
import java.util.Locale

class TeamEntityAdaptor(
    context: Context?,
    private val resource: Int,
    var allEntities: List<TeamEntity>
) :
    ArrayAdapter<Any?>(context!!, resource, allEntities), Filterable{
    var originalEntities: List<TeamEntity> = allEntities
    private val filter = StringFilter()

    override fun getCount(): Int {
        return allEntities.size
    }

    override fun getItem(position: Int): TeamEntity {
        return allEntities[position]
    }

    override fun getItemId(position: Int): Long {
        return allEntities[position].teamId
    }

    fun getItemByName(name: String): TeamEntity? {
        return allEntities.firstOrNull { it.teamName == name }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView =
            convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

        val entry: TeamEntity = getItem(position)

        val text1 = listItemView.findViewById<TextView>(R.id.textAutoComplete)
        text1.text = entry.teamName

        return listItemView
    }

    inner class StringFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (constraint.isNullOrEmpty()) {
                val results = FilterResults()
                results.values = originalEntities
                results.count = originalEntities.size
                return results
            }
            val filterString = constraint.toString().lowercase(Locale.getDefault())
            val results = FilterResults()
            val list = originalEntities
            val count = list.size
            val nlist = ArrayList<TeamEntity>(count)
            var filterableString: TeamEntity
            for (i in 0 until count) {
                filterableString = list[i]
                if (filterableString.teamName.lowercase(Locale.getDefault()).contains(filterString)) {
                    nlist.add(filterableString)
                }
            }
            results.values = nlist
            results.count = nlist.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            allEntities = results.values as List<TeamEntity>
            notifyDataSetChanged()
        }

        override fun convertResultToString(resultValue: Any): CharSequence {
            return (resultValue as TeamEntity).teamName
        }
    }

    override fun getFilter(): Filter {
        return filter
    }
}