package com.example.thewitcherrpg.characterSheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.thewitcherrpg.data.Character
import kotlin.math.absoluteValue

class SharedViewModel: ViewModel() {

    var inCharacterCreation: Boolean = false

    private var _name = MutableLiveData("")
    val name: LiveData<String> = _name

    private var _iP = MutableLiveData(44)
    val iP: LiveData<Int> = _iP

    private var _race = MutableLiveData("")
    val race: LiveData<String> = _race

    private var _gender = MutableLiveData("")
    val gender: LiveData<String> = _gender

    private var _profession = MutableLiveData("")
    val profession: LiveData<String> = _profession

    private var _age = MutableLiveData(0)
    val age: LiveData<Int> = _age

    private var _definingSkill = MutableLiveData("")
    val definingSkill: LiveData<String> = _definingSkill


    //Stats
    private var _intelligence = MutableLiveData(0)
    val intelligence: LiveData<Int> = _intelligence

    private var _ref = MutableLiveData(0)
    val ref: LiveData<Int> = _ref

    private var _dex = MutableLiveData(0)
    val dex: LiveData<Int> = _dex

    private var _body = MutableLiveData(0)
    val body: LiveData<Int> = _body

    private var _spd = MutableLiveData(0)
    val spd: LiveData<Int> = _spd

    private var _emp = MutableLiveData(0)
    val emp: LiveData<Int> = _emp

    private var _cra = MutableLiveData(0)
    val cra: LiveData<Int> = _cra

    private var _will = MutableLiveData(0)
    val will: LiveData<Int> = _will

    private var _luck = MutableLiveData(0)
    val luck: LiveData<Int> = _luck

    private var _stun = MutableLiveData(0)
    val stun: LiveData<Int> = _stun

    private var _run = MutableLiveData(0)
    val run: LiveData<Int> = _run

    private var _leap = MutableLiveData(0)
    val leap: LiveData<Int> = _leap

    private var _maxHP = MutableLiveData(0)
    val maxHp: LiveData<Int> = _maxHP

    private var _hp = MutableLiveData(0)
    val hp: LiveData<Int> = _hp

    private var _maxSta = MutableLiveData(0)
    val maxSta: LiveData<Int> = _maxSta

    private var _sta = MutableLiveData(0)
    val sta: LiveData<Int> = _sta

    private var _enc = MutableLiveData(0)
    val enc: LiveData<Int> = _enc

    private var _rec = MutableLiveData(0)
    val rec: LiveData<Int> = _rec

    private var _punch = MutableLiveData("1d6 +2")
    val punch: LiveData<String> = _punch

    private var _kick = MutableLiveData("1d6 +6")
    val kick: LiveData<String> = _kick


    //Skills
    private var _awareness = MutableLiveData(0)
    val awareness: LiveData<Int> = _awareness

    private var _business = MutableLiveData(0)
    val business: LiveData<Int> = _business

    private var _deduction = MutableLiveData(0)
    val deduction: LiveData<Int> = _deduction

    private var _education = MutableLiveData(0)
    val education: LiveData<Int> = _education

    private var _commonSpeech = MutableLiveData(0)
    val commonSpeech: LiveData<Int> = _commonSpeech

    private var _elderSpeech = MutableLiveData(0)
    val elderSpeech: LiveData<Int> = _elderSpeech

    private var _dwarven = MutableLiveData(0)
    val dwarven: LiveData<Int> = _dwarven

    private var _monsterLore = MutableLiveData(0)
    val monsterLore: LiveData<Int> = _monsterLore

    private var _socialEtiquette = MutableLiveData(0)
    val socialEtiquette: LiveData<Int> = _socialEtiquette

    private var _streetwise = MutableLiveData(0)
    val streetwise: LiveData<Int> = _streetwise

    private var _tactics = MutableLiveData(0)
    val tactics: LiveData<Int> = _tactics

    private var _teaching = MutableLiveData(0)
    val teaching: LiveData<Int> = _teaching

    private var _wildernessSurvival = MutableLiveData(0)
    val wildernessSurvival: LiveData<Int> = _wildernessSurvival

    private var _brawling = MutableLiveData(0)
    val brawling: LiveData<Int> = _brawling

    private var _dodgeEscape = MutableLiveData(0)
    val dodgeEscape: LiveData<Int> = _dodgeEscape

    private var _melee = MutableLiveData(0)
    val melee: LiveData<Int> = _melee

    private var _riding = MutableLiveData(0)
    val riding: LiveData<Int> = _riding

    private var _sailing = MutableLiveData(0)
    val sailing: LiveData<Int> = _sailing

    private var _smallBlades = MutableLiveData(0)
    val smallBlades: LiveData<Int> = _smallBlades

    private var _staffSpear = MutableLiveData(0)
    val staffSpear: LiveData<Int> = _staffSpear

    private var _swordsmanship = MutableLiveData(0)
    val swordsmanship: LiveData<Int> = _swordsmanship

    private var _archery = MutableLiveData(0)
    val archery: LiveData<Int> = _archery

    private var _athletics = MutableLiveData(0)
    val athletics: LiveData<Int> = _athletics

