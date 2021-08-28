package com.example.thewitcherrpg.characterCreation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.thewitcherrpg.R
import com.example.thewitcherrpg.databinding.FragmentCharCreationSecBinding
import com.example.thewitcherrpg.characterSheet.SharedViewModel
import com.example.thewitcherrpg.characterSheet.SkillsFragment

class CharCreationSecFrag : Fragment() {
    private var _bindind: FragmentCharCreationSecBinding? = null
    private val binding get() = _bindind!!

    private val fragment = SkillsFragment()

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _bindind = FragmentCharCreationSecBinding.inflate(inflater, container, false)
        val view = binding.root

        if(childFragmentManager.findFragmentByTag("frag") == null){
            childFragmentManager.beginTransaction().add(R.id.fragmentContainerView, fragment, "frag")
                .commitNow()
        }

        binding.buttonToThirdFrag.setOnClickListener(){
            Navigation.findNavController(view).navigate(R.id.action_charCreation_secFrag_to_charCreation_thirdFrag2)
        }

        return view
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _bindind = null
    }


}