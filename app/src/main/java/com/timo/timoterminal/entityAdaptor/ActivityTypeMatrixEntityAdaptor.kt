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
import com.timo.timoterminal.entityClasses.ActivityTypeMatrixEntity
import java.util.Locale

class ActivityTypeMatrixEntityAdaptor(
    context: Context?,
    private val resource: Int,
    var allEntities: List<ActivityTypeMatrixEntity>
) :
    ArrayAdapter<Any?>(context!!, resource, allEntities), Filterable {
    var originalEntities: List<ActivityTypeMatrixEntity> = allEntities
    private val filter = StringFilter()

    override fun getCount(): Int {
        return allEntities.size
    }

    override fun getItem(position: Int): ActivityTypeMatrixEntity {
        return allEntities[position]
    }

    override fun getItemId(position: Int): Long {
        return allEntities[position].activityTypeMatrixId
    }

    fun getItemByName(name: String): ActivityTypeMatrixEntity? {
        return allEntities.firstOrNull { it.activityTypeMatrixName == name }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView =
            convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

        val entry: ActivityTypeMatrixEntity = getItem(position)

        val text1 = listItemView.findViewById<TextView>(R.id.textAutoComplete)
        text1.text = entry.activityTypeMatrixName

        return listItemView
    }

    inner class StringFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterString = constraint.toString().lowercase(Locale.getDefault())
            val results = FilterResults()
            val list = originalEntities
            val count = list.size
            val nlist = ArrayList<ActivityTypeMatrixEntity>(count)
            var filterableString: ActivityTypeMatrixEntity
            for (i in 0 until count) {
                filterableString = list[i]
                if (filterableString.activityTypeMatrixName.lowercase(Locale.getDefault())
                        .contains(filterString)
                ) {
                    nlist.add(filterableString)
                }
            }
            results.values = nlist
            results.count = nlist.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            allEntities = results.values as List<ActivityTypeMatrixEntity>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filter
    }
}