package com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases

import com.witcher.thewitcherrpg.core.Resource
import com.witcher.thewitcherrpg.core.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class DeleteCharacterUseCase @Inject constructor(
    private val repository: CharacterRepository
){
    operator fun invoke(characterId: Int): Flow<Resource<String>> = flow{
        emit(Resource.Loading())

        try {
            repository.getCharacterById(characterId).collect {
                repository.deleteChar(it)
            }
            emit(Resource.Success("Character Deleted Successfully"))
        } catch (ex: Exception){
            emit(Resource.Error(ex.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}