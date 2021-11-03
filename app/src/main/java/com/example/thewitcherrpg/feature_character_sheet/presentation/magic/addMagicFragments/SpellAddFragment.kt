package com.example.thewitcherrpg.feature_character_sheet.presentation.magic.addMagicFragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thewitcherrpg.R
import com.example.thewitcherrpg.feature_character_sheet.SharedViewModel
import com.example.thewitcherrpg.feature_character_sheet.presentation.magic.spellListAdapters.JourneymanSpellListAdapter
import com.example.thewitcherrpg.feature_character_sheet.presentation.magic.spellListAdapters.MasterSpellListAdapter
import com.example.thewitcherrpg.feature_character_sheet.presentation.magic.spellListAdapters.NoviceSpellListAdapter
import com.example.thewitcherrpg.databinding.CustomDialogAddSpellBinding
import com.example.thewitcherrpg.databinding.FragmentSpellAddBinding

class SpellAddFragment : Fragment() {
    private var _binding: FragmentSpellAddBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpellAddBinding.inflate(inflater, container, false)
        val view = binding.root

        //Adds a callback to back button to return to previous fragment in nav graph instead of destroying activity
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back button event
            Navigation.findNavController(view).navigate(R.id.action_spellAddFragment_to_charMagicFragment)
        }
        callback.isEnabled = true

        listAdaptersInit()

        return view
    }

    private fun listAdaptersInit(){

        //Receive information from recyclerView adapter
        val noviceAdapter = NoviceSpellListAdapter(requireContext()){
            spell -> showSpellDialog(spell, SpellLevel.NOVICE) //Determines which list this spell goes under
        }
        noviceAdapter.setData(resources.getStringArray(R.array.novice_spells_list_data).toList())
        noviceAdapter.setAddSpell(true) //Sets add spell state to true to show all spells

        val journeymanAdapter = JourneymanSpellListAdapter(requireContext()){
                spell -> showSpellDialog(spell, SpellLevel.JOURNEYMAN)
        }
        journeymanAdapter.setData(resources.getStringArray(R.array.journeyman_spells_list_data).toList())
        journeymanAdapter.setAddSpell(true) //Sets add spell state to true to show all spells

        val masterAdapter = MasterSpellListAdapter(requireContext()){
                spell -> showSpellDialog(spell, SpellLevel.MASTER)
        }
        masterAdapter.setData(resources.getStringArray(R.array.master_spells_list_data).toList())
        masterAdapter.setAddSpell(true) //Sets add spell state to true to show all spells

        binding.recyclerViewNovice.adapter = noviceAdapter
        binding.recyclerViewNovice.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerViewJourneyman.adapter = journeymanAdapter
        binding.recyclerViewJourneyman.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerViewMaster.adapter = masterAdapter
        binding.recyclerViewMaster.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerViewNovice.isNestedScrollingEnabled = false
        binding.recyclerViewJourneyman.isNestedScrollingEnabled = false
        binding.recyclerViewMaster.isNestedScrollingEnabled = false

    }

    private fun showSpellDialog(spell: String?, level: SpellLevel) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val bind : CustomDialogAddSpellBinding = CustomDialogAddSpellBinding.inflate(layoutInflater)
        dialog.setContentView(bind.root)

        val pair = spell!!.split(":").toTypedArray()
        val spellName = pair[0]
        val staCost = "<b>" + "STA Cost: " + "</b>" + pair[1]
        val effect = "<b>" + "Effect: " + "</b>" + pair[2]
        val range = "<b>" + "Range: " + "</b>" + pair[3]
        val duration = "<b>" + "Duration: " + "</b>" + pair[4]
        val defense = "<b>" + "Defense: " + "</b>" + pair[5]
        val element = pair[6]

        bind.addSpellNameText.text = spellName
        bind.addStaCostText.text = HtmlCompat.fromHtml(staCost, HtmlCompat.FROM_HTML_MODE_LEGACY)
        bind.addRangeText.text = HtmlCompat.fromHtml(range, HtmlCompat.FROM_HTML_MODE_LEGACY)
        bind.addDefenseText.text = HtmlCompat.fromHtml(defense, HtmlCompat.FROM_HTML_MODE_LEGACY)
        bind.addEffectText.text = HtmlCompat.fromHtml(effect, HtmlCompat.FROM_HTML_MODE_LEGACY)
        bind.addDurationText.text = HtmlCompat.fromHtml(duration, HtmlCompat.FROM_HTML_MODE_LEGACY)
        bind.addSpellElementText.text = element

        //Check spell level to add it to correct character spell list
        bind.addSpellbutton.setOnClickListener(){
            when (level){
                /*SpellLevel.NOVICE -> {
                    if (sharedViewModel.addNoviceSpell(spellName))
                        Toast.makeText(context, "$spellName added to ${sharedViewModel.name.value}", Toast.LENGTH_SHORT).show()

                    else Toast.makeText(context, "${sharedViewModel.name.value} already knows $spellName", Toast.LENGTH_SHORT).show()

                }
                SpellLevel.JOURNEYMAN -> {
                    if (sharedViewModel.addJourneymanSpell(spellName))
                        Toast.makeText(context, "$spellName added to ${sharedViewModel.name.value}", Toast.LENGTH_SHORT).show()

                    else Toast.makeText(context, "${sharedViewModel.name.value} already knows $spellName", Toast.LENGTH_SHORT).show()
                }
                SpellLevel.MASTER -> {
                    if (sharedViewModel.addMasterSpell(spellName))
                        Toast.makeText(context, "$spellName added to ${sharedViewModel.name.value}", Toast.LENGTH_SHORT).show()

                    else Toast.makeText(context, "${sharedViewModel.name.value} already knows $spellName", Toast.LENGTH_SHORT).show()
                }*/
            }

            dialog.dismiss()
        }

        bind.addSpellCancelButton.setOnClickListener(){
            dialog.dismiss()
        }

        dialog.show()
    }

    //Enum class to determine spell level
    enum class SpellLevel {
        NOVICE,
        JOURNEYMAN,
        MASTER
    }

}