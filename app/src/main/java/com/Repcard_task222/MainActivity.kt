package com.Repcard_task222

import android.os.Bundle
import android.widget.ExpandableListView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import expendable.ChildItem
import expendable.GrandChildItem
import expendable.ParentItem
import expendable.ThreeLevelExpandableAdapter
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var expandableListView: ExpandableListView
    private lateinit var adapter: ThreeLevelExpandableAdapter
    private val expandedGroupPositions = HashSet<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        expandableListView = findViewById(R.id.expandableListView)
        adapter = ThreeLevelExpandableAdapter(this, generateData())
        expandableListView.setAdapter(adapter)

        expandableListView.setOnGroupExpandListener { groupPosition ->
            // Update arrow and track expanded group
            expandedGroupPositions.add(groupPosition)
            updateGroupView(groupPosition, true)
        }

        expandableListView.setOnGroupCollapseListener { groupPosition ->
            // Update arrow and remove from expanded group set
            expandedGroupPositions.remove(groupPosition)
            updateGroupView(groupPosition, false)
        }
    }

    private fun updateGroupView(groupPosition: Int, isExpanded: Boolean) {
        val groupView = expandableListView.getChildAt(groupPosition)
        val imgArrow: ImageView? = groupView?.findViewById(R.id.imgArrow)

        if (imgArrow != null) {
            imgArrow.setImageResource(if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down)
        }
    }

    private fun generateData(): List<ParentItem> {
        val json = loadJSONFromAsset("Heirarchy.json")
        val gson = Gson()
        val officeListType = object : TypeToken<List<Office>>() {}.type
        val offices: List<Office> = gson.fromJson(json, officeListType)

        return convertOfficesToItems(offices)
    }

    private fun loadJSONFromAsset(filename: String): String {
        val inputStream: InputStream = assets.open(filename)
        return inputStream.bufferedReader().use { it.readText() }
    }

    private fun convertOfficesToItems(offices: List<Office>): List<ParentItem> {
        return offices.map { office ->
            val children = office.items.map { team ->
                val grandChildren = team.items.map { user ->
                    GrandChildItem(user.title,user.type, user.image,false) // Assuming false for initial selection
                }
                ChildItem(team.title, false, grandChildren)
            }
            ParentItem(office.title, false, children)
        }
    }
}

data class Office(
    val type: String,
    val image: String,
    val items: List<Team>,
    val id: Int,
    val title: String
)

data class Team(
    val id: Int,
    val type: String,
    val title: String,
    val items: List<User>
)

data class User(
    val type: String,
    val image: String,
    val title: String,
    val id: Int
)