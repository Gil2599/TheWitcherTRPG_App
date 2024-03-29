package com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.magic

import com.witcher.thewitcherrpg.R
import com.witcher.thewitcherrpg.TheWitcherTRPGApp
import com.witcher.thewitcherrpg.feature_character_sheet.domain.models.MagicItem
import com.witcher.thewitcherrpg.feature_character_sheet.domain.item_types.MagicType
import javax.inject.Inject

class GetMagicListUseCase @Inject constructor() {

    operator fun invoke(source: Int): ArrayList<MagicItem> {

        val magicStringArray = TheWitcherTRPGApp.getContext()!!.resources!!.getStringArray(source)

        return when (source) {
            R.array.minor_gifts_list_data -> getMagicalGiftListInfo(
                magicStringArray,
                MagicType.MINOR_GIFT
            )
            R.array.major_gifts_list_data -> getMagicalGiftListInfo(
                magicStringArray,
                MagicType.MAJOR_GIFT
            )
            R.array.novice_spells_list_data -> getSpellListInfo(
                magicStringArray,
                MagicType.NOVICE_SPELL
            )
            R.array.journeyman_spells_list_data -> getSpellListInfo(
                magicStringArray,
                MagicType.JOURNEYMAN_SPELL
            )
            R.array.master_spells_list_data -> getSpellListInfo(
                magicStringArray,
                MagicType.MASTER_SPELL
            )
            R.array.novice_rituals_list_data -> getRitualListInfo(
                magicStringArray,
                MagicType.NOVICE_RITUAL
            )
            R.array.journeyman_rituals_list_data -> getRitualListInfo(
                magicStringArray,
                MagicType.JOURNEYMAN_RITUAL
            )
            R.array.master_rituals_list_data -> getRitualListInfo(
                magicStringArray,
                MagicType.MASTER_RITUAL
            )
            R.array.hexes_list_data -> getHexListInfo(
                magicStringArray
            )
            R.array.basic_signs_list_data -> getSpellListInfo(
                magicStringArray,
                MagicType.BASIC_SIGN
            )
            R.array.alternate_signs_list_data -> getSpellListInfo(
                magicStringArray,
                MagicType.ALTERNATE_SIGN
            )
            R.array.novice_druidInvo_list_data -> getInvocationListInfo(
                magicStringArray,
                MagicType.NOVICE_DRUID_INVOCATION
            )
            R.array.journeyman_druidInvo_list_data -> getInvocationListInfo(
                magicStringArray,
                MagicType.JOURNEYMAN_DRUID_INVOCATION
            )
            R.array.master_druidInvo_data -> getInvocationListInfo(
                magicStringArray,
                MagicType.MASTER_DRUID_INVOCATION
            )
            R.array.hierophant_flaminika_priestInvo_list_data -> getInvocationListInfo(
                magicStringArray,
                MagicType.HIEROPHANT_FLAMINIKA_DRUID_INVOCATION
            )
            R.array.novice_priestInvo_list_data -> getInvocationListInfo(
                magicStringArray,
                MagicType.NOVICE_PREACHER_INVOCATION
            )
            R.array.journeyman_priestInvo_list_data -> getInvocationListInfo(
                magicStringArray,
                MagicType.JOURNEYMAN_PREACHER_INVOCATION
            )
            R.array.master_priestInvo_list_data -> getInvocationListInfo(
                magicStringArray,
                MagicType.MASTER_PREACHER_INVOCATION
            )
            R.array.archPriestInvo_list_data -> getInvocationListInfo(
                magicStringArray,
                MagicType.ARCH_PRIEST_INVOCATION
            )
            else -> arrayListOf()
        }
    }

    private fun getSpellListInfo(itemList: Array<String>, type: MagicType): ArrayList<MagicItem> {

        val magicArray: ArrayList<MagicItem> = arrayListOf()

        for (tag in itemList) {
            val pair = tag.split(":").toTypedArray()
            val spellName = pair[0]
            val staCost = pair[1]
            val description = pair[2]
            val range = pair[3]
            val duration = pair[4]
            val defense = pair[5]
            val element = pair[6]
            var tomesOfChaosDlc = false

            if (pair.size > 7) {
                tomesOfChaosDlc = true
            }

            magicArray.add(
                MagicItem(
                    type = type,
                    name = spellName,
                    try {
                        staCost.toInt()
                    } catch (ex: NumberFormatException) {
                        -1
                    },
                    description = description,
                    range = range,
                    duration = duration,
                    defense = defense,
                    element = element,
                    isTomesOfChaosDLC = tomesOfChaosDlc
                )
            )
        }
        return magicArray
    }

