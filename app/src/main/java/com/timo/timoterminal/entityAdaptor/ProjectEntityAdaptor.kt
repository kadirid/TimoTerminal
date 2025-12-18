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
import com.timo.timoterminal.entityClasses.ProjectEntity
import java.util.Locale

class ProjectEntityAdaptor(
    context: Context?,
    private val resource: Int,
    var projectEntities: List<ProjectEntity>
) : ArrayAdapter<Any?>(context!!, resource, projectEntities), Filterable {
    var originalEntities: List<ProjectEntity> = projectEntities
    private val filter = StringFilter()

    override fun getCount(): Int {
        return projectEntities.size
    }

    override fun getItem(position: Int): ProjectEntity {
        return projectEntities[position]
    }

    override fun getItemId(position: Int): Long {
        return projectEntities[position].projectId
    }

    fun getItemByName(name: String): ProjectEntity? {
        return projectEntities.firstOrNull { it.projectName == name }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

        val entry : ProjectEntity = getItem(position)

        val text1 = listItemView!!.findViewById<TextView>(R.id.textAutoComplete)
        text1.text = entry.projectName

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
            val nlist = ArrayList<ProjectEntity>(count)
            var filterableString: ProjectEntity
            for (i in 0 until count) {
                filterableString = list[i]
                if (filterableString.projectName.lowercase(Locale.getDefault()).contains(filterString)) {
                    nlist.add(filterableString)
                }
            }
            results.values = nlist
            results.count = nlist.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            projectEntities = results.values as List<ProjectEntity>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filter
    }


}