package com.example.thewitcherrpg.core.data.repository

import com.example.thewitcherrpg.core.data.data_source.CharacterDao
import com.example.thewitcherrpg.core.domain.model.Character
import com.example.thewitcherrpg.core.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CharacterRepositoryImpl @Inject constructor(
    private val charDao: CharacterDao
    ): CharacterRepository {

    override fun getCharacters(): Flow<List<Character>> {
        return charDao.readAllData()
    }

    override suspend fun addChar(character: Character){
        charDao.addChar(character)
    }

    override fun getCharacterById(id: Int): Flow<Character>{
        return charDao.getCharacterById(id)
    }

    override suspend fun deleteChar(character: Character){
        charDao.deleteChar(character)
    }

    override suspend fun updateChar(character: Character){
        charDao.updateChar(character)
    }

}