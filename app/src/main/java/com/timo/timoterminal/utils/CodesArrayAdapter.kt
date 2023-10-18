package com.timo.timoterminal.utils

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import java.util.Locale


class CodesArrayAdapter(context: Context?, resource: Int, var allCodes: List<String>) :
    ArrayAdapter<Any?>(context!!, resource, allCodes), Filterable {
    var originalCodes: List<String> = allCodes
    var filter: StringFilter? = null

    override fun getCount(): Int {
        return allCodes.size
    }

    override fun getItem(position: Int): Any {
        return allCodes[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class StringFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterString = constraint.toString().lowercase(Locale.getDefault())
            val results = FilterResults()
            val list = originalCodes
            val count = list.size
            val nlist = ArrayList<String>(count)
            var filterableString: String
            for (i in 0 until count) {
                filterableString = list[i]
                if (filterableString.lowercase(Locale.getDefault()).contains(filterString)) {
                    nlist.add(filterableString)
                }
            }
            results.values = nlist
            results.count = nlist.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            allCodes = results.values as List<String>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return StringFilter()
    }
}