    private var _crossbow = MutableLiveData(0)
    val crossbow: LiveData<Int> = _crossbow

    private var _sleightOfHand = MutableLiveData(0)
    val sleightOfHand: LiveData<Int> = _sleightOfHand

    private var _stealth = MutableLiveData(0)
    val stealth: LiveData<Int> = _stealth

    private var _physique = MutableLiveData(0)
    val physique: LiveData<Int> = _physique

    private var _endurance = MutableLiveData(0)
    val endurance: LiveData<Int> = _endurance

    private var _charisma = MutableLiveData(0)
    val charisma: LiveData<Int> = _charisma

    private var _deceit = MutableLiveData(0)
    val deceit: LiveData<Int> = _deceit

    private var _fineArts = MutableLiveData(0)
    val fineArts: LiveData<Int> = _fineArts

    private var _gambling = MutableLiveData(0)
    val gambling: LiveData<Int> = _gambling

    private var _groomingAndStyle = MutableLiveData(0)
    val groomingAndStyle: LiveData<Int> = _groomingAndStyle

    private var _humanPerception = MutableLiveData(0)
    val humanPerception: LiveData<Int> = _humanPerception

    private var _leadership = MutableLiveData(0)
    val leadership: LiveData<Int> = _leadership

    private var _persuasion = MutableLiveData(0)
    val persuasion: LiveData<Int> = _persuasion

    private var _performance = MutableLiveData(0)
    val performance: LiveData<Int> = _performance

    private var _seduction = MutableLiveData(0)
    val seduction: LiveData<Int> = _seduction

    private var _alchemy = MutableLiveData(0)
    val alchemy: LiveData<Int> = _alchemy

    private var _crafting = MutableLiveData(0)
    val crafting: LiveData<Int> = _crafting

    private var _disguise = MutableLiveData(0)
    val disguise: LiveData<Int> = _disguise

    private var _firstAid = MutableLiveData(0)
    val firstAid: LiveData<Int> = _firstAid

    private var _forgery = MutableLiveData(0)
    val forgery: LiveData<Int> = _forgery

    private var _pickLock = MutableLiveData(0)
    val pickLock: LiveData<Int> = _pickLock

    private var _trapCrafting = MutableLiveData(0)
    val trapCrafting: LiveData<Int> = _trapCrafting

    private var _courage = MutableLiveData(0)
    val courage: LiveData<Int> = _courage

    private var _hexWeaving = MutableLiveData(0)
    val hexWeaving: LiveData<Int> = _hexWeaving

    private var _intimidation = MutableLiveData(0)
    val intimidation: LiveData<Int> = _intimidation

    private var _spellCasting = MutableLiveData(0)
    val spellCasting: LiveData<Int> = _spellCasting

    private var _resistMagic = MutableLiveData(0)
    val resistMagic: LiveData<Int> = _resistMagic

    private var _resistCoercion = MutableLiveData(0)
    val resistCoercion: LiveData<Int> = _resistCoercion

    private var _ritualCrafting = MutableLiveData(0)
    val ritualCrafting: LiveData<Int> = _ritualCrafting


    //Setter Functions
    fun setName(name: String){
        _name.value = name
    }

    fun setIP(ip: Int){
        _iP.value = ip
    }

    fun setRace(race: String){
        _race.value = race
    }
    fun setGender(gender: String){
        _gender.value = gender
    }

    fun setAge(age: Int){
        _age.value = age
    }

    fun setProfession(profession: String){
        _profession.value = profession
    }

    fun setDefiningSkill(defSkill: String){
        _definingSkill.value = defSkill
    }

    fun setIntelligence(intelligence: Int){
        _intelligence.value = intelligence
    }

    fun setReflex(reflex: Int){
        _ref.value = reflex
    }

    fun setDexterity(dexterity: Int){
        _dex.value = dexterity
    }

    fun setBody(body: Int){
        _body.value = body
    }

    fun setSpeed(speed: Int){
        _spd.value = speed
    }

    fun setEmpathy(empathy: Int){
        _emp.value = empathy
    }

    fun setCraftsmanship(craftsmanship: Int){
        _cra.value = craftsmanship
    }

    fun setWill(will: Int){
        _will.value = will
    }

    fun setLuck(luck: Int){
        _luck.value = luck
    }

    fun setStun(stun: Int){
        _stun.value = stun
    }

    fun setRun(run: Int){
        _run.value = run
    }

    fun setLeap(leap: Int){
        _leap.value = leap
    }

    fun setMaxHP(hp: Int){
        _maxHP.value = hp
    }

    fun setHP(hp: Int){
        _hp.value = hp
    }

    fun setMaxStamina(stamina: Int){
        _maxSta.value = stamina
    }

    fun setStamina(stamina: Int){
        _sta.value = stamina
    }

    fun setEncumbrance(enc: Int){
        _enc.value = enc
    }

    fun setRecovery(recovery: Int){
        _rec.value = recovery
    }

    fun setPunch(punch: String){
        _punch.value = punch
    }

    fun setKick(kick: String){
        _kick.value = kick
    }

    fun setAwareness(awareness: Int){
        _awareness.value = awareness
    }

    fun setBusiness(business: Int){
        _business.value = business
    }

