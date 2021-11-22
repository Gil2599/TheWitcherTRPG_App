package com.example.thewitcherrpg.feature_character_sheet.domain.item_models

import android.os.Parcelable
import com.example.thewitcherrpg.feature_character_sheet.domain.item_types.EquipmentTypes
import com.example.thewitcherrpg.feature_character_sheet.domain.item_types.WeaponTypes
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class WeaponItem(
    var name: String,
    var damageType: String,
    var weaponAccuracy: Int,
    var availability: String,
    var damage: String,
    var reliability: Int,
    var currentReliability: Int,
    var hands: Int,
    var rng: String,
    var effect: ArrayList<String>,
    var concealment: String,
    var enhancements: Int,
    var weight: Float,
    var cost: Int,
    var type: WeaponTypes,
    var focus: Int = 0,
    val uniqueID: UUID = UUID.randomUUID()
) : Parcelable