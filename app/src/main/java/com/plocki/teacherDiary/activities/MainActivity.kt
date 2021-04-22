package com.plocki.teacherDiary.activities

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.google.android.material.navigation.NavigationView
import com.plocki.teacherDiary.ApolloInstance
import com.plocki.teacherDiary.MyCalendarQuery
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.Store
import com.plocki.teacherDiary.fragments.CalendarFragment
import com.plocki.teacherDiary.fragments.DatabaseFragment
import com.plocki.teacherDiary.fragments.HomeFragment
import com.plocki.teacherDiary.fragments.SettingsFragment
import com.plocki.teacherDiary.model.SubjectEntry
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val subjectEntries = ArrayList<SubjectEntry>()
    private var mDrawer: DrawerLayout? = null
    private var toolbar: Toolbar? = null
    private var nvDrawer: NavigationView? = null
    private var drawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val store = Store()
        val teacherId = store.retrieve("teacherId")


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);

        mDrawer = findViewById(R.id.drawer_layout)
        nvDrawer = findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer!!)

        drawerToggle = setupDrawerToggle()
        drawerToggle!!.isDrawerIndicatorEnabled = true;
        drawerToggle!!.syncState();

        mDrawer!!.addDrawerListener(drawerToggle!!);

        val fragment = HomeFragment()
        val fragmentManager: FragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit()


        myCalendar(teacherId!!.toInt())
    }




    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggles
        drawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (drawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun setupDrawerToggle(): ActionBarDrawerToggle? {
        return ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }

    private fun selectDrawerItem(menuItem: MenuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        var fragment: Fragment? = null
        val fragmentClass: Class<*> = when (menuItem.itemId) {
            R.id.nav_home -> HomeFragment::class.java
            R.id.nav_calendar -> CalendarFragment::class.java
            R.id.nav_database -> DatabaseFragment::class.java
            R.id.nav_settings -> SettingsFragment::class.java
            else -> HomeFragment::class.java
        }
        try {
            fragment = fragmentClass.newInstance() as Fragment
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        val fragmentManager: FragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment!!).commit()

        menuItem.isChecked = true
        title = menuItem.title
        mDrawer!!.closeDrawers()
    }

    private fun myCalendar(teacherId: Int){

        val query = MyCalendarQuery(teacherId.toInput())

        GlobalScope.launch(Main) {
            try{
                val tmp  = ApolloInstance.get().query(query).toDeferred().await()
                try {
                    for(entry in tmp.data!!.sUBJECT_ENTRY){
                        subjectEntries.add(SubjectEntry(entry))
                    }
                    print("dupa")
                }catch (e: NullPointerException){
                    Toast.makeText(baseContext, "Błąd pobierania kalendarza",
                            Toast.LENGTH_SHORT).show()

                }
            }catch (e: Exception){
                Toast.makeText(baseContext, "Bład połączenia z serwerem",
                        Toast.LENGTH_SHORT).show()

            }
        }
    }

}