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
import com.timo.timoterminal.entityClasses.TaskEntity
import java.util.Locale

class TaskEntityAdaptor(
    context: Context?,
    private val resource: Int,
    var allEntities: List<TaskEntity>
) :
    ArrayAdapter<Any?>(context!!, resource, allEntities), Filterable {
    var originalEntities: List<TaskEntity> = allEntities
    private val filter = StringFilter()

    override fun getCount(): Int {
        return allEntities.size
    }

    override fun getItem(position: Int): TaskEntity {
        return allEntities[position]
    }

    override fun getItemId(position: Int): Long {
        return allEntities[position].taskId
    }

    fun getItemByName(name: String): TaskEntity? {
        return allEntities.firstOrNull { it.taskName == name }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView =
            convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

        val entry: TaskEntity = getItem(position)

        val text1 = listItemView.findViewById<TextView>(R.id.textAutoComplete)
        text1.text = entry.taskName

        return listItemView
    }

    inner class StringFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterString = constraint.toString().lowercase(Locale.getDefault())
            val results = FilterResults()
            val list = originalEntities
            val count = list.size
            val nlist = ArrayList<TaskEntity>(count)
            var filterableString: TaskEntity
            for (i in 0 until count) {
                filterableString = list[i]
                if (filterableString.taskName.lowercase(Locale.getDefault())
                        .contains(filterString)
                ) {
                    nlist.add(filterableString)
                }
            }
            results.values = nlist
            results.count = nlist.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            if (results.values != null)
                allEntities = results.values as List<TaskEntity>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filter
    }
}