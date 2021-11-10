package com.example.thewitcherrpg.feature_character_sheet.domain.use_cases.stats

import com.example.thewitcherrpg.core.Resource
import javax.inject.Inject

class OnStatChangeUseCase @Inject constructor() {

    operator fun invoke(
        value: Int,
        ip: Int,
        increase: Boolean,
        inCharacterCreation: Boolean
    ): Resource<Pair<Int, Int>> {

        var newIP = ip
        var newVal = value

        if (!inCharacterCreation) {
            if (increase) {
                if (ip >= newVal * 10) {
                    if (newVal == 0 && ip >= 10) {
                        newVal = 1
                        newIP -= 10
                    } else {
                        newIP -= newVal * 10
                        newVal += 1
                    }
                } else return Resource.Error("Not enough IP")
            }

            if (!increase) {
                if (newVal > 0) {

                    newIP += if (newVal == 1) 10
                    else ((newVal - 1) * 10)

                    newVal -= 1

                } else return Resource.Error("Cannot go below 0")
            }
        } else {
            if (increase) {
                if (ip > 0 && newVal < 10) {
                    newIP -= 1
                    newVal += 1
                } else return Resource.Error("Not enough IP")
            }

            if (!increase) {
                if (newVal > 0) {
                    newIP += 1
                    newVal -= 1
                } else return Resource.Error("Cannot go below 0")
            }
        }
        return Resource.Success(Pair(newIP, newVal))
    }
}