    fun setDeduction(deduction: Int){
        _deduction.value = deduction
    }

    fun setEducation(education: Int){
        _education.value = education
    }

    fun setCommonSpeech(commonSpeech: Int){
        _commonSpeech.value = commonSpeech
    }

    fun setElderSpeech(elderSpeech: Int){
        _elderSpeech.value = elderSpeech
    }

    fun setDwarven(dwarven: Int){
        _dwarven.value = dwarven
    }

    fun setMonsterLore(monsterLore: Int){
        _monsterLore.value = monsterLore
    }

    fun setSocialEtiquette(socialEtiquette: Int){
        _socialEtiquette.value = socialEtiquette
    }

    fun setStreetwise(streetwise: Int){
        _streetwise.value = streetwise
    }

    fun setTactics(tactics: Int){
        _tactics.value = tactics
    }

    fun setTeaching(teaching: Int){
        _teaching.value = teaching
    }

    fun setWildernessSurvival(wildernessSurvival: Int){
        _wildernessSurvival.value = wildernessSurvival
    }

    fun setBrawling(brawling: Int){
        _brawling.value = brawling
    }

    fun setDodgeEscape(dodgeEscape: Int){
        _dodgeEscape.value = dodgeEscape
    }

    fun setMelee(melee: Int){
        _melee.value = melee
    }

    fun setRiding(riding: Int){
        _riding.value = riding
    }

    fun setSailing(sailing: Int){
        _sailing.value = sailing
    }

    fun setSmallBlades(smallBlades: Int){
        _smallBlades.value = smallBlades
    }

    fun setStaffSpear(staffSpear: Int){
        _staffSpear.value = staffSpear
    }

    fun setSwordsmanship(swordsmanship: Int){
        _swordsmanship.value = swordsmanship
    }

    fun setArchery(archery: Int){
        _archery.value = archery
    }

    fun setAthletics(athletics: Int){
        _athletics.value = athletics
    }

    fun setCrossbow(crossbow: Int){
        _crossbow.value = crossbow
    }

    fun setSleightOfHand(sleightOfHand: Int){
        _sleightOfHand.value = sleightOfHand
    }

    fun setStealth(stealth: Int){
        _stealth.value = stealth
    }

    fun setPhysique(physique: Int){
        _physique.value = physique
    }

    fun setEndurance(endurance: Int){
        _endurance.value = endurance
    }

    fun setCharisma(charisma: Int){
        _charisma.value = charisma
    }

    fun setDeceit(deceit: Int){
        _deceit.value = deceit
    }

    fun setFineArts(fineArts: Int){
        _fineArts.value = fineArts
    }

    fun setGambling(gambling: Int) {
        _gambling.value = gambling
    }

    fun setGroomingAndStyle(groomingAndStyle: Int) {
        _groomingAndStyle.value = groomingAndStyle
    }

    fun setHumanPerception(humanPerception: Int) {
        _humanPerception.value = humanPerception
    }

    fun setLeadership(leadership: Int) {
        _leadership.value = leadership
    }

    fun setPersuasion(persuasion: Int) {
        _persuasion.value = persuasion
    }

    fun setPerformance(performance: Int) {
        _performance.value = performance
    }

    fun setSeduction(seduction: Int) {
        _seduction.value = seduction
    }

    fun setAlchemy(alchemy: Int) {
        _alchemy.value = alchemy
    }

    fun setCrafting(crafting: Int) {
        _crafting.value = crafting
    }

    fun setDisguise(disguise: Int) {
        _disguise.value = disguise
    }

    fun setFirstAid(firstAid: Int) {
        _firstAid.value = firstAid
    }

    fun setForgery(forgery: Int) {
        _forgery.value = forgery
    }

    fun setPickLock(pickLock: Int) {
        _pickLock.value = pickLock
    }

    fun setTrapCrafting(trapCrafting: Int) {
        _trapCrafting.value = trapCrafting
    }

    fun setCourage(courage: Int) {
        _courage.value = courage
    }

    fun setHexWeaving(hexWeaving: Int) {
        _hexWeaving.value = hexWeaving
    }

    fun setIntimidation(intimidation: Int) {
        _intimidation.value = intimidation
    }

    fun setSpellCasting(spellCasting: Int) {
        _spellCasting.value = spellCasting
    }

    fun setResistMagic(resistMagic: Int) {
        _resistMagic.value = resistMagic
    }

    fun setResistCoercion(resistCoercion: Int) {
        _resistCoercion.value = resistCoercion
    }

    fun setRitualCrafting(ritualCrafting: Int) {
        _ritualCrafting.value = ritualCrafting
    }

    private fun onStatChange(value: Int, increase: Boolean): Boolean{

        var newIP = iP.value!!
        var newVal = value

        if(!inCharacterCreation) {
            if (increase) {
                if (newVal == 0) newVal = 1

                if (iP.value!! >= newVal*10) {
                    newIP -= newVal * 10
                } else return false
            }

            if (!increase) {
                if (newVal > 0) {
                    if (newVal == 1) newVal = 2

                    newIP += (newVal - 1) * 10
                } else return false
            }
            _iP.value = newIP
        }
        else {
            if (increase) {
                if (iP.value!! > 0 && newVal < 10) {
                    newIP -= 1
                } else return false
            }

            if (!increase) {
                if (newVal > 0) {

                    newIP += 1
                } else return false
            }
            _iP.value = newIP
        }
        return true

    }

