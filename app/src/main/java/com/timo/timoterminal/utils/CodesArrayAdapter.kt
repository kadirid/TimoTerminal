package com.timo.timoterminal.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.timo.timoterminal.R
import java.util.Locale


class CodesArrayAdapter(
    context: Context?,
    private val resource: Int,
    var allCodes: List<TimeZoneListEntry>
) :
    ArrayAdapter<Any?>(context!!, resource, allCodes), Filterable {
    var originalCodes: List<TimeZoneListEntry> = allCodes
    private val filter = StringFilter()

    override fun getCount(): Int {
        return allCodes.size
    }

    override fun getItem(position: Int): TimeZoneListEntry {
        return allCodes[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(resource, parent, false)
        }

        val entry : TimeZoneListEntry = getItem(position)

        val text1 = listItemView!!.findViewById<TextView>(R.id.dropdown_menu_timezone_name)
        text1.text = entry.getId()
        val text2 = listItemView.findViewById<TextView>(R.id.dropdown_menu_timezone_offset)
        text2.text = entry.getOffset()

        return listItemView
    }

    inner class StringFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterString = constraint.toString().lowercase(Locale.getDefault())
            val results = FilterResults()
            val list = originalCodes
            val count = list.size
            val nlist = ArrayList<TimeZoneListEntry>(count)
            var filterableString: TimeZoneListEntry
            for (i in 0 until count) {
                filterableString = list[i]
                if (filterableString.getId().lowercase(Locale.getDefault()).contains(filterString)) {
                    nlist.add(filterableString)
                }
            }
            results.values = nlist
            results.count = nlist.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            if (results.values != null)
                allCodes = results.values as List<TimeZoneListEntry>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

    class TimeZoneListEntry(
        private val id: String,
        private val zoneName: String,
        private val offset: String
    ) {
        fun getId(): String {
            return id
        }

        fun getZoneName(): String {
            return zoneName
        }

        fun getOffset(): String {
            return offset
        }

        override fun toString(): String {
            return getId()
        }
    }
}