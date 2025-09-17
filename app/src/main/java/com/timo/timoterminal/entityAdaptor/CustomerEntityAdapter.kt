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
import com.timo.timoterminal.entityClasses.CustomerEntity

class CustomerEntityAdapter(
    context: Context?,
    private val resource: Int,
    var allEntities: List<CustomerComboEntity>
) :
    ArrayAdapter<Any?>(context!!, resource, allEntities), Filterable {
    var originalEntities: List<CustomerComboEntity> = allEntities
    private val filter = StringFilter()

    override fun getCount(): Int {
        return allEntities.size
    }
    
    override fun getItem(position: Int): CustomerComboEntity {
        return allEntities[position]
    }
    
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getItemByName(name: String): CustomerComboEntity? {
        return allEntities.firstOrNull { it.name == name }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView =
            convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

        val entry: CustomerComboEntity = getItem(position)

        val text1 = listItemView.findViewById<TextView>(R.id.textAutoComplete)
        text1.text = entry.name

        return listItemView
    }

    inner class StringFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterString = constraint.toString().lowercase()
            val results = FilterResults()
            val list = originalEntities
            val count = list.size
            val nlist = ArrayList<CustomerComboEntity>(count)
            var filterableString: CustomerComboEntity
            for (i in 0 until count) {
                filterableString = list[i]
                if (filterableString.name.lowercase().contains(filterString)) {
                    nlist.add(filterableString)
                }
            }
            results.values = nlist
            results.count = nlist.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            allEntities = results.values as List<CustomerComboEntity>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

    class CustomerComboEntity(
        val id: String,
        val name: String
    ){
        override fun toString(): String {
            return name
        }
    }
}