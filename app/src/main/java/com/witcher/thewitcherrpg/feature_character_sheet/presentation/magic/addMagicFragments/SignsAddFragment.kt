package com.witcher.thewitcherrpg.feature_character_sheet.presentation.magic.addMagicFragments

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.witcher.thewitcherrpg.R
import com.witcher.thewitcherrpg.core.presentation.MainCharacterViewModel
import com.witcher.thewitcherrpg.databinding.CustomDialogAddSpellBinding
import com.witcher.thewitcherrpg.databinding.FragmentSignsAddBinding
import com.witcher.thewitcherrpg.feature_character_sheet.domain.models.MagicItem
import com.witcher.thewitcherrpg.feature_character_sheet.presentation.magic.spellListAdapter.MagicListAdapter
import com.google.android.material.snackbar.Snackbar

class SignsAddFragment : Fragment() {
    private var _binding: FragmentSignsAddBinding? = null
    private val binding get() = _binding!!

    private val mainCharacterViewModel: MainCharacterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignsAddBinding.inflate(inflater, container, false)
        val view = binding.root

        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle the back button event
            Navigation.findNavController(view)
                .navigate(R.id.action_signsAddFragment_to_charMagicFragment)
        }
        callback.isEnabled = true

        listAdaptersInit()

        return view
    }

    private fun listAdaptersInit() {

        //Receive information from recyclerView adapter
        val basicAdapter = MagicListAdapter { sign ->
            showSignDialog(sign) //Determines which list this spell goes under
        }
        basicAdapter.setData(mainCharacterViewModel.getMagicList(R.array.basic_signs_list_data))

        val alternateAdapter = MagicListAdapter { spell ->
            showSignDialog(spell)
        }
        alternateAdapter.setData(mainCharacterViewModel.getMagicList(R.array.alternate_signs_list_data))

        binding.recyclerViewBasic.adapter = basicAdapter
        binding.recyclerViewBasic.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerViewAlternate.adapter = alternateAdapter
        binding.recyclerViewAlternate.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerViewBasic.isNestedScrollingEnabled = false
        binding.recyclerViewAlternate.isNestedScrollingEnabled = false

    }

    private fun showSignDialog(item: MagicItem) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val bind: CustomDialogAddSpellBinding = CustomDialogAddSpellBinding.inflate(layoutInflater)
        dialog.setContentView(bind.root)

        val staCost = "<b>" + "STA Cost: " + "</b>" + item.staminaCost
        val effect = "<b>" + "Effect: " + "</b>" + item.description
        val range = "<b>" + "Range: " + "</b>" + item.range
        val duration = "<b>" + "Duration: " + "</b>" + item.duration
        val defense = "<b>" + "Defense: " + "</b>" + item.defense
        val element = item.element

        bind.customTitle.setTitle(item.name)
        bind.customTitle.setTitleSize(18F)
        bind.addStaCostText.text = HtmlCompat.fromHtml(staCost, HtmlCompat.FROM_HTML_MODE_LEGACY)
        bind.addRangeText.text = HtmlCompat.fromHtml(range, HtmlCompat.FROM_HTML_MODE_LEGACY)
        bind.addDefenseText.text = HtmlCompat.fromHtml(defense, HtmlCompat.FROM_HTML_MODE_LEGACY)
        bind.addEffectText.text = HtmlCompat.fromHtml(effect, HtmlCompat.FROM_HTML_MODE_LEGACY)
        bind.addEffectText.typeface = Typeface.DEFAULT
        bind.addDurationText.text = HtmlCompat.fromHtml(duration, HtmlCompat.FROM_HTML_MODE_LEGACY)
        bind.addSpellElementText.text = element

        //Check spell level to add it to correct character spell list
        bind.addSpellbutton.setOnClickListener {
            mainCharacterViewModel.addMagicItem(item)
            Snackbar.make(
                binding.root, "${item.name} added to ${mainCharacterViewModel.name.value}",
                Snackbar.LENGTH_SHORT
            ).show()
            dialog.dismiss()
        }

        bind.addSpellCancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}