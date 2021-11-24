package com.example.thewitcherrpg.feature_character_sheet.presentation.magic.spellListAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.thewitcherrpg.databinding.SpellRowBinding
import com.example.thewitcherrpg.feature_character_sheet.domain.models.MagicItem
import com.example.thewitcherrpg.feature_character_sheet.domain.item_types.MagicType

class MagicListAdapter(val itemClick: (MagicItem) -> Unit) :
    RecyclerView.Adapter<MagicListAdapter.NoviceViewHolder>() {

    private var itemList = emptyList<MagicItem>()
    private lateinit var currentItem: MagicItem

    inner class NoviceViewHolder(private val binding: SpellRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MagicItem, position: Int) {


            when (item.type) {
                MagicType.NOVICE_SPELL,
                MagicType.JOURNEYMAN_SPELL,
                MagicType.MASTER_SPELL,
                MagicType.BASIC_SIGN,
                MagicType.ALTERNATE_SIGN,
                MagicType.NOVICE_DRUID_INVOCATION,
                MagicType.JOURNEYMAN_DRUID_INVOCATION,
                MagicType.MASTER_DRUID_INVOCATION,
                MagicType.NOVICE_PREACHER_INVOCATION,
                MagicType.JOURNEYMAN_PREACHER_INVOCATION,
                MagicType.MASTER_PREACHER_INVOCATION,
                MagicType.ARCH_PRIEST_INVOCATION -> {
                    val staCost =
                        "STA Cost: " + if (item.staminaCost != null) item.staminaCost.toString() else "Variable"
                    val range = "Range: " + item.range

                    with(binding) {
                        spellNameText.text = item.name
                        staCostText.text = staCost
                        elementText.text = item.element
                        rangeText.text = range
                        rowLayout.setOnClickListener {
                            itemClick(itemList[position])
                        }
                    }
                }
                MagicType.NOVICE_RITUAL,
                MagicType.JOURNEYMAN_RITUAL,
                MagicType.MASTER_RITUAL -> {
                    val staCost =
                        "STA Cost: " + if (item.staminaCost != null) item.staminaCost.toString() else "Variable"
                    val difficulty =
                        "Difficulty: " + if (item.difficulty != null) item.difficulty.toString() else "Variable"

                    with(binding) {
                        spellNameText.text = item.name
                        staCostText.text = staCost
                        rangeText.text = difficulty
                        rowLayout.setOnClickListener {
                            itemClick(itemList[position])
                        }
                    }
                }
                MagicType.HEX -> {
                    val staCost = "STA Cost: " + item.staminaCost
                    val danger = "Danger: " + item.danger

                    with(binding) {
                        spellNameText.text = item.name
                        staCostText.text = staCost
                        rangeText.text = danger
                        rowLayout.setOnClickListener() {
                            itemClick(itemList[position])
                        }
                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoviceViewHolder {
        val itemBinding =
            SpellRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoviceViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: NoviceViewHolder, position: Int) {
        currentItem = itemList[position]
        holder.bind(currentItem, position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setData(spell: List<MagicItem>) {
        this.itemList = spell
        notifyDataSetChanged()
    }
}