    private fun onSkillChange(value: Int, increase: Boolean): Boolean{

        var newIP = iP.value!!
        var newVal = value

        if(!inCharacterCreation) {
            if (increase) {
                if (newVal == 0) newVal = 1

                if (iP.value!! >= newVal) {
                    newIP -= newVal
                } else return false
            }

            if (!increase) {
                if (newVal > 0) {
                    if (newVal == 1) newVal = 2

                    newIP += (newVal - 1)
                } else return false
            }
            _iP.value = newIP
        }
        else {
            if (increase) {
                if (iP.value!! > 0 && newVal < 6) {
                    newIP -= 1
                } else return false
            }

            if (!increase) {
                if (newVal > 0) {

                    newIP += 1
                } else return false
            }
            _iP.value = newIP
        }
        return true
    }

    fun onSaveFinal(): Character{

        val name = name.value!!
        val ip = iP.value!!
        val race = race.value!!
        val gender = gender.value!!
        val age = age.value!!
        val profession = profession.value!!
        val definingSkill = definingSkill.value!!


        //Stats
        val inte = _intelligence.value!!
        val ref = _ref.value!!
        val dex = _dex.value!!
        val body = _body.value!!
        val spd = _spd.value!!
        val emp = _emp.value!!
        val cra = _cra.value!!
        val will = _will.value!!
        val luck = _luck.value!!
        val stun = _stun.value!!
        val run = _run.value!!
        val leap = _leap.value!!
        val maxHp = _maxHP.value!!
        val hp = _hp.value!!
        val maxSta = _maxSta.value!!
        val sta = _sta.value!!
        val enc = _enc.value!!
        val rec = _rec.value!!
        val punch = _punch.value!!
        val kick = _kick.value!!

        //Skills
        val awareness = _awareness.value!!
        val business = _business.value!!
        val deduction = _deduction.value!!
        val education = _education.value!!
        val commonSpeech = _commonSpeech.value!!
        val elderSpeech = _elderSpeech.value!!
        val dwarven = _dwarven.value!!
        val monsterLore = _monsterLore.value!!
        val socialEtiquette = _socialEtiquette.value!!
        val streetwise = _streetwise.value!!
        val tactics = _tactics.value!!
        val teaching = _teaching.value!!
        val wildernessSurvival = _wildernessSurvival.value!!
        val brawling = _brawling.value!!
        val dodgeEscape = _dodgeEscape.value!!
        val melee = _melee.value!!
        val riding = _riding.value!!
        val sailing = _sailing.value!!
        val smallBlades = _smallBlades.value!!
        val staffSpear = _staffSpear.value!!
        val swordsmanship = _swordsmanship.value!!
        val archery = _archery.value!!
        val athletics = _athletics.value!!
        val crossbow = _crossbow.value!!
        val sleightOfHand = _sleightOfHand.value!!
        val stealth = _stealth.value!!
        val physique = _physique.value!!
        val endurance = _endurance.value!!
        val charisma = _charisma.value!!
        val deceit = _deceit.value!!
        val fineArts = _fineArts.value!!
        val gambling = _gambling.value!!
        val groomingAndStyle = _groomingAndStyle.value!!
        val humanPerception = _humanPerception.value!!
        val leadership = _leadership.value!!
        val persuasion = _persuasion.value!!
        val performance = _performance.value!!
        val seduction = _seduction.value!!
        val alchemy = _alchemy.value!!
        val crafting = _crafting.value!!
        val disguise = _disguise.value!!
        val firstAid = _firstAid.value!!
        val forgery = _forgery.value!!
        val pickLock = _pickLock.value!!
        val trapCrafting = _trapCrafting.value!!
        val courage = _courage.value!!
        val hexWeaving = _hexWeaving.value!!
        val intimidation = _intimidation.value!!
        val spellCasting = _spellCasting.value!!
        val resistMagic = _resistMagic.value!!
        val resistCoercion = _resistCoercion.value!!
        val ritualCrafting = _ritualCrafting.value!!

        return Character(0, name, ip, race, gender, age, profession, definingSkill,
            inte, ref, dex, body, spd, emp, cra, will, luck, stun, run, leap, maxHp, hp, maxSta, sta, enc, rec, punch, kick,
            awareness, business, deduction, education, commonSpeech, elderSpeech, dwarven, monsterLore, socialEtiquette,
            streetwise, tactics, teaching, wildernessSurvival, brawling, dodgeEscape, melee, riding, sailing, smallBlades,
            staffSpear, swordsmanship, archery, athletics, crossbow, sleightOfHand, stealth, physique, endurance, charisma,
            deceit, fineArts, gambling, groomingAndStyle, humanPerception, leadership, persuasion, performance, seduction,
            alchemy, crafting, disguise, firstAid, forgery, pickLock, trapCrafting, courage, hexWeaving, intimidation,
            spellCasting, resistMagic, resistCoercion, ritualCrafting)

    }

