package com.witcher.thewitcherrpg.core.presentation

//import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.witcher.thewitcherrpg.core.Constants
import com.witcher.thewitcherrpg.core.Resource
import com.witcher.thewitcherrpg.core.dataStoreRepository.DataStoreRepository
import com.witcher.thewitcherrpg.core.domain.model.Character
import com.witcher.thewitcherrpg.core.domain.model.CustomEquipment
import com.witcher.thewitcherrpg.core.domain.model.CustomMagic
import com.witcher.thewitcherrpg.core.domain.model.CustomWeapon
import com.witcher.thewitcherrpg.core.firebase.FirebaseEvents
import com.witcher.thewitcherrpg.feature_character_creation.domain.use_cases.*
import com.witcher.thewitcherrpg.feature_character_creation.presentation.CharacterState
import com.witcher.thewitcherrpg.feature_character_sheet.domain.item_types.EquipmentTypes
import com.witcher.thewitcherrpg.feature_character_sheet.domain.item_types.MagicType
import com.witcher.thewitcherrpg.feature_character_sheet.domain.item_types.WeaponTypes
import com.witcher.thewitcherrpg.feature_character_sheet.domain.models.*
import com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.CheckIfDataChangedUseCase
import com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.DeleteCharacterUseCase
import com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.GetCharacterUseCase
import com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.SaveCharacterUseCase
import com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.character_information.*
import com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.equipment.*
import com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.magic.CastMagicUseCase
import com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.magic.GetCustomMagicUseCase
import com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.magic.GetMagicListUseCase
import com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.profession_tree.OnProfessionSkillChangeUseCase
import com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.skills.OnSkillChangeUseCase
import com.witcher.thewitcherrpg.feature_character_sheet.domain.use_cases.stats.OnStatChangeUseCase
import com.witcher.thewitcherrpg.feature_custom_attributes.domain.DeleteCustomEquipmentUseCase
import com.witcher.thewitcherrpg.feature_custom_attributes.domain.DeleteCustomMagicUseCase
import com.witcher.thewitcherrpg.feature_custom_attributes.domain.DeleteCustomWeaponUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class MainCharacterViewModel @Inject constructor(
    private val characterCreationUseCases: CharacterCreationUseCases,
    private val onSkillChangeUseCase: OnSkillChangeUseCase,
    private val onStatChangeUseCase: OnStatChangeUseCase,
    private val getCharacterUseCase: GetCharacterUseCase,
    private val saveImageUseCase: SaveImageUseCase,
    private val onProfessionSkillChangeUseCase: OnProfessionSkillChangeUseCase,
    private val getMagicListUseCase: GetMagicListUseCase,
    private val castMagicUseCase: CastMagicUseCase,
    private val getEquipmentListUseCase: GetEquipmentListUseCase,
    private val saveCharacterUseCase: SaveCharacterUseCase,
    private val deleteCharacterUseCase: DeleteCharacterUseCase,
    private val checkIfDataChangedUseCase: CheckIfDataChangedUseCase,
    private val getWeaponListUseCase: GetWeaponListUseCase,
    private val getProfessionSkillsUseCase: GetProfessionSkillsUseCase,
    private val getProfessionGearUseCase: GetProfessionGearUseCase,
    private val getProfessionSpecialUseCase: GetProfessionSpecialUseCase,
    private val getArmorSetListUseCase: GetArmorSetListUseCase,
    private val getArmorFromArmorSetUseCase: GetArmorFromArmorSetUseCase,
    private val dataStore: DataStoreRepository,
    private val characterToFileUseCase: CharacterToFileUseCase,
    private val getCustomMagicUseCase: GetCustomMagicUseCase,
    private val deleteCustomMagicUseCase: DeleteCustomMagicUseCase,
    private val getCustomWeaponsUseCase: GetCustomWeaponsUseCase,
    private val getCustomEquipmentUseCase: GetCustomEquipmentUseCase,
    private val deleteCustomWeaponUseCase: DeleteCustomWeaponUseCase,
    private val deleteCustomEquipmentUseCase: DeleteCustomEquipmentUseCase,
) : ViewModel() {
    private val firebaseAnalytics = Firebase.analytics

    init {
        fetchCustomWeapons()
        fetchCustomMagic()
        fetchCustomEquipment()
    }

    val isDarkModeEnabled = dataStore.readDarkMode

    private var _id = MutableStateFlow(70)
    val id = _id.asStateFlow()

    val _saveAvailable = MutableLiveData(false)
    //val saveAvailable: LiveData<Int> = _saveAvailable

    //ViewModel States
    private val _inCharacterCreation = mutableStateOf(false)
    val inCharacterCreation: State<Boolean> = _inCharacterCreation

    private val _addState = MutableStateFlow(CharacterState())
    val addState = _addState.asStateFlow()

    private val _dataChangedState = MutableStateFlow(CharacterState())
    val dataChangedState = _dataChangedState.asStateFlow()

    private val _deleteState = MutableStateFlow(CharacterState())
    val deleteState = _deleteState.asStateFlow()

    val _editStatMode = MutableStateFlow(false)
    val editStatMode = _editStatMode.asStateFlow()

    val charInfoMode = dataStore.readCharInfoMode
    val statsInfoMode = dataStore.readStatsInfoMode
    val skillsInfoMode = dataStore.readSkillsInfoMode
    val skillTreeInfoMode = dataStore.readSkillTreeInfoMode

    private val _image = MutableStateFlow("")
    val image = _image.asStateFlow()

    private val _imageBitmap = MutableStateFlow<Bitmap?>(null)
    val imageBitmap = _imageBitmap.asStateFlow()

    private val _tempImage = MutableStateFlow("")
    val tempImage = _tempImage.asStateFlow()

    val name = MutableLiveData("")

    val age = MutableLiveData("")

    val clothing = MutableLiveData("")
    val hairStyle = MutableLiveData("")
    val personality = MutableLiveData("")
    val affectations = MutableLiveData("")
    val valuedPerson = MutableLiveData("")
    val values = MutableLiveData("")
    val feelingsOnPeople = MutableLiveData("")
    val socialStanding = MutableLiveData("")
    val reputation = MutableLiveData("")

    private var _lifeEvents = MutableStateFlow(arrayListOf<LifeEvent>())
    val lifeEvents = _lifeEvents.asStateFlow()

    private val _gender = MutableStateFlow("")
    val gender = _gender.asStateFlow()

    private val _race = MutableStateFlow("")
    val race = _race.asStateFlow()

    private val _profession = mutableStateOf(Constants.Professions.BARD)
    val profession: State<Constants.Professions> = _profession

    private val _definingSkill = MutableStateFlow("")
    val definingSkill = _definingSkill.asStateFlow()

    private val _definingSkillInfo = MutableStateFlow("")
    val definingSkillInfo = _definingSkillInfo.asStateFlow()

    private val _racePerks = MutableStateFlow("")
    val racePerks = _racePerks.asStateFlow()

    private var _ip = MutableStateFlow(70)
    val ip = _ip.asStateFlow()

    private var _crowns = MutableStateFlow(0)
    val crowns = _crowns.asStateFlow()

    //Profession Skills
    private var _professionSkillA1 = MutableStateFlow(0)
    val professionSkillA1 = _professionSkillA1.asStateFlow()

    private var _professionSkillA2 = MutableStateFlow(0)
    val professionSkillA2 = _professionSkillA2.asStateFlow()

    private var _professionSkillA3 = MutableStateFlow(0)
    val professionSkillA3 = _professionSkillA3.asStateFlow()

    private var _professionSkillB1 = MutableStateFlow(0)
    val professionSkillB1 = _professionSkillB1.asStateFlow()

    private var _professionSkillB2 = MutableStateFlow(0)
    val professionSkillB2 = _professionSkillB2.asStateFlow()

    private var _professionSkillB3 = MutableStateFlow(0)
    val professionSkillB3 = _professionSkillB3.asStateFlow()

    private var _professionSkillC1 = MutableStateFlow(0)
    val professionSkillC1 = _professionSkillC1.asStateFlow()

    private var _professionSkillC2 = MutableStateFlow(0)
    val professionSkillC2 = _professionSkillC2.asStateFlow()

    private var _professionSkillC3 = MutableStateFlow(0)
    val professionSkillC3 = _professionSkillC3.asStateFlow()

    //Stats
    private var _intelligence = MutableStateFlow(0)
    val intelligence = _intelligence.asStateFlow()

    private var _intelligenceModifier = MutableStateFlow(0)
    val intelligenceModifier = _intelligenceModifier.asStateFlow()

    private var _ref = MutableStateFlow(0)
    val ref = _ref.asStateFlow()

    private var _refModifier = MutableStateFlow(0)
    val refModifier = _refModifier.asStateFlow()

    private var _dex = MutableStateFlow(0)
    val dex = _dex.asStateFlow()

    private var _dexModifier = MutableStateFlow(0)
    val dexModifier = _dexModifier.asStateFlow()

    private var _body = MutableStateFlow(0)
    val body = _body.asStateFlow()

    private var _bodyModifier = MutableStateFlow(0)
    val bodyModifier = _bodyModifier.asStateFlow()

    private var _spd = MutableStateFlow(0)
    val spd = _spd.asStateFlow()

    private var _spdModifier = MutableStateFlow(0)
    val spdModifier = _spdModifier.asStateFlow()

    private var _emp = MutableStateFlow(0)
    val emp = _emp.asStateFlow()

    private var _empModifier = MutableStateFlow(0)
    val empModifier = _empModifier.asStateFlow()

    private var _cra = MutableStateFlow(0)
    val cra = _cra.asStateFlow()

    private var _craModifier = MutableStateFlow(0)
    val craModifier = _craModifier.asStateFlow()

    private var _will = MutableStateFlow(0)
    val will = _will.asStateFlow()

    private var _willModifier = MutableStateFlow(0)
    val willModifier = _willModifier.asStateFlow()

    private var _luck = MutableStateFlow(0)
    val luck = _luck.asStateFlow()

    private var _luckModifier = MutableStateFlow(0)
    val luckModifier = _luckModifier.asStateFlow()

    private var _stun = MutableStateFlow(0)
    val stun = _stun.asStateFlow()

    private var _stunModifier = MutableStateFlow(0)
    val stunModifier = _stunModifier.asStateFlow()

    private var _run = MutableStateFlow(0)
    val run = _run.asStateFlow()

    private var _runModifier = MutableStateFlow(0)
    val runModifier = _runModifier.asStateFlow()

    private var _leap = MutableStateFlow(0)
    val leap = _leap.asStateFlow()

    private var _leapModifier = MutableStateFlow(0)
    val leapModifier = _leapModifier.asStateFlow()

    private var _maxHP = MutableStateFlow(0)
    val maxHp = _maxHP.asStateFlow()

    private var _hpModifier = MutableStateFlow(0)
    val hpModifier = _hpModifier.asStateFlow()

    private var _hp = MutableStateFlow(0)
    val hp = _hp.asStateFlow()

    private var _maxSta = MutableStateFlow(0)
    val maxSta = _maxSta.asStateFlow()

    private var _staModifier = MutableStateFlow(0)
    val staModifier = _staModifier.asStateFlow()

    private var _sta = MutableStateFlow(0)
    val sta = _sta.asStateFlow()

    private var _enc = MutableStateFlow(0)
    val enc = _enc.asStateFlow()

    private var _encModifier = MutableStateFlow(0)
    val encModifier = _encModifier.asStateFlow()

    private var _currentEnc = MutableStateFlow(0F)
    val currentEnc = _currentEnc.asStateFlow()

    private var _encWithModifier = MutableStateFlow(0)
    val encWithModifier = _encWithModifier.asStateFlow()

    private var _rec = MutableStateFlow(0)
    val rec = _rec.asStateFlow()

    private var _recModifier = MutableStateFlow(0)
    val recModifier = _recModifier.asStateFlow()

    private var _punch = MutableStateFlow("1d6 +2")
    val punch = _punch.asStateFlow()

    private var _kick = MutableStateFlow("1d6 +6")
    val kick = _kick.asStateFlow()

    private var _maxHPWithModifier = MutableStateFlow(0)
    val maxHPWithModifier = _maxHPWithModifier.asStateFlow()

    private var _maxSTAWithModifier = MutableStateFlow(0)
    val maxSTAWithModifier = _maxSTAWithModifier.asStateFlow()

    //Skills
    private var _definingSkillValue = MutableStateFlow(0)
    val definingSkillValue = _definingSkillValue.asStateFlow()

    private var _awareness = MutableStateFlow(0)
    val awareness = _awareness.asStateFlow()

    private var _awarenessModifier = MutableStateFlow(0)
    val awarenessModifier = _awarenessModifier.asStateFlow()

    private var _business = MutableStateFlow(0)
    val business = _business.asStateFlow()

    private var _businessModifier = MutableStateFlow(0)
    val businessModifier = _businessModifier.asStateFlow()

    private var _deduction = MutableStateFlow(0)
    val deduction = _deduction.asStateFlow()

    private var _deductionModifier = MutableStateFlow(0)
    val deductionModifier = _deductionModifier.asStateFlow()

    private var _education = MutableStateFlow(0)
    val education = _education.asStateFlow()

    private var _educationModifier = MutableStateFlow(0)
    val educationModifier = _educationModifier.asStateFlow()

    private var _commonSpeech = MutableStateFlow(0)
    val commonSpeech = _commonSpeech.asStateFlow()

    private var _commonSpeechModifier = MutableStateFlow(0)
    val commonSpeechModifier = _commonSpeechModifier.asStateFlow()

    private var _elderSpeech = MutableStateFlow(0)
    val elderSpeech = _elderSpeech.asStateFlow()

    private var _elderSpeechModifier = MutableStateFlow(0)
    val elderSpeechModifier = _elderSpeechModifier.asStateFlow()

    private var _dwarven = MutableStateFlow(0)
    val dwarven = _dwarven.asStateFlow()

    private var _dwarvenModifier = MutableStateFlow(0)
    val dwarvenModifier = _dwarvenModifier.asStateFlow()

    private var _monsterLore = MutableStateFlow(0)
    val monsterLore = _monsterLore.asStateFlow()

    private var _monsterLoreModifier = MutableStateFlow(0)
    val monsterLoreModifier = _monsterLoreModifier.asStateFlow()

    private var _socialEtiquette = MutableStateFlow(0)
    val socialEtiquette = _socialEtiquette.asStateFlow()

    private var _socialEtiquetteModifier = MutableStateFlow(0)
    val socialEtiquetteModifier = _socialEtiquetteModifier.asStateFlow()

    private var _streetwise = MutableStateFlow(0)
    val streetwise = _streetwise.asStateFlow()

    private var _streetwiseModifier = MutableStateFlow(0)
    val streetwiseModifier = _streetwiseModifier.asStateFlow()

    private var _tactics = MutableStateFlow(0)
    val tactics = _tactics.asStateFlow()

    private var _tacticsModifier = MutableStateFlow(0)
    val tacticsModifier = _tacticsModifier.asStateFlow()

    private var _teaching = MutableStateFlow(0)
    val teaching = _teaching.asStateFlow()

    private var _teachingModifier = MutableStateFlow(0)
    val teachingModifier = _teachingModifier.asStateFlow()

    private var _wildernessSurvival = MutableStateFlow(0)
    val wildernessSurvival = _wildernessSurvival.asStateFlow()

    private var _wildernessSurvivalModifier = MutableStateFlow(0)
    val wildernessSurvivalModifier = _wildernessSurvivalModifier.asStateFlow()

    private var _brawling = MutableStateFlow(0)
    val brawling = _brawling.asStateFlow()

    private var _brawlingModifier = MutableStateFlow(0)
    val brawlingModifier = _brawlingModifier.asStateFlow()

    private var _dodgeEscape = MutableStateFlow(0)
    val dodgeEscape = _dodgeEscape.asStateFlow()

    private var _dodgeEscapeModifier = MutableStateFlow(0)
    val dodgeEscapeModifier = _dodgeEscapeModifier.asStateFlow()

    private var _melee = MutableStateFlow(0)
    val melee = _melee.asStateFlow()

    private var _meleeModifier = MutableStateFlow(0)
    val meleeModifier = _meleeModifier.asStateFlow()

    private var _riding = MutableStateFlow(0)
    val riding = _riding.asStateFlow()

    private var _ridingModifier = MutableStateFlow(0)
    val ridingModifier = _ridingModifier.asStateFlow()

    private var _sailing = MutableStateFlow(0)
    val sailing = _sailing.asStateFlow()

    private var _sailingModifier = MutableStateFlow(0)
    val sailingModifier = _sailingModifier.asStateFlow()

    private var _smallBlades = MutableStateFlow(0)
    val smallBlades = _smallBlades.asStateFlow()

    private var _smallBladesModifier = MutableStateFlow(0)
    val smallBladesModifier = _smallBladesModifier.asStateFlow()

    private var _staffSpear = MutableStateFlow(0)
    val staffSpear = _staffSpear.asStateFlow()

    private var _staffSpearModifier = MutableStateFlow(0)
    val staffSpearModifier = _staffSpearModifier.asStateFlow()

    private var _swordsmanship = MutableStateFlow(0)
    val swordsmanship = _swordsmanship.asStateFlow()

    private var _swordsmanshipModifier = MutableStateFlow(0)
    val swordsmanshipModifier = _swordsmanshipModifier.asStateFlow()

    private var _archery = MutableStateFlow(0)
    val archery = _archery.asStateFlow()

    private var _archeryModifier = MutableStateFlow(0)
    val archeryModifier = _archeryModifier.asStateFlow()

    private var _athletics = MutableStateFlow(0)
    val athletics = _athletics.asStateFlow()

    private var _athleticsModifier = MutableStateFlow(0)
    val athleticsModifier = _athleticsModifier.asStateFlow()

    private var _crossbow = MutableStateFlow(0)
    val crossbow = _crossbow.asStateFlow()

    private var _crossbowModifier = MutableStateFlow(0)
    val crossbowModifier = _crossbowModifier.asStateFlow()

    private var _sleightOfHand = MutableStateFlow(0)
    val sleightOfHand = _sleightOfHand.asStateFlow()

    private var _sleightOfHandModifier = MutableStateFlow(0)
    val sleightOfHandModifier = _sleightOfHandModifier.asStateFlow()

    private var _stealth = MutableStateFlow(0)
    val stealth = _stealth.asStateFlow()

    private var _stealthModifier = MutableStateFlow(0)
    val stealthModifier = _stealthModifier.asStateFlow()

    private var _physique = MutableStateFlow(0)
    val physique = _physique.asStateFlow()

    private var _physiqueModifier = MutableStateFlow(0)
    val physiqueModifier = _physiqueModifier.asStateFlow()

    private var _endurance = MutableStateFlow(0)
    val endurance = _endurance.asStateFlow()

    private var _enduranceModifier = MutableStateFlow(0)
    val enduranceModifier = _enduranceModifier.asStateFlow()

    private var _charisma = MutableStateFlow(0)
    val charisma = _charisma.asStateFlow()

    private var _charismaModifier = MutableStateFlow(0)
    val charismaModifier = _charismaModifier.asStateFlow()

    private var _deceit = MutableStateFlow(0)
    val deceit = _deceit.asStateFlow()

    private var _deceitModifier = MutableStateFlow(0)
    val deceitModifier = _deceitModifier.asStateFlow()

    private var _fineArts = MutableStateFlow(0)
    val fineArts = _fineArts.asStateFlow()

    private var _fineArtsModifier = MutableStateFlow(0)
    val fineArtsModifier = _fineArtsModifier.asStateFlow()

    private var _gambling = MutableStateFlow(0)
    val gambling = _gambling.asStateFlow()

    private var _gamblingModifier = MutableStateFlow(0)
    val gamblingModifier = _gamblingModifier.asStateFlow()

    private var _groomingAndStyle = MutableStateFlow(0)
    val groomingAndStyle = _groomingAndStyle.asStateFlow()

    private var _groomingAndStyleModifier = MutableStateFlow(0)
    val groomingAndStyleModifier = _groomingAndStyleModifier.asStateFlow()

    private var _humanPerception = MutableStateFlow(0)
    val humanPerception = _humanPerception.asStateFlow()

    private var _humanPerceptionModifier = MutableStateFlow(0)
    val humanPerceptionModifier = _humanPerceptionModifier.asStateFlow()

    private var _leadership = MutableStateFlow(0)
    val leadership = _leadership.asStateFlow()

    private var _leadershipModifier = MutableStateFlow(0)
    val leadershipModifier = _leadershipModifier.asStateFlow()

    private var _persuasion = MutableStateFlow(0)
    val persuasion = _persuasion.asStateFlow()

    private var _persuasionModifier = MutableStateFlow(0)
    val persuasionModifier = _persuasionModifier.asStateFlow()

    private var _performance = MutableStateFlow(0)
    val performance = _performance.asStateFlow()

    private var _performanceModifier = MutableStateFlow(0)
    val performanceModifier = _performanceModifier.asStateFlow()

    private var _seduction = MutableStateFlow(0)
    val seduction = _seduction.asStateFlow()

    private var _seductionModifier = MutableStateFlow(0)
    val seductionModifier = _seductionModifier.asStateFlow()

    private var _alchemy = MutableStateFlow(0)
    val alchemy = _alchemy.asStateFlow()

    private var _alchemyModifier = MutableStateFlow(0)
    val alchemyModifier = _alchemyModifier.asStateFlow()

    private var _crafting = MutableStateFlow(0)
    val crafting = _crafting.asStateFlow()

    private var _craftingModifier = MutableStateFlow(0)
    val craftingModifier = _craftingModifier.asStateFlow()

    private var _disguise = MutableStateFlow(0)
    val disguise = _disguise.asStateFlow()

    private var _disguiseModifier = MutableStateFlow(0)
    val disguiseModifier = _disguiseModifier.asStateFlow()

    private var _firstAid = MutableStateFlow(0)
    val firstAid = _firstAid.asStateFlow()

    private var _firstAidModifier = MutableStateFlow(0)
    val firstAidModifier = _firstAidModifier.asStateFlow()

    private var _forgery = MutableStateFlow(0)
    val forgery = _forgery.asStateFlow()

    private var _forgeryModifier = MutableStateFlow(0)
    val forgeryModifier = _forgeryModifier.asStateFlow()

    private var _pickLock = MutableStateFlow(0)
    val pickLock = _pickLock.asStateFlow()

    private var _pickLockModifier = MutableStateFlow(0)
    val pickLockModifier = _pickLockModifier.asStateFlow()

    private var _trapCrafting = MutableStateFlow(0)
    val trapCrafting = _trapCrafting.asStateFlow()

    private var _trapCraftingModifier = MutableStateFlow(0)
    val trapCraftingModifier = _trapCraftingModifier.asStateFlow()

    private var _courage = MutableStateFlow(0)
    val courage = _courage.asStateFlow()

    private var _courageModifier = MutableStateFlow(0)
    val courageModifier = _courageModifier.asStateFlow()

    private var _hexWeaving = MutableStateFlow(0)
    val hexWeaving = _hexWeaving.asStateFlow()

    private var _hexWeavingModifier = MutableStateFlow(0)
    val hexWeavingModifier = _hexWeavingModifier.asStateFlow()

    private var _intimidation = MutableStateFlow(0)
    val intimidation = _intimidation.asStateFlow()

    private var _intimidationModifier = MutableStateFlow(0)
    val intimidationModifier = _intimidationModifier.asStateFlow()

    private var _spellCasting = MutableStateFlow(0)
    val spellCasting = _spellCasting.asStateFlow()

    private var _spellCastingModifier = MutableStateFlow(0)
    val spellCastingModifier = _spellCastingModifier.asStateFlow()

    private var _resistMagic = MutableStateFlow(0)
    val resistMagic = _resistMagic.asStateFlow()

    private var _resistMagicModifier = MutableStateFlow(0)
    val resistMagicModifier = _resistMagicModifier.asStateFlow()

    private var _resistCoercion = MutableStateFlow(0)
    val resistCoercion = _resistCoercion.asStateFlow()

    private var _resistCoercionModifier = MutableStateFlow(0)
    val resistCoercionModifier = _resistCoercionModifier.asStateFlow()

    private var _ritualCrafting = MutableStateFlow(0)
    val ritualCrafting = _ritualCrafting.asStateFlow()

    private var _ritualCraftingModifier = MutableStateFlow(0)
    val ritualCraftingModifier = _ritualCraftingModifier.asStateFlow()

    //##### Magic #################################################
    private var _vigor = MutableStateFlow(0)
    val vigor = _vigor.asStateFlow()

    private var _focus = MutableStateFlow(0)
    val focus = _focus.asStateFlow()

    private var _magicalGiftsEnabled = false
    val magicalGiftsEnabled
        get() = _magicalGiftsEnabled

    //Gifts
    private var _minorGifts = MutableStateFlow(arrayListOf<MagicItem>())
    val minorGifts = _minorGifts.asStateFlow()

    private var _majorGifts = MutableStateFlow(arrayListOf<MagicItem>())
    val majorGifts = _majorGifts.asStateFlow()

    //Mages
    private var _noviceSpellList = MutableStateFlow(arrayListOf<MagicItem>())
    val noviceSpellList = _noviceSpellList.asStateFlow()

    private var _journeymanSpellList = MutableStateFlow(arrayListOf<MagicItem>())
    val journeymanSpellList = _journeymanSpellList.asStateFlow()

    private var _masterSpellList = MutableStateFlow(arrayListOf<MagicItem>())
    val masterSpellList = _masterSpellList.asStateFlow()

    //Priests
    private var _noviceDruidInvocations = MutableStateFlow(arrayListOf<MagicItem>())
    val noviceDruidInvocations = _noviceDruidInvocations.asStateFlow()

    private var _journeymanDruidInvocations = MutableStateFlow(arrayListOf<MagicItem>())
    val journeymanDruidInvocations = _journeymanDruidInvocations.asStateFlow()

    private var _masterDruidInvocations = MutableStateFlow(arrayListOf<MagicItem>())
    val masterDruidInvocations = _masterDruidInvocations.asStateFlow()

    private var _hierophantFlaminikaDruidInvocations = MutableStateFlow(arrayListOf<MagicItem>())
    val hierophantFlaminikaDruidInvocations = _hierophantFlaminikaDruidInvocations.asStateFlow()

    private var _novicePreacherInvocations = MutableStateFlow(arrayListOf<MagicItem>())
    val novicePreacherInvocations = _novicePreacherInvocations.asStateFlow()

    private var _journeymanPreacherInvocations = MutableStateFlow(arrayListOf<MagicItem>())
    val journeymanPreacherInvocations = _journeymanPreacherInvocations.asStateFlow()

    private var _masterPreacherInvocations = MutableStateFlow(arrayListOf<MagicItem>())
    val masterPreacherInvocations = _masterPreacherInvocations.asStateFlow()

    private var _archPriestInvocations = MutableStateFlow(arrayListOf<MagicItem>())
    val archPriestInvocations = _archPriestInvocations.asStateFlow()

    //Signs
    private var _basicSigns = MutableStateFlow(arrayListOf<MagicItem>())
    val basicSigns = _basicSigns.asStateFlow()

    private var _alternateSigns = MutableStateFlow(arrayListOf<MagicItem>())
    val alternateSigns = _alternateSigns.asStateFlow()

    //Rituals
    private var _noviceRitualList = MutableStateFlow(arrayListOf<MagicItem>())
    val noviceRitualList = _noviceRitualList.asStateFlow()

    private var _journeymanRitualList = MutableStateFlow(arrayListOf<MagicItem>())
    val journeymanRitualList = _journeymanRitualList.asStateFlow()

    private var _masterRitualList = MutableStateFlow(arrayListOf<MagicItem>())
    val masterRitualList = _masterRitualList.asStateFlow()

    //Hexes
    private var _hexesList = MutableStateFlow(arrayListOf<MagicItem>())
    val hexesList = _hexesList.asStateFlow()

    //Equipment
    private var _headEquipment = MutableStateFlow(arrayListOf<EquipmentItem>())
    val headEquipment = _headEquipment.asStateFlow()

    private var _equippedHead = MutableStateFlow<EquipmentItem?>(null)
    val equippedHead = _equippedHead.asStateFlow()

    private var _chestEquipment = MutableStateFlow(arrayListOf<EquipmentItem>())
    val chestEquipment = _chestEquipment.asStateFlow()

    private var _equippedChest = MutableStateFlow<EquipmentItem?>(null)
    val equippedChest = _equippedChest.asStateFlow()

    private var _legEquipment = MutableStateFlow(arrayListOf<EquipmentItem>())
    val legEquipment = _legEquipment.asStateFlow()

    private var _equippedLegs = MutableStateFlow<EquipmentItem?>(null)
    val equippedLegs = _equippedLegs.asStateFlow()

    private var _weaponEquipment = MutableStateFlow(arrayListOf<WeaponItem>())
    val weaponEquipment = _weaponEquipment.asStateFlow()

    private var _equippedWeapon = MutableStateFlow<WeaponItem?>(null)
    val equippedWeapon = _equippedWeapon.asStateFlow()

    private var _accessoryEquipment = MutableStateFlow(arrayListOf<EquipmentItem>())
    val accessoryEquipment = _accessoryEquipment.asStateFlow()

    private var _equippedSecondHandShield = MutableStateFlow<EquipmentItem?>(null)
    val equippedSecondHandShield = _equippedSecondHandShield.asStateFlow()

    private var _equippedSecondHandWeapon = MutableStateFlow<WeaponItem?>(null)
    val equippedSecondHandWeapon = _equippedSecondHandWeapon.asStateFlow()

    private var _miscEquipment = MutableStateFlow(arrayListOf<EquipmentItem>())
    val miscEquipment = _miscEquipment.asStateFlow()

    private var _campaignNotes = MutableStateFlow(arrayListOf<CampaignNote>())
    val campaignNotes = _campaignNotes.asStateFlow()

    fun setInCharCreation(inCharacterCreation: Boolean) {
        _inCharacterCreation.value = inCharacterCreation
    }

    private var _customMagicList = MutableStateFlow(listOf<MagicItem>())
    val customMagicList = _customMagicList.asStateFlow()

    private var _customWeaponList = MutableStateFlow(listOf<WeaponItem>())
    val customWeaponList = _customWeaponList.asStateFlow()

    private var _customEquipmentList = MutableStateFlow(listOf<EquipmentItem>())
    val customEquipmentList = _customEquipmentList.asStateFlow()

    fun addCharacter() {

        var startingVigor = 0

        when (_profession.value) {
            Constants.Professions.MAGE -> startingVigor = 5
            Constants.Professions.PRIEST -> startingVigor = 2
            Constants.Professions.DRUID -> startingVigor = 2
            Constants.Professions.WITCHER -> startingVigor = 2
            else -> {}
        }
        if (_magicalGiftsEnabled) startingVigor = 2

        characterCreationUseCases.addCharacterUseCase(
            Character(
                id = 0,
                imagePath = _image.value,
                name = name.value!!,
                iP = _ip.value,
                race = _race.value,
                gender = _gender.value,
                age = age.value.toString().toInt(),
                profession = _profession.value,
                magicalGifts = if (_magicalGiftsEnabled) 1 else 0,
                definingSkill = _definingSkill.value,
                definingSkillInfo = _definingSkillInfo.value,
                racePerks = _racePerks.value,
                intelligence = _intelligence.value,
                reflex = _ref.value,
                dexterity = _dex.value,
                body = _body.value,
                speed = _spd.value,
                empathy = _emp.value,
                craftsmanship = _cra.value,
                will = _will.value,
                luck = _luck.value,
                stun = _stun.value,
                run = _run.value,
                leap = _leap.value,
                MaxHP = _maxHP.value,
                hp = _maxHP.value,
                MaxStamina = _maxSta.value,
                stamina = _maxSta.value,
                encumbrance = _enc.value,
                recovery = _rec.value,
                punch = _punch.value,
                kick = _kick.value,
                definingSkillValue = _definingSkillValue.value,
                awareness = _awareness.value,
                business = _business.value,
                deduction = _deduction.value,
                education = _education.value,
                commonSpeech = _commonSpeech.value,
                elderSpeech = _elderSpeech.value,
                dwarven = _dwarven.value,
                monsterLore = _monsterLore.value,
                socialEtiquette = _socialEtiquette.value,
                streetwise = _streetwise.value,
                tactics = _tactics.value,
                teaching = _teaching.value,
                wildernessSurvival = _wildernessSurvival.value,
                brawling = _brawling.value,
                dodgeEscape = _dodgeEscape.value,
                melee = _melee.value,
                riding = _riding.value,
                sailing = _sailing.value,
                smallBlades = _smallBlades.value,
                staffSpear = staffSpear.value,
                swordsmanship = _swordsmanship.value,
                archery = _archery.value,
                athletics = _athletics.value,
                crossbow = _crossbow.value,
                sleightOfHand = _sleightOfHand.value,
                stealth = _stealth.value,
                physique = _physique.value,
                endurance = _endurance.value,
                charisma = _charisma.value,
                deceit = _deceit.value,
                fineArts = _fineArts.value,
                gambling = _gambling.value,
                groomingAndStyle = _groomingAndStyle.value,
                humanPerception = _humanPerception.value,
                leadership = _leadership.value,
                persuasion = _persuasion.value,
                performance = _performance.value,
                seduction = _seduction.value,
                alchemy = _alchemy.value,
                crafting = _crafting.value,
                disguise = _disguise.value,
                firstAid = _firstAid.value,
                forgery = _forgery.value,
                pickLock = _pickLock.value,
                trapCrafting = _trapCrafting.value,
                courage = _courage.value,
                hexWeaving = _hexWeaving.value,
                intimidation = _intimidation.value,
                spellCasting = _spellCasting.value,
                resistMagic = _resistMagic.value,
                resistCoercion = _resistCoercion.value,
                ritualCrafting = _ritualCrafting.value,
                intelligenceModifier = _intelligenceModifier.value,
                reflexModifier = _refModifier.value,
                dexterityModifier = _dexModifier.value,
                bodyModifier = _bodyModifier.value,
                speedModifier = _spdModifier.value,
                empathyModifier = _empModifier.value,
                craftsmanshipModifier = _craModifier.value,
                willModifier = _willModifier.value,
                luckModifier = _luckModifier.value,
                stunModifier = _stunModifier.value,
                runModifier = _runModifier.value,
                leapModifier = _leapModifier.value,
                hpModifier = _hpModifier.value,
                staModifier = _staModifier.value,
                encumbranceModifier = _encModifier.value,
                recoveryModifier = _recModifier.value,
                awarenessModifier = _awarenessModifier.value,
                businessModifier = _businessModifier.value,
                deductionModifier = _deductionModifier.value,
                educationModifier = _educationModifier.value,
                commonSpeechModifier = _commonSpeechModifier.value,
                elderSpeechModifier = _elderSpeechModifier.value,
                dwarvenModifier = _dwarvenModifier.value,
                monsterLoreModifier = _monsterLoreModifier.value,
                socialEtiquetteModifier = _socialEtiquetteModifier.value,
                streetwiseModifier = _streetwiseModifier.value,
                tacticsModifier = _tacticsModifier.value,
                teachingModifier = _teachingModifier.value,
                wildernessSurvivalModifier = _wildernessSurvivalModifier.value,
                brawlingModifier = _brawlingModifier.value,
                dodgeEscapeModifier = _dodgeEscapeModifier.value,
                meleeModifier = _meleeModifier.value,
                ridingModifier = _ridingModifier.value,
                sailingModifier = _sailingModifier.value,
                smallBladesModifier = _smallBladesModifier.value,
                staffSpearModifier = _staffSpearModifier.value,
                swordsmanshipModifier = _swordsmanshipModifier.value,
                archeryModifier = _archeryModifier.value,
                athleticsModifier = _athleticsModifier.value,
                crossbowModifier = _crossbowModifier.value,
                sleightOfHandModifier = _sleightOfHandModifier.value,
                stealthModifier = _stealthModifier.value,
                physiqueModifier = _physiqueModifier.value,
                enduranceModifier = _enduranceModifier.value,
                charismaModifier = _charismaModifier.value,
                deceitModifier = _deceitModifier.value,
                fineArtsModifier = _fineArtsModifier.value,
                gamblingModifier = _gamblingModifier.value,
                groomingAndStyleModifier = _groomingAndStyleModifier.value,
                humanPerceptionModifier = _humanPerceptionModifier.value,
                leadershipModifier = _leadershipModifier.value,
                persuasionModifier = _persuasionModifier.value,
                performanceModifier = _performanceModifier.value,
                seductionModifier = _seductionModifier.value,
                alchemyModifier = _alchemyModifier.value,
                craftingModifier = _craftingModifier.value,
                disguiseModifier = _disguiseModifier.value,
                firstAidModifier = _firstAidModifier.value,
                forgeryModifier = _forgeryModifier.value,
                pickLockModifier = _pickLockModifier.value,
                trapCraftingModifier = _trapCraftingModifier.value,
                courageModifier = _courageModifier.value,
                hexWeavingModifier = _hexWeavingModifier.value,
                intimidationModifier = _intimidationModifier.value,
                spellCastingModifier = _spellCastingModifier.value,
                resistMagicModifier = _resistMagicModifier.value,
                resistCoercionModifier = _resistCoercionModifier.value,
                ritualCraftingModifier = _ritualCraftingModifier.value,
                vigor = startingVigor
            )

        ).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _addState.value = CharacterState(success = true)
                    firebaseAnalytics.logEvent(FirebaseEvents.NEW_CHARACTER_CREATED.name) {
                        param("profession", _profession.value.toString())
                        param("race", _race.value)
                        param("magical_gifts_enabled", _magicalGiftsEnabled.toString())
                    }
                }
                is Resource.Error -> {
                    _addState.value = CharacterState(
                        error = result.message ?: "An unexpected error occurred"
                    )
                    firebaseAnalytics.logEvent(FirebaseEvents.NEW_CHARACTER_CREATE_ERROR.name) {
                        param("create_error", result.message ?: "An unexpected error occurred")
                    }
                }
                is Resource.Loading -> {
                    _addState.value = CharacterState(isLoading = true)
                }
            }

        }.launchIn(viewModelScope)
    }

    private fun fromViewModelToCharacter(): Character? {
        try {
            return Character(
                id = _id.value,
                imagePath = _image.value,
                name = name.value!!,
                iP = _ip.value,
                race = _race.value,
                gender = _gender.value,
                age = age.value.toString().toInt(),
                profession = _profession.value,
                magicalGifts = if (_magicalGiftsEnabled) 1 else 0,
                definingSkill = _definingSkill.value,
                definingSkillInfo = _definingSkillInfo.value,
                racePerks = _racePerks.value,
                crowns = _crowns.value,
                clothing = clothing.value!!,
                hairStyle = hairStyle.value!!,
                personality = personality.value!!,
                affectations = affectations.value!!,
                valuedPerson = valuedPerson.value!!,
                values = values.value!!,
                feelingsOnPeople = feelingsOnPeople.value!!,
                socialStanding = socialStanding.value!!,
                reputation = reputation.value!!,
                lifeEvents = _lifeEvents.value,
                intelligence = _intelligence.value,
                intelligenceModifier = _intelligenceModifier.value,
                reflex = _ref.value,
                reflexModifier = _refModifier.value,
                dexterity = _dex.value,
                dexterityModifier = _dexModifier.value,
                body = _body.value,
                bodyModifier = _bodyModifier.value,
                speed = _spd.value,
                speedModifier = _spdModifier.value,
                empathy = _emp.value,
                empathyModifier = _empModifier.value,
                craftsmanship = _cra.value,
                craftsmanshipModifier = _craModifier.value,
                will = _will.value,
                willModifier = _willModifier.value,
                luck = _luck.value,
                luckModifier = _luckModifier.value,
                stun = _stun.value,
                stunModifier = _stunModifier.value,
                run = _run.value,
                runModifier = _runModifier.value,
                leap = _leap.value,
                leapModifier = _leapModifier.value,
                MaxHP = _maxHP.value,
                hpModifier = _hpModifier.value,
                hp = _hp.value,
                MaxStamina = _maxSta.value,
                staModifier = _staModifier.value,
                stamina = _sta.value,
                encumbrance = _enc.value,
                encumbranceModifier = _encModifier.value,
                currentEncumbrance = _currentEnc.value,
                recovery = _rec.value,
                recoveryModifier = _recModifier.value,
                punch = _punch.value,
                kick = _kick.value,

                definingSkillValue = _definingSkillValue.value,
                awareness = _awareness.value,
                awarenessModifier = _awarenessModifier.value,
                business = _business.value,
                businessModifier = _businessModifier.value,
                deduction = _deduction.value,
                deductionModifier = _deductionModifier.value,
                education = _education.value,
                educationModifier = _educationModifier.value,
                commonSpeech = _commonSpeech.value,
                commonSpeechModifier = _commonSpeechModifier.value,
                elderSpeech = _elderSpeech.value,
                elderSpeechModifier = _elderSpeechModifier.value,
                dwarven = _dwarven.value,
                dwarvenModifier = _dwarvenModifier.value,
                monsterLore = _monsterLore.value,
                monsterLoreModifier = _monsterLoreModifier.value,
                socialEtiquette = _socialEtiquette.value,
                socialEtiquetteModifier = _socialEtiquetteModifier.value,
                streetwise = _streetwise.value,
                streetwiseModifier = _streetwiseModifier.value,
                tactics = _tactics.value,
                tacticsModifier = _tacticsModifier.value,
                teaching = _teaching.value,
                teachingModifier = _teachingModifier.value,
                wildernessSurvival = _wildernessSurvival.value,
                wildernessSurvivalModifier = _wildernessSurvivalModifier.value,
                brawling = _brawling.value,
                brawlingModifier = _brawlingModifier.value,
                dodgeEscape = _dodgeEscape.value,
                dodgeEscapeModifier = _dodgeEscapeModifier.value,
                melee = _melee.value,
                meleeModifier = _meleeModifier.value,
                riding = _riding.value,
                ridingModifier = _ridingModifier.value,
                sailing = _sailing.value,
                sailingModifier = _sailingModifier.value,
                smallBlades = _smallBlades.value,
                smallBladesModifier = _smallBladesModifier.value,
                staffSpear = staffSpear.value,
                staffSpearModifier = staffSpearModifier.value,
                swordsmanship = _swordsmanship.value,
                swordsmanshipModifier = _swordsmanshipModifier.value,
                archery = _archery.value,
                archeryModifier = _archeryModifier.value,
                athletics = _athletics.value,
                athleticsModifier = _athleticsModifier.value,
                crossbow = _crossbow.value,
                crossbowModifier = _crossbowModifier.value,
                sleightOfHand = _sleightOfHand.value,
                sleightOfHandModifier = _sleightOfHandModifier.value,
                stealth = _stealth.value,
                stealthModifier = _stealthModifier.value,
                physique = _physique.value,
                physiqueModifier = _physiqueModifier.value,
                endurance = _endurance.value,
                enduranceModifier = _enduranceModifier.value,
                charisma = _charisma.value,
                charismaModifier = _charismaModifier.value,
                deceit = _deceit.value,
                deceitModifier = _deceitModifier.value,
                fineArts = _fineArts.value,
                fineArtsModifier = _fineArtsModifier.value,
                gambling = _gambling.value,
                gamblingModifier = _gamblingModifier.value,
                groomingAndStyle = _groomingAndStyle.value,
                groomingAndStyleModifier = _groomingAndStyleModifier.value,
                humanPerception = _humanPerception.value,
                humanPerceptionModifier = _humanPerceptionModifier.value,
                leadership = _leadership.value,
                leadershipModifier = _leadershipModifier.value,
                persuasion = _persuasion.value,
                persuasionModifier = _persuasionModifier.value,
                performance = _performance.value,
                performanceModifier = _performanceModifier.value,
                seduction = _seduction.value,
                seductionModifier = _seductionModifier.value,
                alchemy = _alchemy.value,
                alchemyModifier = _alchemyModifier.value,
                crafting = _crafting.value,
                craftingModifier = _craftingModifier.value,
                disguise = _disguise.value,
                disguiseModifier = _disguiseModifier.value,
                firstAid = _firstAid.value,
                firstAidModifier = _firstAidModifier.value,
                forgery = _forgery.value,
                forgeryModifier = _forgeryModifier.value,
                pickLock = _pickLock.value,
                pickLockModifier = _pickLockModifier.value,
                trapCrafting = _trapCrafting.value,
                trapCraftingModifier = _trapCraftingModifier.value,
                courage = _courage.value,
                courageModifier = _courageModifier.value,
                hexWeaving = _hexWeaving.value,
                hexWeavingModifier = _hexWeavingModifier.value,
                intimidation = _intimidation.value,
                intimidationModifier = _intimidationModifier.value,
                spellCasting = _spellCasting.value,
                spellCastingModifier = _spellCastingModifier.value,
                resistMagic = _resistMagic.value,
                resistMagicModifier = _resistMagicModifier.value,
                resistCoercion = _resistCoercion.value,
                resistCoercionModifier = _resistCoercionModifier.value,
                ritualCrafting = _ritualCrafting.value,
                ritualCraftingModifier = _ritualCraftingModifier.value,

                professionSkillA1 = _professionSkillA1.value,
                professionSkillA2 = _professionSkillA2.value,
                professionSkillA3 = _professionSkillA3.value,
                professionSkillB1 = _professionSkillB1.value,
                professionSkillB2 = _professionSkillB2.value,
                professionSkillB3 = _professionSkillB3.value,
                professionSkillC1 = _professionSkillC1.value,
                professionSkillC2 = _professionSkillC2.value,
                professionSkillC3 = _professionSkillC3.value,

                vigor = _vigor.value,
                focus = _focus.value,
                minorGifts = _minorGifts.value,
                majorGifts = _majorGifts.value,
                basicSigns = _basicSigns.value,
                alternateSigns = _alternateSigns.value,
                noviceRituals = _noviceRitualList.value,
                journeymanRituals = _journeymanRitualList.value,
                masterRituals = _masterRitualList.value,
                hexes = _hexesList.value,
                noviceSpells = _noviceSpellList.value,
                journeymanSpells = _journeymanSpellList.value,
                masterSpells = _masterSpellList.value,
                noviceDruidInvocations = _noviceDruidInvocations.value,
                journeymanDruidInvocations = _journeymanDruidInvocations.value,
                masterDruidInvocations = _masterDruidInvocations.value,
                novicePreacherInvocations = _novicePreacherInvocations.value,
                journeymanPreacherInvocations = _journeymanPreacherInvocations.value,
                masterPreacherInvocations = _masterPreacherInvocations.value,
                archPriestInvocations = _archPriestInvocations.value,
                hierophantFlaminikaDruidInvocations = _hierophantFlaminikaDruidInvocations.value,

                headEquipment = _headEquipment.value,
                equippedHead = _equippedHead.value,
                chestEquipment = _chestEquipment.value,
                equippedChest = _equippedChest.value,
                legEquipment = _legEquipment.value,
                equippedLegs = _equippedLegs.value,
                accessoryEquipment = _accessoryEquipment.value,
                equippedSecondHandShield = _equippedSecondHandShield.value,
                equippedSecondHandWeapon = _equippedSecondHandWeapon.value,
                miscEquipment = _miscEquipment.value,

                weaponEquipment = _weaponEquipment.value,
                equippedWeapon = _equippedWeapon.value,

                campaignNotes = _campaignNotes.value
            )
        } catch (ex: Exception) {
            return null
        }
    }

    fun saveCharacter() {
        saveImageToInternalStorage()
        fromViewModelToCharacter()?.let {
            saveCharacterUseCase(
                it
            ).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _addState.value = CharacterState(success = true)
                        _saveAvailable.value = false
                        firebaseAnalytics.logEvent(FirebaseEvents.CHARACTER_SAVED.name, null)
                    }
                    is Resource.Error -> {
                        _addState.value = CharacterState(
                            error = result.message ?: "An unexpected error occurred"
                        )
                        firebaseAnalytics.logEvent(FirebaseEvents.CHARACTER_SAVE_ERROR.name) {
                            param("save_error", result.message ?: "An unexpected error occurred")
                        }
                    }
                    is Resource.Loading -> {
                        _addState.value = CharacterState(isLoading = true)
                    }
                }

            }.launchIn(viewModelScope)
        }

    }

    fun checkIfDataChanged(): Boolean {
        return fromViewModelToCharacter()?.let { checkIfDataChangedUseCase(_id.value, it) } ?: false
    }

    fun deleteCharacter(characterId: Int) {
        deleteCharacterUseCase(characterId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _deleteState.value = CharacterState(success = true)
                    firebaseAnalytics.logEvent(FirebaseEvents.CHARACTER_DELETED.name, null)
                }
                is Resource.Error -> {
                    _deleteState.value = CharacterState(
                        error = result.message ?: "An unexpected error occurred"
                    )
                    firebaseAnalytics.logEvent(FirebaseEvents.CHARACTER_DELETE_ERROR.name) {
                        param("character_delete_error", result.message ?: "An unexpected error occurred")
                    }
                }
                is Resource.Loading -> {
                    _deleteState.value = CharacterState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getCharacter(id: Int) {
        getCharacterUseCase(id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val characterData = result.data!!
                    _id.value = characterData.id
                    _image.value = characterData.imagePath
                    name.value = characterData.name
                    _ip.value = characterData.iP
                    _race.value = characterData.race
                    _gender.value = characterData.gender
                    age.value = characterData.age.toString()
                    _profession.value = characterData.profession
                    _magicalGiftsEnabled = characterData.magicalGifts != 0
                    _definingSkill.value = characterData.definingSkill
                    _definingSkillInfo.value = characterData.definingSkillInfo
                    _racePerks.value = characterData.racePerks
                    _crowns.value = characterData.crowns
                    clothing.value = characterData.clothing
                    hairStyle.value = characterData.hairStyle
                    personality.value = characterData.personality
                    affectations.value = characterData.affectations
                    valuedPerson.value = characterData.valuedPerson
                    values.value = characterData.values
                    feelingsOnPeople.value = characterData.feelingsOnPeople
                    socialStanding.value = characterData.socialStanding
                    reputation.value = characterData.reputation
                    _lifeEvents.value = characterData.lifeEvents

                    //Profession Skills
                    _professionSkillA1.value = characterData.professionSkillA1
                    _professionSkillA2.value = characterData.professionSkillA2
                    _professionSkillA3.value = characterData.professionSkillA3
                    _professionSkillB1.value = characterData.professionSkillB1
                    _professionSkillB2.value = characterData.professionSkillB2
                    _professionSkillB3.value = characterData.professionSkillB3
                    _professionSkillC1.value = characterData.professionSkillC1
                    _professionSkillC2.value = characterData.professionSkillC2
                    _professionSkillC3.value = characterData.professionSkillC3

                    //Stats
                    _intelligence.value = characterData.intelligence
                    _intelligenceModifier.value = characterData.intelligenceModifier
                    _ref.value = characterData.reflex
                    _refModifier.value = characterData.reflexModifier
                    _dex.value = characterData.dexterity
                    _dexModifier.value = characterData.dexterityModifier
                    _body.value = characterData.body
                    _bodyModifier.value = characterData.bodyModifier
                    _spd.value = characterData.speed
                    _spdModifier.value = characterData.speedModifier
                    _emp.value = characterData.empathy
                    _empModifier.value = characterData.empathyModifier
                    _cra.value = characterData.craftsmanship
                    _craModifier.value = characterData.craftsmanshipModifier
                    _will.value = characterData.will
                    _willModifier.value = characterData.willModifier
                    _luck.value = characterData.luck
                    _luckModifier.value = characterData.luckModifier
                    _stun.value = characterData.stun
                    _stunModifier.value = characterData.stunModifier
                    _run.value = characterData.run
                    _runModifier.value = characterData.runModifier
                    _leap.value = characterData.leap
                    _leapModifier.value = characterData.leapModifier
                    _maxHP.value = characterData.MaxHP
                    _hpModifier.value = characterData.hpModifier
                    _hp.value = characterData.hp
                    _maxSta.value = characterData.MaxStamina
                    _staModifier.value = characterData.staModifier
                    _sta.value = characterData.stamina
                    _enc.value = characterData.encumbrance
                    _encModifier.value = characterData.encumbranceModifier
                    _rec.value = characterData.recovery
                    _recModifier.value = characterData.recoveryModifier
                    _punch.value = characterData.punch
                    _kick.value = characterData.kick
                    _maxHPWithModifier.value = _maxHP.value.plus(_hpModifier.value)
                    _maxSTAWithModifier.value = _maxSta.value.plus(_staModifier.value)
                    _encWithModifier.value = _enc.value.plus(_encModifier.value)
                    _currentEnc.value = characterData.currentEncumbrance

                    //Skills
                    _definingSkillValue.value = characterData.definingSkillValue
                    _awareness.value = characterData.awareness
                    _awarenessModifier.value = characterData.awarenessModifier
                    _business.value = characterData.business
                    _businessModifier.value = characterData.businessModifier
                    _deduction.value = characterData.deduction
                    _deductionModifier.value = characterData.deductionModifier
                    _education.value = characterData.education
                    _educationModifier.value = characterData.educationModifier
                    _commonSpeech.value = characterData.commonSpeech
                    _commonSpeechModifier.value = characterData.commonSpeechModifier
                    _elderSpeech.value = characterData.elderSpeech
                    _elderSpeechModifier.value = characterData.elderSpeechModifier
                    _dwarven.value = characterData.dwarven
                    _dwarvenModifier.value = characterData.dwarvenModifier
                    _monsterLore.value = characterData.monsterLore
                    _monsterLoreModifier.value = characterData.monsterLoreModifier
                    _socialEtiquette.value = characterData.socialEtiquette
                    _socialEtiquetteModifier.value = characterData.socialEtiquetteModifier
                    _streetwise.value = characterData.streetwise
                    _streetwiseModifier.value = characterData.streetwiseModifier
                    _tactics.value = characterData.tactics
                    _tacticsModifier.value = characterData.tacticsModifier
                    _teaching.value = characterData.teaching
                    _teachingModifier.value = characterData.teachingModifier
                    _wildernessSurvival.value = characterData.wildernessSurvival
                    _wildernessSurvivalModifier.value = characterData.wildernessSurvivalModifier
                    _brawling.value = characterData.brawling
                    _brawlingModifier.value = characterData.brawlingModifier
                    _dodgeEscape.value = characterData.dodgeEscape
                    _dodgeEscapeModifier.value = characterData.dodgeEscapeModifier
                    _melee.value = characterData.melee
                    _meleeModifier.value = characterData.meleeModifier
                    _riding.value = characterData.riding
                    _ridingModifier.value = characterData.ridingModifier
                    _sailing.value = characterData.sailing
                    _sailingModifier.value = characterData.sailingModifier
                    _smallBlades.value = characterData.smallBlades
                    _smallBladesModifier.value = characterData.smallBladesModifier
                    _staffSpear.value = characterData.staffSpear
                    _staffSpearModifier.value = characterData.staffSpearModifier
                    _swordsmanship.value = characterData.swordsmanship
                    _swordsmanshipModifier.value = characterData.swordsmanshipModifier
                    _archery.value = characterData.archery
                    _archeryModifier.value = characterData.archeryModifier
                    _athletics.value = characterData.athletics
                    _athleticsModifier.value = characterData.athleticsModifier
                    _crossbow.value = characterData.crossbow
                    _crossbowModifier.value = characterData.crossbowModifier
                    _sleightOfHand.value = characterData.sleightOfHand
                    _sleightOfHandModifier.value = characterData.sleightOfHandModifier
                    _stealth.value = characterData.stealth
                    _stealthModifier.value = characterData.stealthModifier
                    _physique.value = characterData.physique
                    _physiqueModifier.value = characterData.physiqueModifier
                    _endurance.value = characterData.endurance
                    _enduranceModifier.value = characterData.enduranceModifier
                    _charisma.value = characterData.charisma
                    _charismaModifier.value = characterData.charismaModifier
                    _deceit.value = characterData.deceit
                    _deceitModifier.value = characterData.deceitModifier
                    _fineArts.value = characterData.fineArts
                    _fineArtsModifier.value = characterData.fineArtsModifier
                    _gambling.value = characterData.gambling
                    _gamblingModifier.value = characterData.gamblingModifier
                    _groomingAndStyle.value = characterData.groomingAndStyle
                    _groomingAndStyleModifier.value = characterData.groomingAndStyleModifier
                    _humanPerception.value = characterData.humanPerception
                    _humanPerceptionModifier.value = characterData.humanPerceptionModifier
                    _leadership.value = characterData.leadership
                    _leadershipModifier.value = characterData.leadershipModifier
                    _persuasion.value = characterData.persuasion
                    _persuasionModifier.value = characterData.persuasionModifier
                    _performance.value = characterData.performance
                    _performanceModifier.value = characterData.performanceModifier
                    _seduction.value = characterData.seduction
                    _seductionModifier.value = characterData.seductionModifier
                    _alchemy.value = characterData.alchemy
                    _alchemyModifier.value = characterData.alchemyModifier
                    _crafting.value = characterData.crafting
                    _craftingModifier.value = characterData.craftingModifier
                    _disguise.value = characterData.disguise
                    _disguiseModifier.value = characterData.disguiseModifier
                    _firstAid.value = characterData.firstAid
                    _firstAidModifier.value = characterData.firstAidModifier
                    _forgery.value = characterData.forgery
                    _forgeryModifier.value = characterData.forgeryModifier
                    _pickLock.value = characterData.pickLock
                    _pickLockModifier.value = characterData.pickLockModifier
                    _trapCrafting.value = characterData.trapCrafting
                    _trapCraftingModifier.value = characterData.trapCraftingModifier
                    _courage.value = characterData.courage
                    _courageModifier.value = characterData.courageModifier
                    _hexWeaving.value = characterData.hexWeaving
                    _hexWeavingModifier.value = characterData.hexWeavingModifier
                    _intimidation.value = characterData.intimidation
                    _intimidationModifier.value = characterData.intimidationModifier
                    _spellCasting.value = characterData.spellCasting
                    _spellCastingModifier.value = characterData.spellCastingModifier
                    _resistMagic.value = characterData.resistMagic
                    _resistMagicModifier.value = characterData.resistMagicModifier
                    _resistCoercion.value = characterData.resistCoercion
                    _resistCoercionModifier.value = characterData.resistCoercionModifier
                    _ritualCrafting.value = characterData.ritualCrafting
                    _ritualCraftingModifier.value = characterData.ritualCraftingModifier

                    //Magic
                    _vigor.value = characterData.vigor
                    _focus.value = characterData.focus

                    //Magical Gifts
                    _minorGifts.value = characterData.minorGifts!!
                    _majorGifts.value = characterData.majorGifts

                    //Mages
                    _noviceSpellList.value = characterData.noviceSpells
                    _journeymanSpellList.value = characterData.journeymanSpells
                    _masterSpellList.value = characterData.masterSpells

                    //Priests
                    _noviceDruidInvocations.value = characterData.noviceDruidInvocations
                    _journeymanDruidInvocations.value = characterData.journeymanDruidInvocations
                    _masterDruidInvocations.value = characterData.masterDruidInvocations

                    _novicePreacherInvocations.value = characterData.novicePreacherInvocations
                    _journeymanPreacherInvocations.value =
                        characterData.journeymanPreacherInvocations
                    _masterPreacherInvocations.value = characterData.masterPreacherInvocations

                    _archPriestInvocations.value = characterData.archPriestInvocations
                    _hierophantFlaminikaDruidInvocations.value = characterData.hierophantFlaminikaDruidInvocations

                    //Signs
                    _basicSigns.value = characterData.basicSigns
                    _alternateSigns.value = characterData.alternateSigns

                    //Rituals
                    _noviceRitualList.value = characterData.noviceRituals
                    _journeymanRitualList.value = characterData.journeymanRituals
                    _masterRitualList.value = characterData.masterRituals

                    //Hexes
                    _hexesList.value = characterData.hexes

                    //Equipment
                    _headEquipment.value = characterData.headEquipment
                    _equippedHead.value = characterData.equippedHead

                    _chestEquipment.value = characterData.chestEquipment
                    _equippedChest.value = characterData.equippedChest

                    _legEquipment.value = characterData.legEquipment
                    _equippedLegs.value = characterData.equippedLegs

                    _accessoryEquipment.value = characterData.accessoryEquipment
                    _equippedSecondHandShield.value = characterData.equippedSecondHandShield
                    _equippedSecondHandWeapon.value = characterData.equippedSecondHandWeapon

                    _weaponEquipment.value = characterData.weaponEquipment
                    _equippedWeapon.value = characterData.equippedWeapon

                    _miscEquipment.value = characterData.miscEquipment

                    _campaignNotes.value = characterData.campaignNotes
                    _imageBitmap.value = loadImageFromStorage(_image.value)
                }
                is Resource.Error -> Log.e("Error", "An unexpected error has occurred.")
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun loadImageFromStorage(path: String): Bitmap? {
        return try {
            val f = File(path)
            val b = BitmapFactory.decodeStream(FileInputStream(f))
            b
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    fun setCharacterImage(bitmap: Bitmap) {
        _imageBitmap.value = bitmap
        _saveAvailable.value = true
    }

    private fun saveImageToInternalStorage() {
        _imageBitmap.value?.let {
            saveImageUseCase(it, _id.value).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _image.value = result.data!!
                    }
                    is Resource.Error -> {
                        _image.value = ""
                    }
                    is Resource.Loading -> TODO()
                }
            }.launchIn(viewModelScope)
        }
    }

    fun setRace(race: String) {
        _race.value = race

        characterCreationUseCases.getRacePerkUseCase(race).onEach {
            _racePerks.value = it
        }.launchIn(viewModelScope)

        clearModifiers()
        setRaceModifiers()
    }

    fun setGender(gender: String) {
        _gender.value = gender
        _saveAvailable.value = checkIfDataChanged()
    }

    fun setMagicalGiftsEnabled(value: Boolean) {
        _magicalGiftsEnabled = value
        checkSaveAvailable()
    }

    fun setProfession(profession: String) {

        _profession.value = when (profession) {

            "Bard" -> Constants.Professions.BARD
            "Criminal" -> Constants.Professions.CRIMINAL
            "Craftsman" -> Constants.Professions.CRAFTSMAN
            "Doctor" -> Constants.Professions.DOCTOR
            "Mage" -> Constants.Professions.MAGE
            "Man At Arms" -> Constants.Professions.MAN_AT_ARMS
            "Priest" -> Constants.Professions.PRIEST
            "Druid" -> Constants.Professions.DRUID
            "Witcher" -> Constants.Professions.WITCHER
            "Merchant" -> Constants.Professions.MERCHANT
            "Noble" -> Constants.Professions.NOBLE
            "Peasant" -> Constants.Professions.PEASANT

            else -> Constants.Professions.PEASANT
        }

        characterCreationUseCases.getDefiningSkillUseCase(profession).onEach { defSkill ->
            _definingSkill.value = defSkill
        }.launchIn(viewModelScope)

        characterCreationUseCases.getDefiningSkillInfoUseCase(definingSkill.value).onEach { info ->
            _definingSkillInfo.value = info
        }.launchIn(viewModelScope)
    }

    private fun setRaceModifiers() {
        when (_race.value) {
            "Human" -> {
                _charismaModifier.value = _charismaModifier.value.plus(1)
                _seductionModifier.value = _seductionModifier.value.plus(1)
                _persuasionModifier.value = _persuasionModifier.value.plus(1)
                _deductionModifier.value = _deductionModifier.value.plus(1)
            }
            "Dwarf" -> {
                _physiqueModifier.value = _physiqueModifier.value.plus(1)
                _enc.value = _enc.value.plus(25)
                _businessModifier.value = _businessModifier.value.plus(1)
            }
            "Elf" -> {
                _fineArtsModifier.value = _fineArtsModifier.value.plus(1)
                _archeryModifier.value = _archeryModifier.value.plus(1)
            }
            "Witcher" -> {
                _awarenessModifier.value = _awarenessModifier.value.plus(1)
                _empModifier.value = _empModifier.value.minus(4)
                _refModifier.value = _refModifier.value.plus(1)
                _dexModifier.value = _dexModifier.value.plus(1)
            }
            "Halfling" -> {
                _athleticsModifier.value = _athleticsModifier.value.plus(1)
                _wildernessSurvivalModifier.value = _wildernessSurvivalModifier.value.plus(2)
                _resistMagicModifier.value = _resistMagicModifier.value.plus(5)
            }
            "Gnome" -> {
                _charismaModifier.value = _charismaModifier.value.plus(1)
                _awarenessModifier.value = _awarenessModifier.value.plus(1)
                _physiqueModifier.value = _physiqueModifier.value.minus(5)
            }
            "Vran" -> {
                _resistCoercionModifier.value = _resistCoercionModifier.value.plus(1)
            }
            "Werebbub" -> {
                _courageModifier.value = _courageModifier.value.plus(1)
            }
        }
    }

    private fun clearModifiers() {
        _intelligenceModifier.value = 0
        _refModifier.value = 0
        _dexModifier.value = 0
        _bodyModifier.value = 0
        _spdModifier.value = 0
        _empModifier.value = 0
        _craModifier.value = 0
        _willModifier.value = 0
        _luckModifier.value = 0
        _stunModifier.value = 0
        _runModifier.value = 0
        _leapModifier.value = 0
        _hpModifier.value = 0
        _staModifier.value = 0
        _enc.value = 0
        _encModifier.value = 0
        _recModifier.value = 0
        _awarenessModifier.value = 0
        _businessModifier.value = 0
        _deductionModifier.value = 0
        _educationModifier.value = 0
        _commonSpeechModifier.value = 0
        _elderSpeechModifier.value = 0
        _dwarvenModifier.value = 0
        _monsterLoreModifier.value = 0
        _socialEtiquetteModifier.value = 0
        _streetwiseModifier.value = 0
        _tacticsModifier.value = 0
        _teachingModifier.value = 0
        _wildernessSurvivalModifier.value = 0
        _brawlingModifier.value = 0
        _dodgeEscapeModifier.value = 0
        _meleeModifier.value = 0
        _ridingModifier.value = 0
        _sailingModifier.value = 0
        _smallBladesModifier.value = 0
        _staffSpearModifier.value = 0
        _swordsmanshipModifier.value = 0
        _archeryModifier.value = 0
        _athleticsModifier.value = 0
        _crossbowModifier.value = 0
        _sleightOfHandModifier.value = 0
        _stealthModifier.value = 0
        _physiqueModifier.value = 0
        _enduranceModifier.value = 0
        _charismaModifier.value = 0
        _deceitModifier.value = 0
        _fineArtsModifier.value = 0
        _gamblingModifier.value = 0
        _groomingAndStyleModifier.value = 0
        _humanPerceptionModifier.value = 0
        _leadershipModifier.value = 0
        _persuasionModifier.value = 0
        _performanceModifier.value = 0
        _seductionModifier.value = 0
        _alchemyModifier.value = 0
        _craftingModifier.value = 0
        _disguiseModifier.value = 0
        _firstAidModifier.value = 0
        _forgeryModifier.value = 0
        _pickLockModifier.value = 0
        _trapCraftingModifier.value = 0
        _courageModifier.value = 0
        _hexWeavingModifier.value = 0
        _intimidationModifier.value = 0
        _spellCastingModifier.value = 0
        _resistMagicModifier.value = 0
        _resistCoercionModifier.value = 0
        _ritualCraftingModifier.value = 0
    }

    fun getProfessionIndices(): ArrayList<Int> {
        return characterCreationUseCases.getProfessionSkillsIndicesUseCase(_profession.value)
    }

    fun getProfessionSkills(): ArrayList<String> {
        return getProfessionSkillsUseCase(_profession.value)
    }

    fun getProfessionGear(): ArrayList<String> {
        return getProfessionGearUseCase(_profession.value)
    }

    fun getProfessionSpecial(): String {
        return getProfessionSpecialUseCase(_profession.value)
    }

    fun addLifeEvent(lifeEvent: LifeEvent) {
        _lifeEvents.value.add(lifeEvent)
        _lifeEvents.value = ArrayList(_lifeEvents.value.sortedWith(compareBy { it.age }))
        _saveAvailable.value = checkIfDataChanged()
    }

    fun updateLifeEvent(updatedEvent: LifeEvent) {
        var eventToRemove: LifeEvent? = null
        for (event in _lifeEvents.value) {
            if (updatedEvent.uniqueID == event.uniqueID) {
                eventToRemove = event
            }
        }
        if (eventToRemove != null) {
            _lifeEvents.value.remove(eventToRemove)
            addLifeEvent(updatedEvent)
        }
        _saveAvailable.value = checkIfDataChanged()
    }

    fun removeLifeEvent(lifeEvent: LifeEvent) {
        _lifeEvents.value.remove(lifeEvent)
        _lifeEvents.value = ArrayList(_lifeEvents.value.sortedWith(compareBy { it.age }))
        _saveAvailable.value = checkIfDataChanged()
    }

    fun onSkillChange(skill: String, increase: Boolean): Resource<Pair<Int, Int>>? {

        when (skill) {
            "Awareness" -> {
                val pair = onSkillChangeUseCase(
                    _awareness.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _awareness.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Business" -> {
                val pair = onSkillChangeUseCase(
                    _business.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _business.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Deduction" -> {
                val pair = onSkillChangeUseCase(
                    _deduction.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _deduction.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Education" -> {
                val pair = onSkillChangeUseCase(
                    _education.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _education.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Common Speech" -> {
                val pair = onSkillChangeUseCase(
                    _commonSpeech.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value,
                    doubleCost = true
                )
                if (pair.data != null) {
                    _commonSpeech.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Elder Speech" -> {
                val pair = onSkillChangeUseCase(
                    _elderSpeech.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value,
                    doubleCost = true
                )
                if (pair.data != null) {
                    _elderSpeech.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Dwarven" -> {
                val pair = onSkillChangeUseCase(
                    _dwarven.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value,
                    doubleCost = true
                )
                if (pair.data != null) {
                    _dwarven.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Monster Lore" -> {
                val pair = onSkillChangeUseCase(
                    _monsterLore.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value,
                    doubleCost = true
                )
                if (pair.data != null) {
                    _monsterLore.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Social Etiquette" -> {
                val pair = onSkillChangeUseCase(
                    _socialEtiquette.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _socialEtiquette.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Streetwise" -> {
                val pair = onSkillChangeUseCase(
                    _streetwise.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _streetwise.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Tactics" -> {
                val pair = onSkillChangeUseCase(
                    _tactics.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value,
                    doubleCost = true
                )
                if (pair.data != null) {
                    _tactics.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Teaching" -> {
                val pair = onSkillChangeUseCase(
                    _teaching.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _teaching.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Wilderness Survival" -> {
                val pair = onSkillChangeUseCase(
                    _wildernessSurvival.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _wildernessSurvival.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Brawling" -> {
                val pair = onSkillChangeUseCase(
                    _brawling.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _brawling.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Dodge/Escape" -> {
                val pair = onSkillChangeUseCase(
                    _dodgeEscape.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _dodgeEscape.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Melee" -> {
                val pair = onSkillChangeUseCase(
                    _melee.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _melee.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Riding" -> {
                val pair = onSkillChangeUseCase(
                    _riding.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _riding.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Sailing" -> {
                val pair = onSkillChangeUseCase(
                    _sailing.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _sailing.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Small Blades" -> {
                val pair = onSkillChangeUseCase(
                    _smallBlades.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _smallBlades.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Staff/Spear" -> {
                val pair = onSkillChangeUseCase(
                    _staffSpear.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _staffSpear.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Swordsmanship" -> {
                val pair = onSkillChangeUseCase(
                    _swordsmanship.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _swordsmanship.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Archery" -> {
                val pair = onSkillChangeUseCase(
                    _archery.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _archery.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Athletics" -> {
                val pair = onSkillChangeUseCase(
                    _athletics.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _athletics.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Crossbow" -> {
                val pair = onSkillChangeUseCase(
                    _crossbow.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _crossbow.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Sleight of Hand" -> {
                val pair = onSkillChangeUseCase(
                    _sleightOfHand.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _sleightOfHand.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Stealth" -> {
                val pair = onSkillChangeUseCase(
                    _stealth.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _stealth.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Physique" -> {
                val pair = onSkillChangeUseCase(
                    _physique.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _physique.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Endurance" -> {
                val pair = onSkillChangeUseCase(
                    _endurance.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _endurance.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Charisma" -> {
                val pair = onSkillChangeUseCase(
                    _charisma.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _charisma.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Deceit" -> {
                val pair = onSkillChangeUseCase(
                    _deceit.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _deceit.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Fine Arts" -> {
                val pair = onSkillChangeUseCase(
                    _fineArts.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _fineArts.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Gambling" -> {
                val pair = onSkillChangeUseCase(
                    _gambling.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _gambling.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Grooming and Style" -> {
                val pair = onSkillChangeUseCase(
                    _groomingAndStyle.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _groomingAndStyle.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Human Perception" -> {
                val pair = onSkillChangeUseCase(
                    _humanPerception.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _humanPerception.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Leadership" -> {
                val pair = onSkillChangeUseCase(
                    _leadership.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _leadership.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Persuasion" -> {
                val pair = onSkillChangeUseCase(
                    _persuasion.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _persuasion.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Performance" -> {
                val pair = onSkillChangeUseCase(
                    _performance.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _performance.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Seduction" -> {
                val pair = onSkillChangeUseCase(
                    _seduction.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _seduction.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Alchemy" -> {
                val pair = onSkillChangeUseCase(
                    _alchemy.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value,
                    doubleCost = true
                )
                if (pair.data != null) {
                    _alchemy.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Crafting" -> {
                val pair = onSkillChangeUseCase(
                    _crafting.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value,
                    doubleCost = true
                )
                if (pair.data != null) {
                    _crafting.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Disguise" -> {
                val pair = onSkillChangeUseCase(
                    _disguise.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _disguise.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "First Aid" -> {
                val pair = onSkillChangeUseCase(
                    _firstAid.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _firstAid.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Forgery" -> {
                val pair = onSkillChangeUseCase(
                    _forgery.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _forgery.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Pick Lock" -> {
                val pair = onSkillChangeUseCase(
                    _pickLock.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _pickLock.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Trap Crafting" -> {
                val pair = onSkillChangeUseCase(
                    _trapCrafting.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value,
                    doubleCost = true
                )
                if (pair.data != null) {
                    _trapCrafting.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Courage" -> {
                val pair = onSkillChangeUseCase(
                    _courage.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _courage.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Hex Weaving" -> {
                val pair = onSkillChangeUseCase(
                    _hexWeaving.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value,
                    doubleCost = true
                )
                if (pair.data != null) {
                    _hexWeaving.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Intimidation" -> {
                val pair = onSkillChangeUseCase(
                    _intimidation.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _intimidation.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Spell Casting" -> {
                val pair = onSkillChangeUseCase(
                    _spellCasting.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value,
                    doubleCost = true
                )
                if (pair.data != null) {
                    _spellCasting.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Resist Magic" -> {
                val pair = onSkillChangeUseCase(
                    _resistMagic.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value,
                    doubleCost = true
                )
                if (pair.data != null) {
                    _resistMagic.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Resist Coercion" -> {
                val pair = onSkillChangeUseCase(
                    _resistCoercion.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _resistCoercion.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "Ritual Crafting" -> {
                val pair = onSkillChangeUseCase(
                    _ritualCrafting.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value,
                    doubleCost = true
                )
                if (pair.data != null) {
                    _ritualCrafting.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "DefiningSkill" -> {
                val pair = onSkillChangeUseCase(
                    _definingSkillValue.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _definingSkillValue.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
        }
        return null
    }

    fun onSkillModifierChange(skill: String, increase: Boolean): Int {

        when (skill) {
            "Awareness" -> {
                if (increase) _awarenessModifier.value = _awarenessModifier.value.plus(1)
                else _awarenessModifier.value = _awarenessModifier.value.minus(1)
                return _awarenessModifier.value
            }
            "Business" -> {
                if (increase) _businessModifier.value = _businessModifier.value.plus(1)
                else _businessModifier.value = _businessModifier.value.minus(1)
                return _businessModifier.value
            }
            "Deduction" -> {
                if (increase) _deductionModifier.value = _deductionModifier.value.plus(1)
                else _deductionModifier.value = _deductionModifier.value.minus(1)
                return _deductionModifier.value
            }
            "Education" -> {
                if (increase) _educationModifier.value = _educationModifier.value.plus(1)
                else _educationModifier.value = _educationModifier.value.minus(1)
                return _educationModifier.value
            }
            "Common Speech" -> {
                if (increase) _commonSpeechModifier.value = _commonSpeechModifier.value.plus(1)
                else _commonSpeechModifier.value = _commonSpeechModifier.value.minus(1)
                return _commonSpeechModifier.value
            }
            "Elder Speech" -> {
                if (increase) _elderSpeechModifier.value = _elderSpeechModifier.value.plus(1)
                else _elderSpeechModifier.value = _elderSpeechModifier.value.minus(1)
                return _elderSpeechModifier.value
            }
            "Dwarven" -> {
                if (increase) _dwarvenModifier.value = _dwarvenModifier.value.plus(1)
                else _dwarvenModifier.value = _dwarvenModifier.value.minus(1)
                return _dwarvenModifier.value
            }
            "Monster Lore" -> {
                if (increase) _monsterLoreModifier.value = _monsterLoreModifier.value.plus(1)
                else _monsterLoreModifier.value = _monsterLoreModifier.value.minus(1)
                return _monsterLoreModifier.value
            }
            "Social Etiquette" -> {
                if (increase) _socialEtiquetteModifier.value =
                    _socialEtiquetteModifier.value.plus(1)
                else _socialEtiquetteModifier.value = _socialEtiquetteModifier.value.minus(1)
                return _socialEtiquetteModifier.value
            }
            "Streetwise" -> {
                if (increase) _streetwiseModifier.value = _streetwiseModifier.value.plus(1)
                else _streetwiseModifier.value = _streetwiseModifier.value.minus(1)
                return _streetwiseModifier.value
            }
            "Tactics" -> {
                if (increase) _tacticsModifier.value = _tacticsModifier.value.plus(1)
                else _tacticsModifier.value = _tacticsModifier.value.minus(1)
                return _tacticsModifier.value
            }
            "Teaching" -> {
                if (increase) _teachingModifier.value = _teachingModifier.value.plus(1)
                else _teachingModifier.value = _teachingModifier.value.minus(1)
                return _teachingModifier.value
            }
            "Wilderness Survival" -> {
                if (increase) _wildernessSurvivalModifier.value =
                    _wildernessSurvivalModifier.value.plus(1)
                else _wildernessSurvivalModifier.value = _wildernessSurvivalModifier.value.minus(1)
                return _wildernessSurvivalModifier.value
            }
            "Brawling" -> {
                if (increase) _brawlingModifier.value = _brawlingModifier.value.plus(1)
                else _brawlingModifier.value = _brawlingModifier.value.minus(1)
                return _brawlingModifier.value
            }
            "Dodge/Escape" -> {
                if (increase) _dodgeEscapeModifier.value = _dodgeEscapeModifier.value.plus(1)
                else _dodgeEscapeModifier.value = _dodgeEscapeModifier.value.minus(1)
                return _dodgeEscapeModifier.value
            }
            "Melee" -> {
                if (increase) _meleeModifier.value = _meleeModifier.value.plus(1)
                else _meleeModifier.value = _meleeModifier.value.minus(1)
                return _meleeModifier.value
            }
            "Riding" -> {
                if (increase) _ridingModifier.value = _ridingModifier.value.plus(1)
                else _ridingModifier.value = _ridingModifier.value.minus(1)
                return _ridingModifier.value
            }
            "Sailing" -> {
                if (increase) _sailingModifier.value = _sailingModifier.value.plus(1)
                else _sailingModifier.value = _sailingModifier.value.minus(1)
                return _sailingModifier.value
            }
            "Small Blades" -> {
                if (increase) _smallBladesModifier.value = _smallBladesModifier.value.plus(1)
                else _smallBladesModifier.value = _smallBladesModifier.value.minus(1)
                return _smallBladesModifier.value
            }
            "Staff/Spear" -> {
                if (increase) _staffSpearModifier.value = _staffSpearModifier.value.plus(1)
                else _staffSpearModifier.value = _staffSpearModifier.value.minus(1)
                return _staffSpearModifier.value
            }
            "Swordsmanship" -> {
                if (increase) _swordsmanshipModifier.value = _swordsmanshipModifier.value.plus(1)
                else _swordsmanshipModifier.value = _swordsmanshipModifier.value.minus(1)
                return _swordsmanshipModifier.value
            }
            "Archery" -> {
                if (increase) _archeryModifier.value = _archeryModifier.value.plus(1)
                else _archeryModifier.value = _archeryModifier.value.minus(1)
                return _archeryModifier.value
            }
            "Athletics" -> {
                if (increase) _athleticsModifier.value = _athleticsModifier.value.plus(1)
                else _athleticsModifier.value = _athleticsModifier.value.minus(1)
                return _athleticsModifier.value
            }
            "Crossbow" -> {
                if (increase) _crossbowModifier.value = _crossbowModifier.value.plus(1)
                else _crossbowModifier.value = _crossbowModifier.value.minus(1)
                return _crossbowModifier.value
            }
            "Sleight of Hand" -> {
                if (increase) _sleightOfHandModifier.value = _sleightOfHandModifier.value.plus(1)
                else _sleightOfHandModifier.value = _sleightOfHandModifier.value.minus(1)
                return _sleightOfHandModifier.value
            }
            "Stealth" -> {
                if (increase) _stealthModifier.value = _stealthModifier.value.plus(1)
                else _stealthModifier.value = _stealthModifier.value.minus(1)
                return _stealthModifier.value
            }
            "Physique" -> {
                if (increase) _physiqueModifier.value = _physiqueModifier.value.plus(1)
                else _physiqueModifier.value = _physiqueModifier.value.minus(1)
                return _physiqueModifier.value
            }
            "Endurance" -> {
                if (increase) _enduranceModifier.value = _enduranceModifier.value.plus(1)
                else _enduranceModifier.value = _enduranceModifier.value.minus(1)
                return _enduranceModifier.value
            }
            "Charisma" -> {
                if (increase) _charismaModifier.value = _charismaModifier.value.plus(1)
                else _charismaModifier.value = _charismaModifier.value.minus(1)
                return _charismaModifier.value
            }
            "Deceit" -> {
                if (increase) _deceitModifier.value = _deceitModifier.value.plus(1)
                else _deceitModifier.value = _deceitModifier.value.minus(1)
                return _deceitModifier.value
            }
            "Fine Arts" -> {
                if (increase) _fineArtsModifier.value = _fineArtsModifier.value.plus(1)
                else _fineArtsModifier.value = _fineArtsModifier.value.minus(1)
                return _fineArtsModifier.value
            }
            "Gambling" -> {
                if (increase) _gamblingModifier.value = _gamblingModifier.value.plus(1)
                else _gamblingModifier.value = _gamblingModifier.value.minus(1)
                return _gamblingModifier.value
            }
            "Grooming and Style" -> {
                if (increase) _groomingAndStyleModifier.value =
                    _groomingAndStyleModifier.value.plus(1)
                else _groomingAndStyleModifier.value = _groomingAndStyleModifier.value.minus(1)
                return _groomingAndStyleModifier.value
            }
            "Human Perception" -> {
                if (increase) _humanPerceptionModifier.value =
                    _humanPerceptionModifier.value.plus(1)
                else _humanPerceptionModifier.value = _humanPerceptionModifier.value.minus(1)
                return _humanPerceptionModifier.value
            }
            "Leadership" -> {
                if (increase) _leadershipModifier.value = _leadershipModifier.value.plus(1)
                else _leadershipModifier.value = _leadershipModifier.value.minus(1)
                return _leadershipModifier.value
            }
            "Persuasion" -> {
                if (increase) _persuasionModifier.value = _persuasionModifier.value.plus(1)
                else _persuasionModifier.value = _persuasionModifier.value.minus(1)
                return _persuasionModifier.value
            }
            "Performance" -> {
                if (increase) _performanceModifier.value = _performanceModifier.value.plus(1)
                else _performanceModifier.value = _performanceModifier.value.minus(1)
                return _performanceModifier.value
            }
            "Seduction" -> {
                if (increase) _seductionModifier.value = _seductionModifier.value.plus(1)
                else _seductionModifier.value = _seductionModifier.value.minus(1)
                return _seductionModifier.value
            }
            "Alchemy" -> {
                if (increase) _alchemyModifier.value = _alchemyModifier.value.plus(1)
                else _alchemyModifier.value = _alchemyModifier.value.minus(1)
                return _alchemyModifier.value
            }
            "Crafting" -> {
                if (increase) _craftingModifier.value = _craftingModifier.value.plus(1)
                else _craftingModifier.value = _craftingModifier.value.minus(1)
                return _craftingModifier.value
            }
            "Disguise" -> {
                if (increase) _disguiseModifier.value = _disguiseModifier.value.plus(1)
                else _disguiseModifier.value = _disguiseModifier.value.minus(1)
                return _disguiseModifier.value
            }
            "First Aid" -> {
                if (increase) _firstAidModifier.value = _firstAidModifier.value.plus(1)
                else _firstAidModifier.value = _firstAidModifier.value.minus(1)
                return _firstAidModifier.value
            }
            "Forgery" -> {
                if (increase) _forgeryModifier.value = _forgeryModifier.value.plus(1)
                else _forgeryModifier.value = _forgeryModifier.value.minus(1)
                return _forgeryModifier.value
            }
            "Pick Lock" -> {
                if (increase) _pickLockModifier.value = _pickLockModifier.value.plus(1)
                else _pickLockModifier.value = _pickLockModifier.value.minus(1)
                return _pickLockModifier.value
            }
            "Trap Crafting" -> {
                if (increase) _trapCraftingModifier.value = _trapCraftingModifier.value.plus(1)
                else _trapCraftingModifier.value = _trapCraftingModifier.value.minus(1)
                return _trapCraftingModifier.value
            }
            "Courage" -> {
                if (increase) _courageModifier.value = _courageModifier.value.plus(1)
                else _courageModifier.value = _courageModifier.value.minus(1)
                return _courageModifier.value
            }
            "Hex Weaving" -> {
                if (increase) _hexWeavingModifier.value = _hexWeavingModifier.value.plus(1)
                else _hexWeavingModifier.value = _hexWeavingModifier.value.minus(1)
                return _hexWeavingModifier.value
            }
            "Intimidation" -> {
                if (increase) _intimidationModifier.value = _intimidationModifier.value.plus(1)
                else _intimidationModifier.value = _intimidationModifier.value.minus(1)
                return _intimidationModifier.value
            }
            "Spell Casting" -> {
                if (increase) _spellCastingModifier.value = _spellCastingModifier.value.plus(1)
                else _spellCastingModifier.value = _spellCastingModifier.value.minus(1)
                return _spellCastingModifier.value
            }
            "Resist Magic" -> {
                if (increase) _resistMagicModifier.value = _resistMagicModifier.value.plus(1)
                else _resistMagicModifier.value = _resistMagicModifier.value.minus(1)
                return _resistMagicModifier.value
            }
            "Resist Coercion" -> {
                if (increase) _resistCoercionModifier.value = _resistCoercionModifier.value.plus(1)
                else _resistCoercionModifier.value = _resistCoercionModifier.value.minus(1)
                return _resistCoercionModifier.value
            }
            "Ritual Crafting" -> {
                if (increase) _ritualCraftingModifier.value = _ritualCraftingModifier.value.plus(1)
                else _ritualCraftingModifier.value = _ritualCraftingModifier.value.minus(1)
                return _ritualCraftingModifier.value
            }
        }
        return 0
    }

    fun onStatChange(stat: String, increase: Boolean): Resource<Pair<Int, Int>>? {

        when (stat) {

            "INT" -> {
                val pair = onStatChangeUseCase(
                    _intelligence.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _intelligence.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "REF" -> {
                val pair = onStatChangeUseCase(
                    _ref.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _ref.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "DEX" -> {
                val pair = onStatChangeUseCase(
                    _dex.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _dex.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "BODY" -> {
                val pair = onStatChangeUseCase(
                    _body.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _body.value = pair.data.second
                    _ip.value = pair.data.first
                    onHpChange(increase = increase)
                    onStaminaChange(increase = increase)
                    _rec.value = (_body.value + _will.value) / 2
                    _stun.value =
                        if (((_body.value + _will.value) / 2) < 10) ((_body.value + _will.value) / 2)
                        else 10
                    onEncumbranceChange(increase = increase)
                    _encWithModifier.value = _encModifier.value.plus(_enc.value)
                    when (_body.value) {
                        1, 2 -> {
                            _punch.value = "1d6 - 4"
                            _kick.value = "1d6"
                        }
                        3, 4 -> {
                            _punch.value = "1d6 - 2"
                            _kick.value = "1d6 + 2"
                        }
                        5, 6 -> {
                            _punch.value = "1d6"
                            _kick.value = "1d6 + 4"
                        }
                        7, 8 -> {
                            _punch.value = "1d6 + 2"
                            _kick.value = "1d6 + 6"
                        }
                        9, 10 -> {
                            _punch.value = "1d6 + 4"
                            _kick.value = "1d6 + 8"
                        }
                        11, 12 -> {
                            _punch.value = "1d6 + 6"
                            _kick.value = "1d6 + 10"
                        }
                        13 -> {
                            _punch.value = "1d6 + 8"
                            _kick.value = "1d6 + 12"
                        }
                    }
                } else return pair
            }
            "SPD" -> {
                val pair = onStatChangeUseCase(
                    _spd.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _spd.value = pair.data.second
                    _ip.value = pair.data.first
                    _run.value = _spd.value * 3
                    _leap.value = (_spd.value * 3) / 5
                } else return pair
            }
            "EMP" -> {
                val pair = onStatChangeUseCase(
                    _emp.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _emp.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "CRA" -> {
                val pair = onStatChangeUseCase(
                    _cra.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _cra.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "WILL" -> {
                val pair = onStatChangeUseCase(
                    _will.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _will.value = pair.data.second
                    _ip.value = pair.data.first
                    onHpChange(increase = increase)
                    onStaminaChange(increase = increase)
                    _rec.value = (_body.value + _will.value) / 2
                    _stun.value =
                        if (((_body.value + _will.value) / 2) < 10) ((_body.value + _will.value) / 2)
                        else 10
                } else return pair
            }
            "LUCK" -> {
                val pair = onStatChangeUseCase(
                    _luck.value,
                    _ip.value,
                    increase,
                    _inCharacterCreation.value
                )
                if (pair.data != null) {
                    _luck.value = pair.data.second
                    _ip.value = pair.data.first
                } else return pair
            }
            "STUN" -> {
                _stun.value = if (increase) _stun.value.plus(1) else _stun.value.minus(1)
            }
            "RUN" -> {
                _run.value = if (increase) _run.value.plus(1) else _run.value.minus(1)

            }
            "LEAP" -> {
                _leap.value = if (increase) _leap.value.plus(1) else _leap.value.minus(1)

            }
            "MaxHP" -> {
                _maxHP.value = if (increase) _maxHP.value.plus(1) else _maxHP.value.minus(1)
                _maxHPWithModifier.value = _maxHP.value.plus(_hpModifier.value)

            }
            "MaxSTA" -> {
                _maxSta.value = if (increase) _maxSta.value.plus(1) else _maxSta.value.minus(1)
                _maxSTAWithModifier.value = _maxSta.value.plus(_staModifier.value)
            }
            "ENC" -> {
                _enc.value = if (increase) _enc.value.plus(1) else _enc.value.minus(1)
                _encWithModifier.value = _encModifier.value.plus(_enc.value)
            }
            "REC" -> {
                _rec.value = if (increase) _rec.value.plus(1) else _rec.value.minus(1)

            }
        }
        return null
    }

    fun onStatModifierChange(stat: String, increase: Boolean) {

        when (stat) {

            "INT" -> {
                if (increase) _intelligenceModifier.value = _intelligenceModifier.value.plus(1)
                else _intelligenceModifier.value = _intelligenceModifier.value.minus(1)
            }
            "REF" -> {
                if (increase) _refModifier.value = _refModifier.value.plus(1)
                else _refModifier.value = _refModifier.value.minus(1)
            }
            "DEX" -> {
                if (increase) _dexModifier.value = _dexModifier.value.plus(1)
                else _dexModifier.value = _dexModifier.value.minus(1)
            }
            "BODY" -> {
                if (increase) _bodyModifier.value = _bodyModifier.value.plus(1)
                else _bodyModifier.value = _bodyModifier.value.minus(1)
                onHpChange(increase = increase)
                onStaminaChange(increase = increase)
            }
            "SPD" -> {
                if (increase) _spdModifier.value = _spdModifier.value.plus(1)
                else _spdModifier.value = _spdModifier.value.minus(1)
            }
            "EMP" -> {
                if (increase) _empModifier.value = _empModifier.value.plus(1)
                else _empModifier.value = _empModifier.value.minus(1)
            }
            "CRA" -> {
                if (increase) _craModifier.value = _craModifier.value.plus(1)
                else _craModifier.value = _craModifier.value.minus(1)
            }
            "WILL" -> {
                if (increase) _willModifier.value = _willModifier.value.plus(1)
                else _willModifier.value = _willModifier.value.minus(1)
                onHpChange(increase = increase)
                onStaminaChange(increase = increase)
            }
            "LUCK" -> {
                if (increase) _luckModifier.value = _luckModifier.value.plus(1)
                else _luckModifier.value = _luckModifier.value.minus(1)
            }
            "STUN" -> {
                if (increase) _stunModifier.value = _stunModifier.value.plus(1)
                else _stunModifier.value = _stunModifier.value.minus(1)
            }
            "RUN" -> {
                if (increase) _runModifier.value = _runModifier.value.plus(1)
                else _runModifier.value = _runModifier.value.minus(1)

            }
            "LEAP" -> {
                if (increase) _leapModifier.value = _leapModifier.value.plus(1)
                else _leapModifier.value = _leapModifier.value.minus(1)
            }
            "MaxHP" -> {
                if (increase) _hpModifier.value = _hpModifier.value.plus(1)
                else _hpModifier.value = _hpModifier.value.minus(1)
                _maxHPWithModifier.value = _maxHP.value.plus(_hpModifier.value)
            }
            "MaxSTA" -> {
                if (increase) _staModifier.value = _staModifier.value.plus(1)
                else _staModifier.value = _staModifier.value.minus(1)
                _maxSTAWithModifier.value = _maxSta.value.plus(_staModifier.value)
            }
            "ENC" -> {
                if (increase) _encModifier.value = _encModifier.value.plus(1)
                else _encModifier.value = _encModifier.value.minus(1)
                _encWithModifier.value = _encModifier.value.plus(_enc.value)
            }
            "REC" -> {
                if (increase) _recModifier.value = _recModifier.value.plus(1)
                else _recModifier.value = _recModifier.value.minus(1)

            }
        }
    }

    fun onRest(longRest: Boolean) {
        if (longRest) {
            if (_hp.value < _maxHPWithModifier.value) {
                if (_hp.value + _rec.value > _maxHPWithModifier.value) _hp.value =
                    _maxHPWithModifier.value
                else _hp.value += _rec.value
            }
            if (_sta.value < _maxSTAWithModifier.value) _sta.value = _maxSTAWithModifier.value
        } else {
            if (_sta.value < _maxSTAWithModifier.value) {
                if (_sta.value + _rec.value > _maxSTAWithModifier.value) _sta.value =
                    _maxSTAWithModifier.value
                else _sta.value = _sta.value.plus(_rec.value)
            }
        }
        checkSaveAvailable()
    }

    fun onSaveEdit(name: String, age: String, gender: String, enableMagicalGifts: Boolean) {
        this.name.value = name
        this.age.value = age
        _gender.value = gender
        _magicalGiftsEnabled = enableMagicalGifts
        checkSaveAvailable()
    }

    fun onProfessionSkillChange(skill: Int, increase: Boolean) {

        when (skill) {
            1 -> {
                var initialVigor = _vigor.value - _professionSkillA1.value
                val pair = onProfessionSkillChangeUseCase(
                    _professionSkillA1.value,
                    _ip.value,
                    increase,
                    _professionSkillA2.value
                ).data
                if (pair != null) {
                    _professionSkillA1.value = pair.second
                    _ip.value = pair.first

                    if (_profession.value == Constants.Professions.DRUID) {
                        if (_professionSkillA1.value == 9 && !increase) initialVigor -= 4
                        _vigor.value = if (_professionSkillA1.value <= 9) initialVigor + _professionSkillA1.value else initialVigor + _professionSkillA1.value + 4
                    }
                }
            }
            2 -> {
                val initialVigor = _vigor.value - _professionSkillA2.value / 2
                val pair = onProfessionSkillChangeUseCase(
                    _professionSkillA2.value,
                    _ip.value,
                    increase,
                    _professionSkillA3.value
                ).data
                if (pair != null) {
                    _professionSkillA2.value = pair.second
                    _ip.value = pair.first

                    if (_profession.value == Constants.Professions.WITCHER) {
                        _vigor.value = initialVigor + _professionSkillA2.value / 2
                    }
                }
            }
            3 -> {
                val pair = onProfessionSkillChangeUseCase(
                    _professionSkillA3.value,
                    _ip.value,
                    increase,
                    0
                ).data
                if (pair != null) {
                    _professionSkillA3.value = pair.second
                    _ip.value = pair.first
                }
            }
            4 -> {
                var initialVigor = _vigor.value - _professionSkillB1.value
                val pair = onProfessionSkillChangeUseCase(
                    _professionSkillB1.value,
                    _ip.value,
                    increase,
                    _professionSkillB2.value
                ).data
                if (pair != null) {
                    _professionSkillB1.value = pair.second
                    _ip.value = pair.first

                    if (_profession.value == Constants.Professions.PRIEST) {
                        if (_professionSkillB1.value == 9 && !increase) initialVigor -= 4
                        _vigor.value = if (_professionSkillB1.value <= 9) initialVigor + _professionSkillB1.value else initialVigor + _professionSkillB1.value + 4
                    }
                }
            }
            5 -> {
                val pair = onProfessionSkillChangeUseCase(
                    _professionSkillB2.value,
                    _ip.value,
                    increase,
                    _professionSkillB3.value
                ).data
                if (pair != null) {
                    _professionSkillB2.value = pair.second
                    _ip.value = pair.first
                }
            }
            6 -> {
                val pair = onProfessionSkillChangeUseCase(
                    _professionSkillB3.value,
                    _ip.value,
                    increase,
                    0
                ).data
                if (pair != null) {
                    _professionSkillB3.value = pair.second
                    _ip.value = pair.first
                }
            }
            7 -> {
                val initialVigor = _vigor.value - _professionSkillC1.value * 2
                val pair = onProfessionSkillChangeUseCase(
                    _professionSkillC1.value,
                    _ip.value,
                    increase,
                    _professionSkillC2.value
                ).data
                if (pair != null) {
                    _professionSkillC1.value = pair.second
                    _ip.value = pair.first

                    if (_profession.value == Constants.Professions.MAGE) {
                        _vigor.value = initialVigor + _professionSkillC1.value * 2
                    }
                }
            }
            8 -> {
                val pair = onProfessionSkillChangeUseCase(
                    _professionSkillC2.value,
                    _ip.value,
                    increase,
                    _professionSkillC3.value
                ).data
                if (pair != null) {
                    _professionSkillC2.value = pair.second
                    _ip.value = pair.first
                }
            }
            9 -> {
                val pair = onProfessionSkillChangeUseCase(
                    _professionSkillC3.value,
                    _ip.value,
                    increase,
                    0
                ).data
                if (pair != null) {
                    _professionSkillC3.value = pair.second
                    _ip.value = pair.first
                }
            }
        }
        checkSaveAvailable()
    }

    fun addMagicItem(item: MagicItem) {
        when (item.type) {
            MagicType.NOVICE_SPELL -> if (item !in _noviceSpellList.value) _noviceSpellList.value.add(
                item
            )
            MagicType.JOURNEYMAN_SPELL -> if (item !in _journeymanSpellList.value) _journeymanSpellList.value.add(
                item
            )
            MagicType.MASTER_SPELL -> if (item !in _masterSpellList.value) _masterSpellList.value.add(
                item
            )
            MagicType.NOVICE_RITUAL -> if (item !in _noviceRitualList.value) _noviceRitualList.value.add(
                item
            )
            MagicType.JOURNEYMAN_RITUAL -> if (item !in _journeymanRitualList.value) _journeymanRitualList.value.add(
                item
            )
            MagicType.MASTER_RITUAL -> if (item !in _masterRitualList.value) _masterRitualList.value.add(
                item
            )
            MagicType.HEX -> if (item !in _hexesList.value) _hexesList.value.add(item)
            MagicType.BASIC_SIGN -> if (item !in _basicSigns.value) _basicSigns.value.add(item)
            MagicType.ALTERNATE_SIGN -> if (item !in _alternateSigns.value) _alternateSigns.value.add(
                item
            )
            MagicType.NOVICE_DRUID_INVOCATION -> if (item !in _noviceDruidInvocations.value) _noviceDruidInvocations.value.add(
                item
            )
            MagicType.JOURNEYMAN_DRUID_INVOCATION -> if (item !in _journeymanDruidInvocations.value) _journeymanDruidInvocations.value.add(
                item
            )
            MagicType.MASTER_DRUID_INVOCATION -> if (item !in _masterDruidInvocations.value) _masterDruidInvocations.value.add(
                item
            )
            MagicType.HIEROPHANT_FLAMINIKA_DRUID_INVOCATION -> _hierophantFlaminikaDruidInvocations.value.add(
                item
            )
            MagicType.NOVICE_PREACHER_INVOCATION -> if (item !in _novicePreacherInvocations.value) _novicePreacherInvocations.value.add(
                item
            )
            MagicType.JOURNEYMAN_PREACHER_INVOCATION -> if (item !in _journeymanPreacherInvocations.value) _journeymanPreacherInvocations.value.add(
                item
            )
            MagicType.MASTER_PREACHER_INVOCATION -> if (item !in _masterPreacherInvocations.value) _masterPreacherInvocations.value.add(
                item
            )
            MagicType.ARCH_PRIEST_INVOCATION -> if (item !in _archPriestInvocations.value) _archPriestInvocations.value.add(
                item
            )
            MagicType.MINOR_GIFT -> if (item !in _minorGifts.value) _minorGifts.value.add(
                item
            )
            MagicType.MAJOR_GIFT -> if (item !in _majorGifts.value) _majorGifts.value.add(
                item
            )
        }
        checkSaveAvailable()
    }

    fun removeMagicItem(item: MagicItem) {
        when (item.type) {
            MagicType.NOVICE_SPELL -> _noviceSpellList.value.remove(
                item
            )
            MagicType.JOURNEYMAN_SPELL -> _journeymanSpellList.value.remove(
                item
            )
            MagicType.MASTER_SPELL -> _masterSpellList.value.remove(
                item
            )
            MagicType.NOVICE_RITUAL -> _noviceRitualList.value.remove(
                item
            )
            MagicType.JOURNEYMAN_RITUAL -> _journeymanRitualList.value.remove(
                item
            )
            MagicType.MASTER_RITUAL -> _masterRitualList.value.remove(
                item
            )
            MagicType.HEX -> _hexesList.value.remove(item)
            MagicType.BASIC_SIGN -> _basicSigns.value.remove(item)
            MagicType.ALTERNATE_SIGN -> _alternateSigns.value.remove(
                item
            )
            MagicType.NOVICE_DRUID_INVOCATION -> _noviceDruidInvocations.value.remove(
                item
            )
            MagicType.JOURNEYMAN_DRUID_INVOCATION -> _journeymanDruidInvocations.value.remove(
                item
            )
            MagicType.MASTER_DRUID_INVOCATION -> _masterDruidInvocations.value.remove(
                item
            )
            MagicType.HIEROPHANT_FLAMINIKA_DRUID_INVOCATION -> _hierophantFlaminikaDruidInvocations.value.remove(
                item
            )
            MagicType.NOVICE_PREACHER_INVOCATION -> _novicePreacherInvocations.value.remove(
                item
            )
            MagicType.JOURNEYMAN_PREACHER_INVOCATION -> _journeymanPreacherInvocations.value.remove(
                item
            )
            MagicType.MASTER_PREACHER_INVOCATION -> _masterPreacherInvocations.value.remove(
                item
            )
            MagicType.ARCH_PRIEST_INVOCATION -> _archPriestInvocations.value.remove(
                item
            )
            MagicType.MINOR_GIFT -> _minorGifts.value.remove(
                item
            )
            MagicType.MAJOR_GIFT -> _majorGifts.value.remove(
                item
            )
        }
        checkSaveAvailable()
    }

    fun getMagicList(source: Int): ArrayList<MagicItem> {
        //getCustomMagic()
        return getMagicListUseCase(source)
    }

    fun onCastMagic(item: MagicItem, ignoreHpLoss: Boolean = false): Resource<Int> {

        when (val result = castMagicUseCase(item, _focus.value, _vigor.value)) {

            is Resource.Error -> {

                return if (ignoreHpLoss) {
                    _hp.value -= result.data!!
                    _sta.value -= item.staminaCost!!
                    result
                } else {
                    result
                }
            }
            is Resource.Loading -> {
                TODO()
            }
            is Resource.Success -> {
                _sta.value -= result.data!!
            }
        }
        return Resource.Success(1)
    }

    fun onHpChange(value: Int? = null, increase: Boolean) {
        if (value == null) {
            val bodyPlusModifier = _body.value + _bodyModifier.value
            val willPlusModifier = _will.value + _willModifier.value

            if (increase) {
                val hpBonus = ((bodyPlusModifier + willPlusModifier) / 2) * 5
                _maxHP.value =
                    (_maxHP.value - ((((bodyPlusModifier - 1) + willPlusModifier) / 2) * 5)) + hpBonus
            } else {
                val hpBonus = ((bodyPlusModifier + willPlusModifier) / 2) * 5
                _maxHP.value =
                    (_maxHP.value - ((((bodyPlusModifier + 1) + willPlusModifier) / 2) * 5)) + hpBonus
            }
            _maxHPWithModifier.value = _maxHP.value + _hpModifier.value
        } else {
            if (increase) _hp.value += value
            else _hp.value -= value
        }
        checkSaveAvailable()
    }

    fun onStaminaChange(value: Int? = null, increase: Boolean) {
        if (value != null) {
            if (value < 0) {
                if (value.absoluteValue < _sta.value) {
                    _sta.value = _sta.value.plus(value)
                } else _sta.value = 0
            } else _sta.value = _sta.value.plus(value)
        } else {
            val bodyPlusModifier = _body.value + _bodyModifier.value
            val willPlusModifier = _will.value + _willModifier.value

            if (increase) {
                val staBonus = ((bodyPlusModifier + willPlusModifier) / 2) * 5
                _maxSta.value =
                    (_maxSta.value - ((((bodyPlusModifier - 1) + willPlusModifier) / 2) * 5)) + staBonus
            } else {
                val staBonus = ((bodyPlusModifier + willPlusModifier) / 2) * 5
                _maxSta.value =
                    (_maxSta.value - ((((bodyPlusModifier + 1) + willPlusModifier) / 2) * 5)) + staBonus
            }
            _maxSTAWithModifier.value = _maxSta.value + _staModifier.value
        }
        checkSaveAvailable()
    }

    fun onFocusChange(value: Int) {
        if (value < 0) {
            if (value.absoluteValue < _focus.value) {
                _focus.value = _focus.value.plus(value)
            } else _focus.value = 0
        } else _focus.value = _focus.value.plus(value)
        checkSaveAvailable()
    }

    fun onVigorChange(value: Int) {
        if (value < 0) {
            if (value.absoluteValue < _vigor.value) {
                _vigor.value = _vigor.value.plus(value)
            } else _vigor.value = 0
        } else _vigor.value = _vigor.value.plus(value)
        checkSaveAvailable()
    }

    private fun onEncumbranceChange(increase: Boolean) {
        if (increase) {
            val encBonus = _body.value * 10
            _enc.value = _enc.value - (((_body.value - 1) * 10)) + encBonus
        } else {
            val encBonus = _body.value * 10
            _enc.value = _enc.value - (((_body.value + 1) * 10)) + encBonus
        }
        checkSaveAvailable()
    }

    fun onCrownsChange(value: Int) {
        if (value < 0) {
            if (value.absoluteValue < _crowns.value) {
                _crowns.value = _crowns.value.plus(value)
            } else _crowns.value = 0
        } else _crowns.value = _crowns.value.plus(value)
        checkSaveAvailable()
    }

    fun onIpChange(value: Int) {
        if (value < 0) {
            if (value.absoluteValue < _ip.value) {
                _ip.value = _ip.value.plus(value)
            } else _ip.value = 0
        } else _ip.value = _ip.value.plus(value)
        checkSaveAvailable()
    }

    fun getEquipmentList(source: Int): ArrayList<EquipmentItem> {
        return getEquipmentListUseCase(source)
    }

    fun addEquipmentItem(item: EquipmentItem) {
        when (item.equipmentType) {
            EquipmentTypes.LIGHT_HEAD, EquipmentTypes.MEDIUM_HEAD, EquipmentTypes.HEAVY_HEAD -> _headEquipment.value.add(
                item
            )
            EquipmentTypes.LIGHT_CHEST, EquipmentTypes.MEDIUM_CHEST, EquipmentTypes.HEAVY_CHEST -> _chestEquipment.value.add(
                item
            )
            EquipmentTypes.LIGHT_LEGS, EquipmentTypes.MEDIUM_LEGS, EquipmentTypes.HEAVY_LEGS -> _legEquipment.value.add(
                item
            )
            EquipmentTypes.LIGHT_SHIELD, EquipmentTypes.MEDIUM_SHIELD, EquipmentTypes.HEAVY_SHIELD -> _accessoryEquipment.value.add(
                item
            )
            EquipmentTypes.MISC_CUSTOM -> _miscEquipment.value.add(item)
            else -> {}
        }
        calculateCurrentEncumbrance()
        checkSaveAvailable()
    }

    private fun calculateCurrentEncumbrance() {
        var currentEncumbrance = 0F

        for (item in _headEquipment.value) {
            currentEncumbrance += item.weight
        }
        for (item in _chestEquipment.value) {
            currentEncumbrance += item.weight
        }
        for (item in _legEquipment.value) {
            currentEncumbrance += item.weight
        }
        for (item in _weaponEquipment.value) {
            currentEncumbrance += item.weight
        }
        for (item in _miscEquipment.value) {
            currentEncumbrance += item.weight
        }
        currentEncumbrance += _equippedHead.value?.weight ?: 0F
        currentEncumbrance += _equippedChest.value?.weight ?: 0F
        currentEncumbrance += _equippedLegs.value?.weight ?: 0F
        currentEncumbrance += _equippedWeapon.value?.weight ?: 0F
        currentEncumbrance += _equippedSecondHandWeapon.value?.weight ?: 0F
        currentEncumbrance += _equippedSecondHandShield.value?.weight ?: 0F

        _currentEnc.value = currentEncumbrance
    }

    fun addArmorSet(armorSet: ArmorSet) {
        val armorArray = getArmorFromArmorSetUseCase(armorSet)
        for (armor in armorArray) {
            addEquipmentItem(armor)
        }
        checkSaveAvailable()
    }

    fun buyArmorSet(armorSet: ArmorSet): Boolean {
        return if (armorSet.cost > _crowns.value) {
            false
        } else {
            addArmorSet(armorSet)
            checkSaveAvailable()
            true
        }
    }

    fun removeEquipment(item: EquipmentItem) {
        when (item.equipmentType) {
            EquipmentTypes.LIGHT_HEAD, EquipmentTypes.MEDIUM_HEAD, EquipmentTypes.HEAVY_HEAD -> _headEquipment.value.remove(
                item
            )
            EquipmentTypes.LIGHT_CHEST, EquipmentTypes.MEDIUM_CHEST, EquipmentTypes.HEAVY_CHEST -> _chestEquipment.value.remove(
                item
            )
            EquipmentTypes.LIGHT_LEGS, EquipmentTypes.MEDIUM_LEGS, EquipmentTypes.HEAVY_LEGS -> _legEquipment.value.remove(
                item
            )
            EquipmentTypes.LIGHT_SHIELD, EquipmentTypes.MEDIUM_SHIELD, EquipmentTypes.HEAVY_SHIELD -> _accessoryEquipment.value.remove(
                item
            )
            EquipmentTypes.MISC_CUSTOM -> _miscEquipment.value.remove(item)
            else -> {}
        }
        calculateCurrentEncumbrance()
        checkSaveAvailable()
    }

    fun equipItem(item: EquipmentItem) {
        when (item.equipmentType) {

            EquipmentTypes.LIGHT_HEAD, EquipmentTypes.MEDIUM_HEAD, EquipmentTypes.HEAVY_HEAD -> {

                //Check if theres an item already equipped in this slot
                if (_equippedHead.value != null) {

                    _headEquipment.value.remove(item)
                    _headEquipment.value.add(_equippedHead.value!!)
                    _equippedHead.value = item

                } else {
                    _headEquipment.value.remove(item)
                    _equippedHead.value = item
                }
            }

            EquipmentTypes.LIGHT_CHEST, EquipmentTypes.MEDIUM_CHEST, EquipmentTypes.HEAVY_CHEST -> {

                //Check if theres an item already equipped in this slot
                if (_equippedChest.value != null) {

                    _chestEquipment.value.remove(item)
                    _chestEquipment.value.add(_equippedChest.value!!)
                    _equippedChest.value = item

                } else {
                    _chestEquipment.value.remove(item)
                    _equippedChest.value = item
                }
            }

            EquipmentTypes.LIGHT_LEGS, EquipmentTypes.MEDIUM_LEGS, EquipmentTypes.HEAVY_LEGS -> {

                //Check if theres an item already equipped in this slot
                if (_equippedLegs.value != null) {

                    _legEquipment.value.remove(item)
                    _legEquipment.value.add(_equippedLegs.value!!)
                    _equippedLegs.value = item

                } else {
                    _legEquipment.value.remove(item)
                    _equippedLegs.value = item
                }
            }

            EquipmentTypes.LIGHT_SHIELD, EquipmentTypes.MEDIUM_SHIELD, EquipmentTypes.HEAVY_SHIELD -> {

                //Check if theres an item already equipped in this slot
                when {
                    _equippedSecondHandShield.value != null -> {

                        _accessoryEquipment.value.remove(item)
                        _accessoryEquipment.value.add(_equippedSecondHandShield.value!!)
                        _equippedSecondHandShield.value = item
                    }
                    _equippedSecondHandWeapon.value != null -> {
                        _accessoryEquipment.value.remove(item)
                        _weaponEquipment.value.add(_equippedSecondHandWeapon.value!!)
                        _focus.value -= _equippedSecondHandWeapon.value!!.focus
                        _equippedSecondHandWeapon.value = null
                        _equippedSecondHandShield.value = item
                    }
                    else -> {
                        _accessoryEquipment.value.remove(item)
                        _equippedSecondHandShield.value = item
                    }
                }
            }
            else -> {}
        }
        checkSaveAvailable()
    }

    fun unEquipItem(item: EquipmentItem) {
        when (item.equipmentType) {

            EquipmentTypes.LIGHT_HEAD, EquipmentTypes.MEDIUM_HEAD, EquipmentTypes.HEAVY_HEAD -> {
                _headEquipment.value.add(item)
                _equippedHead.value = null
            }

            EquipmentTypes.LIGHT_CHEST, EquipmentTypes.MEDIUM_CHEST, EquipmentTypes.HEAVY_CHEST -> {
                _chestEquipment.value.add(item)
                _equippedChest.value = null
            }

            EquipmentTypes.LIGHT_LEGS, EquipmentTypes.MEDIUM_LEGS, EquipmentTypes.HEAVY_LEGS -> {
                _legEquipment.value.add(item)
                _equippedLegs.value = null
            }

            EquipmentTypes.LIGHT_SHIELD, EquipmentTypes.MEDIUM_SHIELD, EquipmentTypes.HEAVY_SHIELD -> {
                _accessoryEquipment.value.add(item)
                _equippedSecondHandShield.value = null
            }
            else -> {}
        }
        checkSaveAvailable()
    }

    fun onItemSPChange(item: EquipmentItem, increase: Boolean, armorType: EquipmentTypes?): Int {

        when (armorType) {
            EquipmentTypes.LARM -> {
                item.currentLArmSP = if (increase) item.currentLArmSP.plus(1)
                else item.currentLArmSP.minus(1)
                return item.currentLArmSP
            }

            EquipmentTypes.RARM -> {
                item.currentRArmSP = if (increase) item.currentRArmSP.plus(1)
                else item.currentRArmSP.minus(1)
                return item.currentRArmSP
            }

            EquipmentTypes.LLEG -> {
                item.currentLLegSP = if (increase) item.currentLLegSP.plus(1)
                else item.currentLLegSP.minus(1)
                return item.currentLLegSP
            }

            EquipmentTypes.RLEG -> {
                item.currentRLegSP = if (increase) item.currentRLegSP.plus(1)
                else item.currentRLegSP.minus(1)
                return item.currentRLegSP
            }

            EquipmentTypes.MISC_CUSTOM -> {
                item.quantity = if (increase) item.quantity.plus(1)
                else item.quantity.minus(1)
                return item.quantity
            }

            else -> {
                item.currentStoppingPower = if (increase) item.currentStoppingPower.plus(1)
                else item.currentStoppingPower.minus(1)
                return item.currentStoppingPower
            }
        }

    }

    fun getWeaponList(source: Int): ArrayList<WeaponItem> {
        return getWeaponListUseCase(source)
    }

    fun addWeaponItem(item: WeaponItem) {
        _weaponEquipment.value.add(item)
        calculateCurrentEncumbrance()
        checkSaveAvailable()
    }

    fun removeWeapon(item: WeaponItem) {
        _weaponEquipment.value.remove(item)
        calculateCurrentEncumbrance()
        checkSaveAvailable()
    }

    fun equipWeapon(item: WeaponItem) {
        if (item.type == WeaponTypes.AMULET) {
            if (_equippedSecondHandWeapon.value != null) {
                unEquipWeapon(_equippedSecondHandWeapon.value!!)
            }
            if (_equippedSecondHandShield.value != null) {
                _accessoryEquipment.value.add(_equippedSecondHandShield.value!!)
                _equippedSecondHandShield.value = null
            }
            _equippedSecondHandWeapon.value = item
            _weaponEquipment.value.remove(item)
            _focus.value += item.focus
            return
        }
        if (_equippedWeapon.value == null) {
            if (_equippedSecondHandWeapon.value != null) {
                if (item.hands == 1) {
                    _equippedWeapon.value = item
                    _weaponEquipment.value.remove(item)
                } else {
                    unEquipWeapon(_equippedSecondHandWeapon.value!!)
                    _equippedWeapon.value = item
                    _weaponEquipment.value.remove(item)
                }
            } else {
                _equippedWeapon.value = item
                _weaponEquipment.value.remove(item)
            }

        } else {

            if (_equippedWeapon.value!!.hands == 1 && item.hands == 1) {
                if (_equippedSecondHandShield.value == null && _equippedSecondHandWeapon.value == null) {
                    _equippedSecondHandWeapon.value = item
                    _weaponEquipment.value.remove(item)
                } else if (_equippedSecondHandWeapon.value != null) {
                    _focus.value -= item.focus
                    _weaponEquipment.value.add(_equippedSecondHandWeapon.value!!)
                    _equippedSecondHandWeapon.value = item
                } else if (_equippedSecondHandShield.value != null) {
                    _weaponEquipment.value.remove(item)
                    _accessoryEquipment.value.add(_equippedSecondHandShield.value!!)
                    _equippedSecondHandShield.value = null
                    _equippedSecondHandWeapon.value = item
                }
            } else {
                unEquipWeapon(_equippedWeapon.value!!)
                _equippedWeapon.value = item
                _weaponEquipment.value.remove(item)
            }
        }
        _focus.value += item.focus
        checkSaveAvailable()
    }

    fun unEquipWeapon(item: WeaponItem) {
        if (_equippedWeapon.value != null) {
            if (item.uniqueID == _equippedWeapon.value!!.uniqueID) {
                _focus.value -= item.focus
                _weaponEquipment.value.add(_equippedWeapon.value!!)
                _equippedWeapon.value = null
            }
        }
        if (_equippedSecondHandWeapon.value != null) {
            if (item.uniqueID == _equippedSecondHandWeapon.value!!.uniqueID) {
                _focus.value -= item.focus
                _weaponEquipment.value.add(_equippedSecondHandWeapon.value!!)
                _equippedSecondHandWeapon.value = null
            }
        }
        checkSaveAvailable()
    }

    fun onReliabilityChange(item: WeaponItem, increase: Boolean) {
        if (increase) item.currentReliability = item.currentReliability.plus(1)
        else {
            if (item.currentReliability > 0) item.currentReliability =
                item.currentReliability.minus(1)
        }
        checkSaveAvailable()
    }

    fun getArmorSetList(source: Int): ArrayList<ArmorSet> {
        return getArmorSetListUseCase(source)
    }

    fun buyItem(item: EquipmentItem): Boolean {
        return if (item.cost > _crowns.value) {
            false
        } else {
            _crowns.value = _crowns.value.minus(item.cost)
            addEquipmentItem(item)
            true
        }
    }

    fun buyWeapon(item: WeaponItem): Boolean {
        return if (item.cost > _crowns.value) {
            false
        } else {
            _crowns.value = _crowns.value.minus(item.cost)
            addWeaponItem(item)
            true
        }
    }

    fun addCampaignNote(campaignNote: CampaignNote) {
        _campaignNotes.value.add(campaignNote)
        _campaignNotes.value = ArrayList(_campaignNotes.value.sortedWith(compareBy { it.date }))
        checkSaveAvailable()
    }

    fun updateCampaignNote(campaignNote: CampaignNote) {
        var noteToRemove: CampaignNote? = null
        for (note in _campaignNotes.value) {
            if (campaignNote.uniqueID == note.uniqueID) {
                noteToRemove = note
            }
        }
        if (noteToRemove != null) {
            _campaignNotes.value.remove(noteToRemove)
            campaignNote.date = noteToRemove.date
            addCampaignNote(campaignNote)
        }
        checkSaveAvailable()
    }

    fun removeCampaignNote(campaignNote: CampaignNote) {
        _campaignNotes.value.remove(campaignNote)
        checkSaveAvailable()
    }

    fun saveCharInfoMode(mode: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.setCharacterInfoMode(mode)
    }

    fun saveStatsInfoMode(mode: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.setStatsInfoMode(mode)
    }

    fun saveSkillsInfoMode(mode: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.setSkillsInfoMode(mode)
    }

    fun saveSkillTreeInfoMode(mode: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStore.setSkillTreeInfoMode(mode)
    }

    fun getCharacterFile(): File? {
        var file: File? = null
        fromViewModelToCharacter()?.let {
            characterToFileUseCase.invoke(it).onEach { result ->
                file = if (result is Resource.Success) {
                    result.data
                } else {
                    null
                }
            }.launchIn(viewModelScope)
        }
        return file
    }

    private fun fetchCustomMagic() {
        getCustomMagicUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    result.data?.map { it.magicItem }?.let { _customMagicList.value = it }
                }
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchCustomWeapons() {
        getCustomWeaponsUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    result.data?.map { it.equipment }?.let { _customWeaponList.value = it }
                }
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchCustomEquipment() {
        getCustomEquipmentUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    result.data?.map { it.equipment }?.let { _customEquipmentList.value = it }
                }
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun deleteCustomMagic(magicItem: MagicItem){
        deleteCustomMagicUseCase(CustomMagic(magicItem)).launchIn(viewModelScope)
    }

    fun deleteCustomWeapon(weaponItem: WeaponItem){
        deleteCustomWeaponUseCase(CustomWeapon(weaponItem)).launchIn(viewModelScope)
    }

    fun deleteCustomEquipment(equipmentItem: EquipmentItem){
        deleteCustomEquipmentUseCase(CustomEquipment(equipmentItem)).launchIn(viewModelScope)
    }

    fun checkSaveAvailable(){
        _saveAvailable.value = checkIfDataChanged()
    }
}
