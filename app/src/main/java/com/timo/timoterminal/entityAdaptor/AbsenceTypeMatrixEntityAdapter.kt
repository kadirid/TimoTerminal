package com.timo.timoterminal.entityAdaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.timo.timoterminal.R
import com.timo.timoterminal.entityClasses.AbsenceTypeMatrixEntity

class AbsenceTypeMatrixEntityAdapter(
    context: Context,
    private val resource: Int,
    var absenceTypeMatrixEntities: List<AbsenceTypeMatrixEntity>
): ArrayAdapter<Any?>(context, resource, absenceTypeMatrixEntities) {
    var originalEntities: List<AbsenceTypeMatrixEntity> = absenceTypeMatrixEntities
    private val filter = StringFilter()

    override fun getCount(): Int {
        return absenceTypeMatrixEntities.size
    }

    override fun getItem(position: Int): AbsenceTypeMatrixEntity {
        return absenceTypeMatrixEntities[position]
    }

    override fun getItemId(position: Int): Long {
        return absenceTypeMatrixEntities[position].id.toLong()
    }

    fun getItemByName(name: String): AbsenceTypeMatrixEntity? {
        return absenceTypeMatrixEntities.firstOrNull { it.name == name }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

        val entry : AbsenceTypeMatrixEntity = getItem(position)

        val text1 = listItemView!!.findViewById<TextView>(R.id.textAutoComplete)
        text1.text = entry.name

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
            val filterString = constraint.toString().lowercase()
            val results = FilterResults()
            val list = originalEntities
            val filteredList = list.filter {
                it.name.lowercase().contains(filterString)
            }
            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            absenceTypeMatrixEntities = results.values as List<AbsenceTypeMatrixEntity>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

}