    fun increaseStat(stat: Int){
        when (stat){
            1 -> _intelligence.value = if(onStatChange(_intelligence.value!!, true)) _intelligence.value!!.plus(1) else _intelligence.value
            2 -> _ref.value = if(onStatChange(_ref.value!!, true)) _ref.value!!.plus(1) else _ref.value
            3 -> _dex.value = if(onStatChange(_dex.value!!, true)) _dex.value!!.plus(1) else _dex.value
            4 -> {_body.value = if(onStatChange(_body.value!!, true)) _body.value!!.plus(1) else _body.value; updateDerivedStats()}
            5 -> {_spd.value = if(onStatChange(_spd.value!!, true)) _spd.value!!.plus(1) else _spd.value; updateDerivedStats()}
            6 -> _emp.value = if(onStatChange(_emp.value!!, true)) _emp.value!!.plus(1) else _emp.value
            7 -> _cra.value = if(onStatChange(_cra.value!!, true)) _cra.value!!.plus(1) else _cra.value
            8 -> {_will.value = if(onStatChange(_will.value!!, true)) _will.value!!.plus(1) else _will.value; updateDerivedStats()}
            9 -> _luck.value = if(onStatChange(_luck.value!!, true)) _luck.value!!.plus(1) else _luck.value
            10 -> _stun.value = _stun.value!!.plus(1)
            11 -> {_run.value = _run.value!!.plus(1); updateDerivedStats()}
            12 -> _leap.value = _leap.value!!.plus(1)
            13 -> _maxHP.value = _maxHP.value!!.plus(1)
            14 -> _maxSta.value = _maxSta.value!!.plus(1)
            15 -> _enc.value = _enc.value!!.plus(1)
            16 -> _rec.value = _rec.value!!.plus(1)

        }
    }

    fun decreaseStat(stat: Int){
        when (stat){
            1 -> _intelligence.value = if(onStatChange(_intelligence.value!!, false)) _intelligence.value!!.minus(1) else _intelligence.value
            2 -> _ref.value = if(onStatChange(_ref.value!!, false)) _ref.value!!.minus(1) else _ref.value
            3 -> _dex.value = if(onStatChange(_dex.value!!, false)) _dex.value!!.minus(1) else _dex.value
            4 -> {_body.value = if(onStatChange(_body.value!!, false)) _body.value!!.minus(1) else _body.value; ; updateDerivedStats()}
            5 -> {_spd.value = if(onStatChange(_spd.value!!, false)) _spd.value!!.minus(1) else _spd.value; updateDerivedStats()}
            6 -> _emp.value = if(onStatChange(_emp.value!!, false)) _emp.value!!.minus(1) else _emp.value
            7 -> _cra.value = if(onStatChange(_cra.value!!, false)) _cra.value!!.minus(1) else _cra.value
            8 -> {_will.value = if(onStatChange(_will.value!!, false)) _will.value!!.minus(1) else _will.value; ; updateDerivedStats()}
            9 -> _luck.value = if(onStatChange(_luck.value!!, false)) _luck.value!!.minus(1) else _luck.value
            10 -> _stun.value = _stun.value!!.minus(1)
            11 -> {_run.value = _run.value!!.minus(1); updateDerivedStats()}
            12 -> _leap.value = _leap.value!!.minus(1)
            13 -> _maxHP.value = _maxHP.value!!.minus(1)
            14 -> _maxSta.value = _maxSta.value!!.minus(1)
            15 -> _enc.value = _enc.value!!.minus(1)
            16 -> _rec.value = _rec.value!!.minus(1)

        }
    }

