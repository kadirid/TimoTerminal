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
import com.timo.timoterminal.entityClasses.TicketEntity
import java.util.Locale

class TicketEntityAdaptor(
    context: Context?,
    private val resource: Int,
    var allEntities: List<TicketEntity>
) :
    ArrayAdapter<Any?>(context!!, resource, allEntities), Filterable {
    var originalEntities: List<TicketEntity> = allEntities
    private val filter = StringFilter()

    override fun getCount(): Int {
        return allEntities.size
    }

    override fun getItem(position: Int): TicketEntity {
        return allEntities[position]
    }

    override fun getItemId(position: Int): Long {
        return allEntities[position].ticketId
    }

    fun getItemByName(name: String): TicketEntity? {
        return allEntities.firstOrNull { it.ticketName == name }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView =
            convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

        val entry: TicketEntity = getItem(position)

        val text1 = listItemView.findViewById<TextView>(R.id.textAutoComplete)
        text1.text = entry.ticketName

        return listItemView
    }

    inner class StringFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterString = constraint.toString().lowercase(Locale.getDefault())
            val results = FilterResults()
            val list = originalEntities
            val count = list.size
            val nlist = ArrayList<TicketEntity>(count)
            var filterableString: TicketEntity
            for (i in 0 until count) {
                filterableString = list[i]
                if (filterableString.ticketName.lowercase(Locale.getDefault()).contains(filterString)) {
                    nlist.add(filterableString)
                }
            }
            results.values = nlist
            results.count = nlist.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            allEntities = results.values as List<TicketEntity>
            notifyDataSetChanged()
        }

        override fun convertResultToString(resultValue: Any): CharSequence {
            return (resultValue as TicketEntity).ticketName
        }
    }

    override fun getFilter(): Filter {
        return filter
    }
}