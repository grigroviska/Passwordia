package com.grigroviska.passwordia

import androidx.recyclerview.widget.DiffUtil
import com.grigroviska.passwordia.model.LoginData

class LoginDataDiffCallback(
    private val oldList: List<LoginData>,
    private val newList: List<LoginData>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
