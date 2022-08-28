package com.witcher.thewitcherrpg.feature_custom_attributes.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.witcher.thewitcherrpg.core.Resource

class CustomMagicComposableFragment {

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Preview
    @Composable
    fun SetCustomMagicContent() {
        val activity = (LocalContext.current as? Activity)
        val customAttributeViewModel: CustomAttributeViewModel = viewModel()

        var magicName by remember {
            mutableStateOf("")
        }
        var typeSelection by remember {
            mutableStateOf(MagicGeneralType.SPELL)
        }
        var levelSelection by remember {
            mutableStateOf(MagicLevel.NOVICE)
        }
        var duration by remember {
            mutableStateOf("")
        }
        var staCost by remember {
            mutableStateOf(0F)
        }
        var isVariable by remember {
            mutableStateOf(false)
        }
        var range by remember {
            mutableStateOf(0F)
        }
        var difficulty by remember {
            mutableStateOf(0F)
        }
        var isVariableDiff by remember {
            mutableStateOf(false)
        }
        var isSelf by remember {
            mutableStateOf(false)
        }
        var defenseOrPrepTime by remember {
            mutableStateOf("")
        }
        var effect by remember {
            mutableStateOf("")
        }
        var componentsOrReqToLift by remember {
            mutableStateOf("")
        }
        var element by remember {
            mutableStateOf(Element.AIR)
        }
        val scrollState = rememberScrollState()

        customAttributeViewModel.addMagicState.observe(LocalLifecycleOwner.current) {
            if (customAttributeViewModel.addMagicState.value is Resource.Success) {
                Toast.makeText(activity, "Magic Added Successfully!", Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }

        Scaffold(topBar = {
            TopAppBar(
                title = {
                    Text(text = "Add New Attribute")
                },
                backgroundColor = MaterialTheme.colors.primaryVariant,
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                }
            )
        }
        ) {
            Column {
                CustomTitle(title = "Custom Magic")

                Column(
                    modifier = Modifier
                        .padding(top = 15.dp, end = 15.dp, start = 15.dp)
                        .verticalScroll(scrollState)
                        .weight(1f, fill = true),
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    CustomTextField(value = magicName, updateValue = {
                        magicName = it
                    }, label = "Magic Name", maxLines = 2)
                    CustomDropDownMenu(
                        updateSelection = { newSelection ->
                            typeSelection =
                                customAttributeViewModel.getMagicTypeFromSelection(newSelection)!!
                        },
                        options = customAttributeViewModel.optionsMagicGeneralTypeMap.keys.toList(),
                        label = "Magic Type"
                    )
                    CustomDropDownMenu(
                        selection = customAttributeViewModel.getMagicLevelSelection(
                            typeSelection,
                            levelSelection
                        ),
                        updateSelection = { newSelection ->
                            levelSelection =
                                customAttributeViewModel.getMagicLevelFromSelection(newSelection)!!
                        },
                        options = customAttributeViewModel.getMagicLevelOptions(typeSelection),
                        label = if (typeSelection == MagicGeneralType.HEX) "Danger Level" else "Magic Level",
                    )
                    AnimatedVisibility(visible = (typeSelection == MagicGeneralType.SPELL || typeSelection == MagicGeneralType.SIGN)) {
                        CustomDropDownMenu(
                            updateSelection = { newSelection ->
                                element =
                                    customAttributeViewModel.getElementFromSelection(newSelection)!!
                            },
                            options = customAttributeViewModel.optionsElementsMap.keys.toList(),
                            label = "Element"
                        )
                    }

                    AnimatedVisibility(visible = (typeSelection != MagicGeneralType.HEX)) {
                        CustomTextField(
                            value = defenseOrPrepTime,
                            updateValue = {
                                defenseOrPrepTime = it
                            },
                            label = if (typeSelection != MagicGeneralType.RITUAL) "Defense" else "Preparation Time"
                        )
                    }

                    AnimatedVisibility(visible = (typeSelection != MagicGeneralType.HEX)) {
                        CustomTextField(value = duration, updateValue = {
                            duration = it
                        }, label = "Duration")
                    }

                    AnimatedVisibility(visible = true) {
                        STACostSliderCard(mainValue = staCost, updateMainValue = { newCost ->
                            staCost = newCost
                        }, isChecked = isVariable, updateIsChecked = { newValue ->
                            isVariable = newValue
                        },
                            initialText = "Stamina Cost: ",
                            variableText = "Variable:",
                            noDisable = false
                        )
                    }

                    AnimatedVisibility(visible = (typeSelection != MagicGeneralType.RITUAL && typeSelection != MagicGeneralType.HEX)) {
                        STACostSliderCard(mainValue = range, updateMainValue = { newCost ->
                            range = newCost
                        }, isChecked = isSelf, updateIsChecked = { newValue ->
                            isSelf = newValue
                        },
                            initialText = "Range: ",
                            variableText = "Self:",
                            noDisable = true
                        )
                    }
                    AnimatedVisibility(visible = (typeSelection == MagicGeneralType.RITUAL)) {
                        STACostSliderCard(mainValue = difficulty, updateMainValue = { newCost ->
                            difficulty = newCost
                        }, isChecked = isVariableDiff, updateIsChecked = { newValue ->
                            isVariableDiff = newValue
                        },
                            initialText = "Difficulty: ",
                            variableText = "Variable:",
                            noDisable = false
                        )
                    }
                    AnimatedVisibility(visible = (typeSelection == MagicGeneralType.RITUAL || typeSelection == MagicGeneralType.HEX)) {
                        CustomTextField(
                            value = componentsOrReqToLift, updateValue = { newValue ->
                                componentsOrReqToLift = newValue
                            },
                            label = if (typeSelection == MagicGeneralType.RITUAL) "Components" else "Requirement To Lift",
                            maxLines = 50
                        )
                    }
                    CustomTextField(value = effect, updateValue = { newEffect ->
                        effect = newEffect
                    }, label = "Effect", maxLines = 50)

                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 15.dp, end = 15.dp, start = 15.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button("Cancel",
                        onClick = {
                            activity?.finish()
                        }
                    )
                    Button(
                        "Save",
                        onClick = {
                            if (
                                !customAttributeViewModel.isMagicValid(
                                    type = typeSelection,
                                    magicLevel = levelSelection,
                                    name = magicName,
                                    staminaCost = staCost.toInt(),
                                    description = effect,
                                    range = range.toInt().toString(),
                                    duration = duration,
                                    defenseOrPrepTime = defenseOrPrepTime,
                                    element = element,
                                    difficulty = difficulty.toInt(),
                                    componentsOrReqToLift = componentsOrReqToLift,
                                    isSelfRange = isSelf,
                                    isVariableDiff = isVariableDiff,
                                    isVariableSta = isVariable
                                )
                            ) {
                                Toast.makeText(activity, "Please fill out necessary fields", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun STACostSliderCard(
    mainValue: Float,
    isChecked: Boolean,
    updateMainValue: (Float) -> Unit,
    updateIsChecked: (Boolean) -> Unit,
    initialText: String,
    variableText: String,
    noDisable: Boolean
) {
    var enabled by rememberSaveable { mutableStateOf(true) }

    enabled = if (isChecked) {
        if (!noDisable) {
            updateMainValue(0F)
            !isChecked
        } else {
            true
        }
    } else {
        if (!noDisable) {
            !isChecked
        } else {
            true
        }
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = initialText + mainValue.toInt().toString(),
                modifier = Modifier.padding(top = 16.dp)
            )
            Row {
                Text(text = variableText, modifier = Modifier.padding(top = 16.dp))
                Checkbox(checked = isChecked, onCheckedChange = {
                    updateIsChecked(it)
                }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary))
            }
        }
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = mainValue,
            onValueChange = { updateMainValue(it) },
            steps = 20,
            valueRange = 0F..30F,
            enabled = enabled
        )
    }
}

enum class MagicGeneralType {
    SPELL,
    INVOCATION,
    RITUAL,
    HEX,
    SIGN
}

enum class Element {
    AIR,
    EARTH,
    FIRE,
    WATER,
    MIXED_ELEMENT
}

enum class MagicLevel {
    NOVICE,
    JOURNEYMAN,
    MASTER,
    NOVICE_DRUID,
    JOURNEYMAN_DRUID,
    MASTER_DRUID,
    NOVICE_PREACHER,
    JOURNEYMAN_PREACHER,
    MASTER_PREACHER,
    ARCH_PRIEST,
    LOW,
    MEDIUM,
    HIGH,
    BASIC,
    ALTERNATE
}

@Composable
fun Button(
    label: String = "Save",
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        border = BorderStroke(1.dp, MaterialTheme.colors.primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Transparent),
        shape = RoundedCornerShape(15.dp)
    ) {
        Text(text = label, color = MaterialTheme.colors.onPrimary, fontSize = 16.sp)
    }
}