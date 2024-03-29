package com.witcher.thewitcherrpg.feature_character_creation.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.witcher.thewitcherrpg.R
import com.witcher.thewitcherrpg.databinding.FragmentCharCreationThirdBinding
import com.witcher.thewitcherrpg.feature_character_sheet.presentation.skills.SkillsExpandableFragment

class CharCreationThirdFrag : Fragment() {
    private var _binding: FragmentCharCreationThirdBinding? = null
    private val binding get() = _binding!!

    private val fragment = SkillsExpandableFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCharCreationThirdBinding.inflate(inflater, container, false)
        val view = binding.root

        //Check if fragment exists in case user tries to return to this fragment after pressing back button
        if(childFragmentManager.findFragmentByTag("frag") == null){
            childFragmentManager.beginTransaction().add(R.id.fragmentContainerView, fragment, "frag")
                .commitNow()
        }

        binding.customTitle.setTitle("Profession Abilities")
        binding.customTitle.setTitleSize(20F)

        binding.buttonFinish.setOnClickListener{
            (activity as CharCreationActivity?)?.onSaveFinal()
        }
        binding.buttonToSecFrag.setOnClickListener{
            Navigation.findNavController(view).navigateUp()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}