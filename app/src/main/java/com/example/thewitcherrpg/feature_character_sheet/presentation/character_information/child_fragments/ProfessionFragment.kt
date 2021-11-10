package com.example.thewitcherrpg.feature_character_sheet.presentation.character_information.child_fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.thewitcherrpg.R
import com.example.thewitcherrpg.core.presentation.MainCharacterViewModel
import com.example.thewitcherrpg.databinding.CustomDialogHelpInfoBinding
import com.example.thewitcherrpg.databinding.FragmentProfessionBinding

class ProfessionFragment : Fragment() {
    private var _binding: FragmentProfessionBinding? = null
    private val binding get() = _binding!!

    private val mainCharacterViewModel: MainCharacterViewModel by activityViewModels()

    lateinit var defSkill: String
    lateinit var race: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfessionBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.lifecycleOwner = this
        binding.sharedViewModel = mainCharacterViewModel

        onInit()

        return view
    }

    private fun onInit(){

        defSkill = mainCharacterViewModel.definingSkill.value

        binding.textViewProfessionTitle.text = mainCharacterViewModel.profession.value.toString()

        val definingSkill = "Defining Skill: $defSkill"
        binding.textDefiningSkill.text = definingSkill

        binding.textDefiningSkill.setOnClickListener{
            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)

            alertDialogBuilder.setTitle(defSkill)

            // set dialog message
            alertDialogBuilder.setMessage(mainCharacterViewModel.definingSkillInfo.value)

            // create alert dialog
            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)

            // show it
            alertDialog.show()

        }

        binding.buttonHelp.setOnClickListener(){
            showDialogHelp()
        }
    }

    private fun showDialogHelp() {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.custom_dialog_help_info)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val bind : CustomDialogHelpInfoBinding = CustomDialogHelpInfoBinding.inflate(layoutInflater)
        dialog.setContentView(bind.root)

        bind.textViewInfo.text = resources.getString(R.string.profession_help)
        bind.textViewTitle.text = "Your Profession"

        dialog.show()
    }

}