    private fun getRitualListInfo(itemList: Array<String>, type: MagicType): ArrayList<MagicItem> {

        val magicArray: ArrayList<MagicItem> = arrayListOf()

        for (tag in itemList) {
            val pair = tag.split(":").toTypedArray()
            val spellName = pair[0]
            val staCost = pair[1]
            val description = pair[2]
            val preparation = pair[3]
            val difficulty = pair[4]
            val duration = pair[5]
            val components = pair[6]
            var tomesOfChaosDlc = false

            if (pair.size > 7) {
                tomesOfChaosDlc = true
            }

            magicArray.add(
                MagicItem(
                    type = type,
                    name = spellName,
                    staminaCost = try {
                        staCost.toInt()
                    } catch (ex: NumberFormatException) {
                        -1
                    },
                    description = description,
                    preparation = preparation,
                    difficulty = try {
                        difficulty.toInt()
                    } catch (ex: NumberFormatException) {
                        -1
                    },
                    duration = duration,
                    components = components,
                    isTomesOfChaosDLC = tomesOfChaosDlc
                )
            )
        }
        return magicArray
    }

    private fun getHexListInfo(itemList: Array<String>): ArrayList<MagicItem> {

        val magicArray: ArrayList<MagicItem> = arrayListOf()

        for (tag in itemList) {
            val pair = tag.split(":").toTypedArray()
            val spellName = pair[0]
            val staCost = pair[1]
            val description = pair[2]
            val danger = pair[3]
            val lift = pair[4]
            var tomesOfChaosDlc = false

            if (pair.size > 5) {
                tomesOfChaosDlc = true
            }

            magicArray.add(
                MagicItem(
                    type = MagicType.HEX,
                    name = spellName,
                    staminaCost = try {
                        staCost.toInt()
                    } catch (ex: NumberFormatException) {
                        -1
                    },
                    description = description,
                    danger = danger,
                    requirementToLift = lift,
                    isTomesOfChaosDLC = tomesOfChaosDlc
                )
            )
        }
        return magicArray
    }

    private fun getInvocationListInfo(itemList: Array<String>, type: MagicType): ArrayList<MagicItem> {

        val magicArray: ArrayList<MagicItem> = arrayListOf()

        for (tag in itemList) {
            val pair = tag.split(":").toTypedArray()
            val spellName = pair[0]
            val staCost = pair[1]
            val description = pair[2]
            val range = pair[3]
            val duration = pair[4]
            val defense = pair[5]
            var tomesOfChaosDlc = false

            if (pair.size > 6) {
                tomesOfChaosDlc = true
            }

            magicArray.add(
                MagicItem(
                    type = type,
                    name = spellName,
                    try {
                        staCost.toInt()
                    } catch (ex: NumberFormatException) {
                        -1
                    },
                    description = description,
                    range = range,
                    duration = duration,
                    defense = defense,
                    isTomesOfChaosDLC = tomesOfChaosDlc
                )
            )
        }
        return magicArray
    }

    private fun getMagicalGiftListInfo(itemList: Array<String>, type: MagicType): ArrayList<MagicItem> {

        val magicArray: ArrayList<MagicItem> = arrayListOf()

        for (tag in itemList) {
            val pair = tag.split(":").toTypedArray()
            val spellName = pair[0]
            val staCost = pair[1]
            val description = pair[2]
            val sideEffect = pair[3]
            val spellCastingDc = pair[4]

            magicArray.add(
                MagicItem(
                    type = type,
                    name = spellName,
                    staminaCost = try {
                        staCost.toInt()
                    } catch (ex: NumberFormatException) {
                        0
                    },
                    description = description,
                    requirementToLift = sideEffect,
                    range = spellCastingDc
                )
            )
        }
        return magicArray
    }

}