    fun increaseSkill(stat: Int){
        when (stat){
            1 -> _awareness.value = if(onSkillChange(_awareness.value!!, true)) _awareness.value!!.plus(1) else _awareness.value
            2 -> _business.value = if(onSkillChange(_business.value!!, true)) _business.value!!.plus(1) else _business.value
            3 -> _deduction.value = if(onStatChange(_deduction.value!!, true)) _deduction.value!!.plus(1) else _deduction.value
            4 -> _education.value = if(onSkillChange(_education.value!!, true)) _education.value!!.plus(1) else _education.value
            5 -> _commonSpeech.value = if(onSkillChange(_commonSpeech.value!!, true)) _commonSpeech.value!!.plus(1) else _commonSpeech.value
            6 -> _elderSpeech.value = if(onSkillChange(_elderSpeech.value!!, true)) _elderSpeech.value!!.plus(1) else _elderSpeech.value
            7 -> _dwarven.value = if(onSkillChange(_dwarven.value!!, true)) _dwarven.value!!.plus(1) else _dwarven.value
            8 -> _monsterLore.value = if(onSkillChange(_monsterLore.value!!, true)) _monsterLore.value!!.plus(1) else _monsterLore.value
            9 -> _socialEtiquette.value = if(onSkillChange(_socialEtiquette.value!!, true)) _socialEtiquette.value!!.plus(1) else _socialEtiquette.value
            10 -> _streetwise.value = if(onSkillChange(_streetwise.value!!, true)) _streetwise.value!!.plus(1) else _streetwise.value
            11 -> _tactics.value = if(onSkillChange(_tactics.value!!, true)) _tactics.value!!.plus(1) else _tactics.value
            12 -> _teaching.value = if(onSkillChange(_teaching.value!!, true)) _teaching.value!!.plus(1) else _teaching.value
            13 -> _wildernessSurvival.value = if(onSkillChange(_wildernessSurvival.value!!, true)) _wildernessSurvival.value!!.plus(1) else _wildernessSurvival.value
            14 -> _brawling.value = if(onSkillChange(_brawling.value!!, true)) _brawling.value!!.plus(1) else _brawling.value
            15 -> _dodgeEscape.value = if(onSkillChange(_dodgeEscape.value!!, true)) _dodgeEscape.value!!.plus(1) else _dodgeEscape.value
            16 -> _melee.value = if(onSkillChange(_melee.value!!, true)) _melee.value!!.plus(1) else _melee.value
            17 -> _riding.value = if(onSkillChange(_riding.value!!, true)) _riding.value!!.plus(1) else _riding.value
            18 -> _sailing.value = if(onSkillChange(_sailing.value!!, true)) _sailing.value!!.plus(1) else _sailing.value
            19 -> _smallBlades.value = if(onSkillChange(_smallBlades.value!!, true)) _smallBlades.value!!.plus(1) else _smallBlades.value
            20 -> _staffSpear.value = if(onSkillChange(_staffSpear.value!!, true)) _staffSpear.value!!.plus(1) else _staffSpear.value
            21 -> _swordsmanship.value = if(onSkillChange(_swordsmanship.value!!, true)) _swordsmanship.value!!.plus(1) else _swordsmanship.value
            22 -> _archery.value = if(onSkillChange(_archery.value!!, true)) _archery.value!!.plus(1) else _archery.value
            23 -> _athletics.value = if(onSkillChange(_athletics.value!!, true)) _athletics.value!!.plus(1) else _athletics.value
            24 -> _crossbow.value = if(onSkillChange(_crossbow.value!!, true)) _crossbow.value!!.plus(1) else _crossbow.value
            25 -> _sleightOfHand.value = if(onSkillChange(_sleightOfHand.value!!, true)) _sleightOfHand.value!!.plus(1) else _sleightOfHand.value
            26 -> _stealth.value = if(onSkillChange(_stealth.value!!, true)) _stealth.value!!.plus(1) else _stealth.value
            27 -> _physique.value = if(onSkillChange(_physique.value!!, true)) _physique.value!!.plus(1) else _physique.value
            28 -> _endurance.value = if(onSkillChange(_endurance.value!!, true)) _endurance.value!!.plus(1) else _endurance.value
            29 -> _charisma.value = if(onSkillChange(_charisma.value!!, true)) _charisma.value!!.plus(1) else _charisma.value
            30 -> _deceit.value = if(onSkillChange(_deceit.value!!, true)) _deceit.value!!.plus(1) else _deceit.value
            31 -> _fineArts.value = if(onSkillChange(_fineArts.value!!, true)) _fineArts.value!!.plus(1) else _fineArts.value
            32 -> _gambling.value = if(onSkillChange(_gambling.value!!, true)) _gambling.value!!.plus(1) else _gambling.value
            33 -> _groomingAndStyle.value = if(onSkillChange(_groomingAndStyle.value!!, true)) _groomingAndStyle.value!!.plus(1) else _groomingAndStyle.value
            34 -> _humanPerception.value = if(onSkillChange(_humanPerception.value!!, true)) _humanPerception.value!!.plus(1) else _humanPerception.value
            35 -> _leadership.value = if(onSkillChange(_leadership.value!!, true)) _leadership.value!!.plus(1) else _leadership.value
            36 -> _persuasion.value = if(onSkillChange(_persuasion.value!!, true)) _persuasion.value!!.plus(1) else _persuasion.value
            37 -> _performance.value = if(onSkillChange(_performance.value!!, true)) _performance.value!!.plus(1) else _performance.value
            38 -> _seduction.value = if(onSkillChange(_seduction.value!!, true)) _seduction.value!!.plus(1) else _seduction.value
            39 -> _alchemy.value = if(onSkillChange(_alchemy.value!!, true)) _alchemy.value!!.plus(1) else _alchemy.value
            40 -> _crafting.value = if(onSkillChange(_crafting.value!!, true)) _crafting.value!!.plus(1) else _crafting.value
            41 -> _disguise.value = if(onSkillChange(_disguise.value!!, true)) _disguise.value!!.plus(1) else _disguise.value
            42 -> _firstAid.value = if(onSkillChange(_firstAid.value!!, true)) _firstAid.value!!.plus(1) else _firstAid.value
            43 -> _forgery.value = if(onSkillChange(_alchemy.value!!, true)) _alchemy.value!!.plus(1) else _alchemy.value
            44 -> _pickLock.value = if(onSkillChange(_pickLock.value!!, true)) _pickLock.value!!.plus(1) else _pickLock.value
            45 -> _trapCrafting.value = if(onSkillChange(_trapCrafting.value!!, true)) _trapCrafting.value!!.plus(1) else _trapCrafting.value
            46 -> _courage.value = if(onSkillChange(_courage.value!!, true)) _courage.value!!.plus(1) else _courage.value
            47 -> _hexWeaving.value = if(onSkillChange(_hexWeaving.value!!, true)) _hexWeaving.value!!.plus(1) else _hexWeaving.value
            48 -> _intimidation.value = if(onSkillChange(_intimidation.value!!, true)) _intimidation.value!!.plus(1) else _intimidation.value
            49 -> _spellCasting.value = if(onSkillChange(_spellCasting.value!!, true)) _spellCasting.value!!.plus(1) else _spellCasting.value
            50 -> _resistMagic.value = if(onSkillChange(_resistMagic.value!!, true)) _resistMagic.value!!.plus(1) else _resistMagic.value
            51 -> _resistCoercion.value = if(onSkillChange(_resistCoercion.value!!, true)) _resistCoercion.value!!.plus(1) else _resistCoercion.value
            52 -> _ritualCrafting.value = if(onSkillChange(_ritualCrafting.value!!, true)) _ritualCrafting.value!!.plus(1) else _ritualCrafting.value

        }
    }

