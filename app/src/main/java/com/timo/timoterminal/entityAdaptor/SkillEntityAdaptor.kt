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
import com.timo.timoterminal.entityClasses.SkillEntity
import java.util.Locale

class SkillEntityAdaptor(
    context: Context?,
    private val resource: Int,
    var allEntities: List<SkillEntity>
) :
    ArrayAdapter<Any?>(context!!, resource, allEntities), Filterable {
    var originalEntities: List<SkillEntity> = allEntities
    private val filter = StringFilter()

    override fun getCount(): Int {
        return allEntities.size
    }

    override fun getItem(position: Int): SkillEntity {
        return allEntities[position]
    }

    override fun getItemId(position: Int): Long {
        return allEntities[position].skillId
    }

    fun getItemByName(name: String): SkillEntity? {
        return allEntities.firstOrNull { it.skillName == name }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView =
            convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

        val entry: SkillEntity = getItem(position)

        val text1 = listItemView.findViewById<TextView>(R.id.textAutoComplete)
        text1.text = entry.skillName

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
            val nlist = ArrayList<SkillEntity>(count)
            var filterableString: SkillEntity
            for (i in 0 until count) {
                filterableString = list[i]
                if (filterableString.skillName.lowercase(Locale.getDefault()).contains(filterString)) {
                    nlist.add(filterableString)
                }
            }
            results.values = nlist
            results.count = nlist.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            allEntities = results.values as List<SkillEntity>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filter
    }
}