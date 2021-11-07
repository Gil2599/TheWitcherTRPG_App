package com.example.thewitcherrpg.feature_character_sheet.presentation.equipment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.thewitcherrpg.R
import com.example.thewitcherrpg.core.presentation.MainCharacterViewModel
import com.example.thewitcherrpg.databinding.CustomDialogCharArmorBinding
import com.example.thewitcherrpg.databinding.CustomDialogWeaponBinding
import com.example.thewitcherrpg.databinding.FragmentEquipmentBinding
import com.example.thewitcherrpg.feature_character_sheet.domain.item_models.EquipmentItem
import com.example.thewitcherrpg.feature_character_sheet.domain.item_types.EquipmentTypes

class EquipmentFragment : Fragment() {
    private var _binding: FragmentEquipmentBinding? = null
    private val binding get() = _binding!!

    private val mainCharacterViewModel: MainCharacterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEquipmentBinding.inflate(inflater, container, false)
        val view = binding.root
        
        binding.imageViewHead.setOnClickListener{
            showArmorDialog(mainCharacterViewModel.equippedHead.value)
        }

        binding.imageViewChest.setOnClickListener{
            showArmorDialog(mainCharacterViewModel.equippedChest.value)
        }

        binding.imageViewLHand.setOnClickListener{
            showArmorDialog(mainCharacterViewModel.equippedChest.value, EquipmentTypes.LARM)
        }

        binding.imageViewRHand.setOnClickListener{
            showArmorDialog(mainCharacterViewModel.equippedChest.value, EquipmentTypes.RARM)
        }

        binding.imageViewRLeg.setOnClickListener{
            showArmorDialog(mainCharacterViewModel.equippedLegs.value, EquipmentTypes.RLEG)
        }

        binding.imageViewLLeg.setOnClickListener{
            showArmorDialog(mainCharacterViewModel.equippedLegs.value, EquipmentTypes.LLEG)
        }

        binding.imageViewSword.setOnClickListener{
            showWeaponDialog()
        }

        binding.buttonAddGear.setOnClickListener{
            Navigation.findNavController(view).navigate(R.id.action_equipmentFragment_to_addArmorFragment2)
        }

        binding.imageViewBelt.setOnClickListener{
            Navigation.findNavController(view).navigate(R.id.action_equipmentFragment_to_inventoryFragment)
        }

        return view
    }

    private fun showWeaponDialog() {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val bind : CustomDialogWeaponBinding = CustomDialogWeaponBinding.inflate(layoutInflater)
        bind.textView57.text = mainCharacterViewModel.equippedHead.value?.stoppingPower.toString()


        dialog.setContentView(bind.root)

        dialog.show()
    }

    /*Dialog to display armor information, armor type used to distinguish between armor types and display the correct information
    * For example, if left arm is clicked, the armorType parameter would be LARM and the left arm stopping power of the chest piece
    * will be displayed.
    */
    private fun showArmorDialog(armorItem: EquipmentItem?, armorType: EquipmentTypes? = null){
        val dialog = Dialog(requireContext())
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val bind : CustomDialogCharArmorBinding = CustomDialogCharArmorBinding.inflate(layoutInflater)

        //Check if there is an item equipped in this slot
        if (armorItem != null) {
            val stoppingPower = "Stopping Power (SP): " + armorItem.stoppingPower
            val availability = "Availability: " + armorItem.availability
            val armorEnhancement = "Armor Enhancement: " + armorItem.armorEnhancement
            val effect = "Effect: " + armorItem.effect
            val encumbValue = "Encumbrance Value: " + armorItem.encumbranceValue
            val weight = "Weight: " + armorItem.weight
            val price = "Cost: " + armorItem.cost + " Crowns"
            val type = armorItem.equipmentType

            bind.textViewTitle.text = armorItem.name
            bind.textViewSP.text = stoppingPower
            bind.textViewAvailability.text = availability
            bind.textViewAE.text = armorEnhancement
            bind.textViewEffect.text = effect
            bind.textViewEV.text = encumbValue
            bind.textViewWeight.text = weight
            bind.textViewCost.text = price

            when(armorType){
                EquipmentTypes.LARM -> bind.textCurrentSP.text = armorItem.currentLArmSP.toString()
                EquipmentTypes.RARM -> bind.textCurrentSP.text = armorItem.currentRArmSP.toString()
                EquipmentTypes.RLEG -> bind.textCurrentSP.text = armorItem.currentRLegSP.toString()
                EquipmentTypes.LLEG -> bind.textCurrentSP.text = armorItem.currentLLegSP.toString()

                else -> bind.textCurrentSP.text = armorItem.currentStoppingPower.toString()
            }


            bind.buttonEquipUnequip.text = "Unequip"
            bind.buttonRemove.visibility = View.GONE


            bind.buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            bind.buttonEquipUnequip.setOnClickListener {

                mainCharacterViewModel.unEquipItem(armorItem)
                Toast.makeText(
                    context,
                    "${mainCharacterViewModel.name.value} has unequipped ${armorItem.name}",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }

            bind.imageViewMinus.setOnClickListener{
                if (bind.textCurrentSP.text.toString().toInt() > 0){
                    bind.textCurrentSP.text = mainCharacterViewModel.onItemSPChange(armorItem, increase = false, armorType).toString()
                }
            }

            bind.imageViewPlus.setOnClickListener{
                if (bind.textCurrentSP.text.toString().toInt() < armorItem.stoppingPower){
                    bind.textCurrentSP.text = mainCharacterViewModel.onItemSPChange(armorItem, increase = true, armorType).toString()
                }
            }

            dialog.setContentView(bind.root)

            dialog.show()
        }
        else Toast.makeText(context, "No armor equipped in this slot.", Toast.LENGTH_SHORT).show()
    }


}