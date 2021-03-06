package com.arbazmateen.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.Serializable


/**************************************************************************
** Model
**************************************************************************/
abstract class MultiSelectModel(open var itemId: Long,
                                open var displayText: String,
                                open var isSelected: Boolean = false): Serializable

/**************************************************************************
** Multi Select Adaptor
**************************************************************************/
class MultiSelectAdaptor<T : MultiSelectModel>(private val context: Context, private var dataList: MutableList<T>):
    RecyclerView.Adapter<MultiSelectAdaptor<T>.ViewHolder>() {

    var selectedIdsList = mutableListOf<Long>()
    var selectedItemsList = mutableListOf<T>()
    private var mOnDataBindListener: ((textView: TextView, item: T, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_dialog_multi_select, parent, false))
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position], position)
    }

    fun changeData(list: MutableList<T>) {
        this.dataList = list
        notifyDataSetChanged()
    }

    fun setDataBindListener(onDataBindListener: (textView: TextView, item: T, position: Int) -> Unit) =
        apply { this.mOnDataBindListener = onDataBindListener }


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {

        private var item: T? = null
        private var pos = -1

        private val checkBox = view.findViewById<CheckBox>(R.id.checkbox1)
        private val text = view.findViewById<TextView>(R.id.text1)

        init {
            view.setOnClickListener(this)
        }

        fun bind(i: T, position: Int) {
            this.item = i
            pos = position

            if(item is MultiSelectModel) {
//                item!!.isSelected = !item!!.isSelected
                checkBox.isChecked = item!!.isSelected
                text.text = item!!.displayText
//                if(item!!.isSelected) {
//                    if(!selectedIdsList.contains(item!!.itemId)) {
//                        selectedIdsList.add(item!!.itemId)
//                        selectedItemsList.add(item!!)
//                    }
//                } else {
//                    if(selectedIdsList.contains(item!!.itemId)) {
//                        selectedIdsList.remove(item!!.itemId)
//                        selectedItemsList.remove(item!!)
//                    }
//                }
            }

            if (mOnDataBindListener != null) {
                mOnDataBindListener?.invoke(text, i, pos)
            }
        }

        override fun onClick(p0: View?) {
            if(item is MultiSelectModel) {
                item!!.isSelected = !item!!.isSelected
                checkBox.isChecked = item!!.isSelected
                if(item!!.isSelected) {
                    selectedIdsList.add(item!!.itemId)
                    selectedItemsList.add(item!!)
                } else {
                    selectedIdsList.remove(item!!.itemId)
                    selectedItemsList.remove(item!!)
                }
            }
        }
    }
}