    fun decreaseSkill(stat: Int){
        when (stat){
            1 -> _awareness.value = if(onSkillChange(_awareness.value!!, false)) _awareness.value!!.minus(1) else _awareness.value
            2 -> _business.value = if(onSkillChange(_business.value!!, false)) _business.value!!.minus(1) else _business.value
            3 -> _deduction.value = if(onStatChange(_deduction.value!!, false)) _deduction.value!!.minus(1) else _deduction.value
            4 -> _education.value = if(onSkillChange(_education.value!!, false)) _education.value!!.minus(1) else _education.value
            5 -> _commonSpeech.value = if(onSkillChange(_commonSpeech.value!!, false)) _commonSpeech.value!!.minus(1) else _commonSpeech.value
            6 -> _elderSpeech.value = if(onSkillChange(_elderSpeech.value!!, false)) _elderSpeech.value!!.minus(1) else _elderSpeech.value
            7 -> _dwarven.value = if(onSkillChange(_dwarven.value!!, false)) _dwarven.value!!.minus(1) else _dwarven.value
            8 -> _monsterLore.value = if(onSkillChange(_monsterLore.value!!, false)) _monsterLore.value!!.minus(1) else _monsterLore.value
            9 -> _socialEtiquette.value = if(onSkillChange(_socialEtiquette.value!!, false)) _socialEtiquette.value!!.minus(1) else _socialEtiquette.value
            10 -> _streetwise.value = if(onSkillChange(_streetwise.value!!, false)) _streetwise.value!!.minus(1) else _streetwise.value
            11 -> _tactics.value = if(onSkillChange(_tactics.value!!, false)) _tactics.value!!.minus(1) else _tactics.value
            12 -> _teaching.value = if(onSkillChange(_teaching.value!!, false)) _teaching.value!!.minus(1) else _teaching.value
            13 -> _wildernessSurvival.value = if(onSkillChange(_wildernessSurvival.value!!, false)) _wildernessSurvival.value!!.minus(1) else _wildernessSurvival.value
            14 -> _brawling.value = if(onSkillChange(_brawling.value!!, false)) _brawling.value!!.minus(1) else _brawling.value
            15 -> _dodgeEscape.value = if(onSkillChange(_dodgeEscape.value!!, false)) _dodgeEscape.value!!.minus(1) else _dodgeEscape.value
            16 -> _melee.value = if(onSkillChange(_melee.value!!, false)) _melee.value!!.minus(1) else _melee.value
            17 -> _riding.value = if(onSkillChange(_riding.value!!, false)) _riding.value!!.minus(1) else _riding.value
            18 -> _sailing.value = if(onSkillChange(_sailing.value!!, false)) _sailing.value!!.minus(1) else _sailing.value
            19 -> _smallBlades.value = if(onSkillChange(_smallBlades.value!!, false)) _smallBlades.value!!.minus(1) else _smallBlades.value
            20 -> _staffSpear.value = if(onSkillChange(_staffSpear.value!!, false)) _staffSpear.value!!.minus(1) else _staffSpear.value
            21 -> _swordsmanship.value = if(onSkillChange(_swordsmanship.value!!, false)) _swordsmanship.value!!.minus(1) else _swordsmanship.value
            22 -> _archery.value = if(onSkillChange(_archery.value!!, false)) _archery.value!!.minus(1) else _archery.value
            23 -> _athletics.value = if(onSkillChange(_athletics.value!!, false)) _athletics.value!!.minus(1) else _athletics.value
            24 -> _crossbow.value = if(onSkillChange(_crossbow.value!!, false)) _crossbow.value!!.minus(1) else _crossbow.value
            25 -> _sleightOfHand.value = if(onSkillChange(_sleightOfHand.value!!, false)) _sleightOfHand.value!!.minus(1) else _sleightOfHand.value
            26 -> _stealth.value = if(onSkillChange(_stealth.value!!, false)) _stealth.value!!.minus(1) else _stealth.value
            27 -> _physique.value = if(onSkillChange(_physique.value!!, false)) _physique.value!!.minus(1) else _physique.value
            28 -> _endurance.value = if(onSkillChange(_endurance.value!!, false)) _endurance.value!!.minus(1) else _endurance.value
            29 -> _charisma.value = if(onSkillChange(_charisma.value!!, false)) _charisma.value!!.minus(1) else _charisma.value
            30 -> _deceit.value = if(onSkillChange(_deceit.value!!, false)) _deceit.value!!.minus(1) else _deceit.value
            31 -> _fineArts.value = if(onSkillChange(_fineArts.value!!, false)) _fineArts.value!!.minus(1) else _fineArts.value
            32 -> _gambling.value = if(onSkillChange(_gambling.value!!, false)) _gambling.value!!.minus(1) else _gambling.value
            33 -> _groomingAndStyle.value = if(onSkillChange(_groomingAndStyle.value!!, false)) _groomingAndStyle.value!!.minus(1) else _groomingAndStyle.value
            34 -> _humanPerception.value = if(onSkillChange(_humanPerception.value!!, false)) _humanPerception.value!!.minus(1) else _humanPerception.value
            35 -> _leadership.value = if(onSkillChange(_leadership.value!!, false)) _leadership.value!!.minus(1) else _leadership.value
            36 -> _persuasion.value = if(onSkillChange(_persuasion.value!!, false)) _persuasion.value!!.minus(1) else _persuasion.value
            37 -> _performance.value = if(onSkillChange(_performance.value!!, false)) _performance.value!!.minus(1) else _performance.value
            38 -> _seduction.value = if(onSkillChange(_seduction.value!!, false)) _seduction.value!!.minus(1) else _seduction.value
            39 -> _alchemy.value = if(onSkillChange(_alchemy.value!!, false)) _alchemy.value!!.minus(1) else _alchemy.value
            40 -> _crafting.value = if(onSkillChange(_crafting.value!!, false)) _crafting.value!!.minus(1) else _crafting.value
            41 -> _disguise.value = if(onSkillChange(_disguise.value!!, false)) _disguise.value!!.minus(1) else _disguise.value
            42 -> _firstAid.value = if(onSkillChange(_firstAid.value!!, false)) _firstAid.value!!.minus(1) else _firstAid.value
            43 -> _forgery.value = if(onSkillChange(_alchemy.value!!, false)) _alchemy.value!!.minus(1) else _alchemy.value
            44 -> _pickLock.value = if(onSkillChange(_pickLock.value!!, false)) _pickLock.value!!.minus(1) else _pickLock.value
            45 -> _trapCrafting.value = if(onSkillChange(_trapCrafting.value!!, false)) _trapCrafting.value!!.minus(1) else _trapCrafting.value
            46 -> _courage.value = if(onSkillChange(_courage.value!!, false)) _courage.value!!.minus(1) else _courage.value
            47 -> _hexWeaving.value = if(onSkillChange(_hexWeaving.value!!, false)) _hexWeaving.value!!.minus(1) else _hexWeaving.value
            48 -> _intimidation.value = if(onSkillChange(_intimidation.value!!, false)) _intimidation.value!!.minus(1) else _intimidation.value
            49 -> _spellCasting.value = if(onSkillChange(_spellCasting.value!!, false)) _spellCasting.value!!.minus(1) else _spellCasting.value
            50 -> _resistMagic.value = if(onSkillChange(_resistMagic.value!!, false)) _resistMagic.value!!.minus(1) else _resistMagic.value
            51 -> _resistCoercion.value = if(onSkillChange(_resistCoercion.value!!, false)) _resistCoercion.value!!.minus(1) else _resistCoercion.value
            52 -> _ritualCrafting.value = if(onSkillChange(_ritualCrafting.value!!, false)) _ritualCrafting.value!!.minus(1) else _ritualCrafting.value

        }
    }

