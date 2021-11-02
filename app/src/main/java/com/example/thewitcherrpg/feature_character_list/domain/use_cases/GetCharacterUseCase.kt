package com.example.thewitcherrpg.feature_character_list.domain.use_cases

import com.example.thewitcherrpg.core.domain.model.Character
import com.example.thewitcherrpg.core.domain.repository.CharacterRepository

class GetCharacterUseCase(
    private val repository: CharacterRepository
) {

    suspend operator fun invoke(id: Int): Character? {
        return repository.getCharacterById(id)
    }
}