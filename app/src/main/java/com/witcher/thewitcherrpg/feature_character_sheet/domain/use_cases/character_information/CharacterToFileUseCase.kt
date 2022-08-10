package com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.character_information

import android.app.Application
import android.content.ContextWrapper
import com.witcher.thewitcherrpg.core.Resource
import com.witcher.thewitcherrpg.core.domain.model.Character
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.*
import javax.inject.Inject

class CharacterToFileUseCase @Inject constructor(
    private val application: Application
) {

    operator fun invoke(character: Character): Flow<Resource<File>> = flow {
        val cw = ContextWrapper(application.applicationContext)
        val directory: File? = cw.getExternalFilesDir("characterDir")

        val myPath = File(directory, "${character.name}.wch")

        writeObjectToFile(character, myPath)
        emit(Resource.Success(myPath))
    }


    @Throws(IOException::class)
    fun writeObjectToFile(character: Character, file: File?) {
        FileOutputStream(file).use { fos ->
            ObjectOutputStream(fos).use { oos ->
                oos.writeObject(character)
                oos.flush()
            }
        }
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    fun readObjectFromFile(inputStream: InputStream): Character? {
        var result: Character? = null
        ObjectInputStream(inputStream).use { ois ->
            result = ois.readObject() as Character
        }
        return result
    }
}