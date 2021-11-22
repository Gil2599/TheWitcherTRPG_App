package com.example.thewitcherrpg.feature_character_sheet.presentation

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.thewitcherrpg.R
import com.example.thewitcherrpg.core.presentation.MainCharacterViewModel
import com.example.thewitcherrpg.databinding.ActivityMainBinding
import com.example.thewitcherrpg.feature_character_sheet.presentation.character_information.CharFragment
import com.example.thewitcherrpg.feature_character_sheet.presentation.equipment.EquipmentParentFragment
import com.example.thewitcherrpg.feature_character_sheet.presentation.magic.MagicParentFragment
import com.example.thewitcherrpg.feature_character_sheet.presentation.profession_skill_tree.ProfessionSkillTree
import com.example.thewitcherrpg.feature_character_sheet.presentation.skills.SkillsFragment
import com.example.thewitcherrpg.feature_character_sheet.presentation.stats.StatsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import android.app.Activity
import android.graphics.BitmapFactory
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.thewitcherrpg.databinding.NavHeaderBinding
import com.example.thewitcherrpg.feature_character_creation.presentation.CharCreationFirstFrag
import com.google.android.material.snackbar.Snackbar
import java.io.FileInputStream
import java.io.FileNotFoundException


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle
    lateinit var binding: ActivityMainBinding
    private var characterId: Int = -1

    private val mainCharacterViewModel: MainCharacterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        characterId = intent.getIntExtra("CHARACTER_ID", -1) //Get character id

        try {
            mainCharacterViewModel.getCharacter(characterId) //Initialize viewModel with character data
        } catch (ex: KotlinNullPointerException){
            Toast.makeText(this, "Unexpected error has occurred", Toast.LENGTH_SHORT).show()
        }

        //Set up navigation drawer
        val drawerLayout = binding.drawerLayout

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val fragmentManager = supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragmentContainerView3, CharFragment())
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()

        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.Character -> {
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainerView3, CharFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit() }
                R.id.Skills -> {
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainerView3, SkillsFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit() }
                R.id.Stats -> {
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainerView3, StatsFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
                }
                R.id.ProfessionSkillTree -> {
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainerView3, ProfessionSkillTree())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
                }
                R.id.Magic -> {
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainerView3, MagicParentFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
                }
                R.id.Equipment -> {
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainerView3, EquipmentParentFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
                }
            }
            drawerLayout.closeDrawer(binding.navView)
            true
        }

        val navHeaderBinding: NavHeaderBinding = DataBindingUtil.inflate(layoutInflater, R.layout.nav_header, binding.navView, true)
        navHeaderBinding.mainViewModel = mainCharacterViewModel
        navHeaderBinding.lifecycleOwner = this

        val imageObserver = Observer<String> { newImagePath ->
            if (newImagePath.isNotEmpty()){
                //Make margins 0 if image is found
                (navHeaderBinding.imageView.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0,0,0,0)
                //Load Image into imageview
                loadImageFromStorage(newImagePath, navHeaderBinding.imageView)
            }
        }

        mainCharacterViewModel.image.observe(this, imageObserver)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Repeat when the lifecycle is STARTED, cancel when PAUSED
                launch {
                    mainCharacterViewModel.addState.collectLatest {
                        if (it.success) {
                            Toast.makeText(this@MainActivity,
                                "Saved Successfully!",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.menu_delete){
            deleteCharacter()
        }
        if(item.itemId == R.id.menu_save){
            saveCharacter()
        }
        if(item.itemId == R.id.menu_long_rest){
            mainCharacterViewModel.onLongRest()
            Snackbar.make(
                binding.root, "${mainCharacterViewModel.name.value} has long rested.",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        if (item.itemId == R.id.menu_edit_character){
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.fragmentContainerView3, CharCreationFirstFrag())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
        }
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteCharacter() {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton("Yes"){_, _ ->
            mainCharacterViewModel.deleteCharacter(characterId)

            //Deletes the character image associated with this character
            val path = mainCharacterViewModel.image.value.toString()
            val f = File(path, mainCharacterViewModel.id.value.toString() + ".jpeg")
            f.delete()

            Toast.makeText(this, "Character Deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
        builder.setNegativeButton("No"){_, _ -> }
        builder.setTitle("Delete character?")
        builder.create().show()
    }

    private fun saveCharacter(){
        mainCharacterViewModel.saveCharacter()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.delete_save_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        //Check if there is a callback set by a fragment then don't close activity
        if (this.onBackPressedDispatcher.hasEnabledCallbacks()){
            super.onBackPressed()
            return
        }

        //If no callbacks are set by fragments, ask user if they would like to save character or cancel
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton("Yes"){_, _ ->
            saveCharacter()
            finish()
            }
        builder.setNegativeButton("No"){_, _ -> finish()}

        builder.setNeutralButton("Cancel"){_, _ -> }

        builder.setTitle("Save changes to character?")
        builder.setMessage("All unsaved changes will be lost.")
        builder.create().show()

    }

    private fun loadImageFromStorage(path: String, img: ImageView) {
        try {
            val f = File(path, mainCharacterViewModel.id.value.toString() + ".jpeg")
            val b = BitmapFactory.decodeStream(FileInputStream(f))
            img.setImageBitmap(b)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

}
