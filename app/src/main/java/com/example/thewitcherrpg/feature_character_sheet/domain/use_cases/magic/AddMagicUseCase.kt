package com.example.thewitcherrpg.feature_character_sheet.domain.use_cases.magic

import com.example.thewitcherrpg.TheWitcherTRPGApp
import com.example.thewitcherrpg.feature_character_sheet.presentation.magic.MagicItem
import com.example.thewitcherrpg.feature_character_sheet.presentation.magic.MagicType
import javax.inject.Inject

class AddMagicUseCase @Inject constructor() {

    lateinit var magicItemInfo: String

    operator fun invoke(magic: String, magicType: MagicType, magicList: ArrayList<MagicItem>) {

        when (magicType) {
            MagicType.NOVICE_SPELL -> {

            }

        }
    }

    fun getMagicItemInfo(listData: Int){
        magicItemInfo = TheWitcherTRPGApp.getContext()?.resources?.getStringArray(listData).toString()
    }
}