/**************************************************************************
** Multi Select Dialog Fragment
**************************************************************************/
class MultiSelectDialog<T : MultiSelectModel>: AppCompatDialogFragment(),
    SearchView.OnQueryTextListener, View.OnClickListener {

    private var onSubmitClickListener: ((selectedIds: MutableList<Long>,
                                         selectedItems: MutableList<T>,
                                         stringData: String) -> Unit)? = null
    private var onCloseClickListener: (() -> Unit)? = null

    private lateinit var mContext: Context

    private var mainDataList = mutableListOf<T>()
    private var preSelectedIds = mutableListOf<Long>()
    private var selectedItems = mutableListOf<T>()

    private lateinit var adaptor: MultiSelectAdaptor<T>

    private var title: String = "Multi Selection"
    private var positiveButtonText: String = "DONE"
    private var negativeButtonText: String = "CLOSE"

    private var searchAble = false
    private var selectAll = false

    private var minSelect = 1
    private var maxSelect = Int.MAX_VALUE

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var dialogTitle: TextView
    private lateinit var dialogSubmit: TextView
    private lateinit var dialogCancel: TextView
    private lateinit var selectAllContainer: LinearLayout
    private lateinit var selectAllCheckBox: CheckBox
    private lateinit var selectAllTextView: TextView


    fun with(context: Context) = apply { mContext = context }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(mContext)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setFlags(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        dialog.setContentView(R.layout.dialog_multi_select_view)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        recyclerView = dialog.findViewById(R.id.recycler_view)
        searchView = dialog.findViewById(R.id.search_view)
        dialogTitle = dialog.findViewById(R.id.title)
        dialogSubmit = dialog.findViewById(R.id.done)
        dialogCancel = dialog.findViewById(R.id.cancel)
        selectAllContainer = dialog.findViewById(R.id.select_all_container)
        selectAllCheckBox = dialog.findViewById(R.id.select_all_checkbox)
        selectAllTextView = dialog.findViewById(R.id.select_all_text)

        val layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        if(selectAll) {
            selectAllContainer.visibility = View.VISIBLE
        } else {
            selectAllContainer.visibility = View.GONE
        }

        dialogSubmit.setOnClickListener(this)
        dialogCancel.setOnClickListener(this)
        selectAllContainer.setOnClickListener(this)

        if(mainDataList.isNotEmpty() && preSelectedIds.isNotEmpty()) {
            if(mainDataList.size == preSelectedIds.size) {
                selectAllCheckBox.isChecked = true
                selectAllTextView.text = "DESELECT ALL"
            } else {
                selectAllCheckBox.isChecked = false
                selectAllTextView.text = "SELECT ALL"
            }
        }

        checkSelectedItems()
        adaptor = MultiSelectAdaptor(mContext, mainDataList)
        adaptor.selectedIdsList = preSelectedIds
        adaptor.selectedItemsList = selectedItems
        recyclerView.adapter = adaptor

        if(searchAble) {
            searchView.setOnQueryTextListener(this)
            searchView.onActionViewExpanded()
            searchView.clearFocus()
        } else {
            searchView.visibility = View.GONE
        }

        dialogTitle(title)

        return dialog
    }

    fun dataList(dataList: MutableList<T>) = apply { this.mainDataList = dataList }

    fun preSelectedIds(preSelectedIds: MutableList<Long>) = apply { this.preSelectedIds = preSelectedIds }

    fun dialogTitle(title: String) = apply { this.title = title; dialogTitle.text = title }

    fun searchable(searchAble: Boolean) = apply { this.searchAble = searchAble }

    fun canSelectAll(selectAll: Boolean) = apply { this.selectAll = selectAll }

    fun minimumSelect(minSelect: Int) = apply { this.minSelect = minSelect }
    fun maximumSelect(maxSelect: Int) = apply { this.maxSelect = maxSelect }

    fun positiveButton(text: String, listener: (selectedIds: MutableList<Long>,
                                                selectedItems: MutableList<T>,
                                                stringData: String) -> Unit) =
        apply {
            this.positiveButtonText = text
            this.onSubmitClickListener = listener
        }

    fun negativeButton(text: String, listener: () -> Unit) =
        apply {
            this.negativeButtonText = text
            this.onCloseClickListener = listener
        }

    override fun onQueryTextSubmit(query: String?): Boolean { return false }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(!newText.isNullOrEmpty()) {
            val filteredList = mainDataList.asSequence()
                .filter { item -> item.displayText.toLowerCase().contains(newText.toLowerCase()) }
                .toMutableList()
            adaptor.changeData(filteredList)
        } else {
            adaptor.changeData(mainDataList)
        }
        return false
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.done -> {
                if(onSubmitClickListener != null) {
                    val size = adaptor.selectedItemsList.size
                    when {
                        size < minSelect -> {
                            Toast.makeText(mContext, "Minimum selection required: $minSelect", Toast.LENGTH_LONG).show()
                        }
                        size > maxSelect -> {
                            Toast.makeText(mContext, "Maximum selection allowed: $maxSelect", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            val text = adaptor.selectedItemsList.toString()
                            onSubmitClickListener?.invoke(
                                adaptor.selectedIdsList,
                                adaptor.selectedItemsList,
                                text.substring(1, text.length - 1))

                            dismiss()
                        }
                    }
                }
            }
            R.id.cancel -> {
                if(onCloseClickListener != null) {
                    onCloseClickListener?.invoke()
                    dismiss()
                }
            }
            R.id.select_all_container -> {
                if(selectAllCheckBox.isChecked) {
                    selectAllCheckBox.isChecked = false
                    selectAllTextView.text = "SELECT ALL"
                    deSelectAll()
                } else {
                    selectAllCheckBox.isChecked = true
                    selectAllTextView.text = "DESELECT ALL"
                    selectAll()
                }
            }
        }
    }

    private fun checkSelectedItems() {
        preSelectedIds.sort()
        selectedItems.clear()
        mainDataList.forEach { item ->
            item.isSelected = false
            val (_, find) = findIn(preSelectedIds, 0, preSelectedIds.size - 1, item.itemId)
            if(find) {
                item.isSelected = true
                selectedItems.add(item)
            }
        }
    }

    private fun selectAll() {
        preSelectedIds.clear()
        selectedItems.clear()
        mainDataList.forEach { item ->
            item.isSelected = true
            preSelectedIds.add(item.itemId)
            selectedItems.add(item)
        }
        adaptor.changeData(mainDataList)
    }

    private fun deSelectAll() {
        preSelectedIds.clear()
        selectedItems.clear()
        mainDataList.forEach { item ->
            item.isSelected = false
        }
        adaptor.changeData(mainDataList)
    }

    class Builder<T : MultiSelectModel>(private val context: Context) {

        private var title = "Select"
        private var positiveButtonText = "DONE"
        private var negativeButtonText = "CLOSE"
        private var searchAble = false
        private var selectAll = false

        private var minSelect = 1
        private var maxSelect = Int.MAX_VALUE

        private var dataList = mutableListOf<T>()
        private var preSelectedIdsList = mutableListOf<Long>()

        private var onSubmitClickListener: ((selectedIds: MutableList<Long>,
                                             selectedItems: MutableList<T>,
                                             stringData: String) -> Unit)? = null
        private var onCloseClickListener: (() -> Unit)? = null


        fun dataList(dataList: MutableList<T>) = apply { this.dataList = dataList }

        fun preSelectedIds(preSelectedIds: MutableList<Long>) = apply { this.preSelectedIdsList = preSelectedIds }

        fun dialogTitle(title: String) = apply { this.title = title }

        fun searchable(searchAble: Boolean) = apply { this.searchAble = searchAble }
        fun canSelectAll(selectAll: Boolean) = apply { this.selectAll = selectAll }

        fun minimumSelect(minSelect: Int) = apply { this.minSelect = minSelect }
        fun maximumSelect(maxSelect: Int) = apply { this.maxSelect = maxSelect }

        fun positiveButton(text: String, listener: (selectedIds: MutableList<Long>,
                                                     selectedItems: MutableList<T>,
                                                     stringData: String) -> Unit) =
            apply {
                this.positiveButtonText = text
                this.onSubmitClickListener = listener
            }

        fun negativeButton(text: String, listener: () -> Unit) =
            apply {
                this.negativeButtonText = text
                this.onCloseClickListener = listener
            }

        fun build(): MultiSelectDialog<T> {
            val dialog = MultiSelectDialog<T>().with(context)

            dialog.title = this.title
            dialog.mainDataList = dataList
            dialog.preSelectedIds = preSelectedIdsList
            dialog.positiveButtonText = positiveButtonText
            dialog.negativeButtonText = negativeButtonText
            dialog.onSubmitClickListener = onSubmitClickListener
            dialog.onCloseClickListener = onCloseClickListener
            dialog.searchAble = searchAble
            dialog.selectAll = selectAll
            dialog.minSelect = minSelect
            dialog.maxSelect = maxSelect

            return dialog
        }
    }
}