    private fun updateDerivedStats(){
        val multiplier: Int = (_body.value!! + _will.value!!)/2

        _stun.value = multiplier*10
        _run.value = _spd.value!!*3
        _leap.value = _run.value!!/5
        _maxHP.value =  multiplier*5
        _maxSta.value = multiplier*5
        _enc.value = _body.value!!*10
        _rec.value = multiplier

        when(_body.value){
            1,2 -> {_punch.value = "1d6 -4"; _kick.value = "1d6"}
            3,4 -> {_punch.value = "1d6 -2"; _kick.value = "1d6 +2"}
            5,6 -> {_punch.value = "1d6"; _kick.value = "1d6 +4"}
            7,8 -> {_punch.value = "1d6 +2"; _kick.value = "1d6 +6"}
            9,10 -> {_punch.value = "1d6 +4"; _kick.value = "1d6 +8"}
            11,12 -> {_punch.value = "1d6 +6"; _kick.value = "1d6 +10"}
            13 -> {_punch.value = "1d6 +8"; _kick.value = "1d6 +12"}
        }

    }

    fun onHealthChange(value: Int){
        if (value < 0){
            if (value.absoluteValue < _hp.value!!) {
                _hp.value = _hp.value?.plus(value)
            }
            else _hp.value = 0
        }
        else _hp.value = _hp.value?.plus(value)
    }

}

