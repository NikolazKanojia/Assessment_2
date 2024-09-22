package expendable

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.Repcard_task222.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.imageview.ShapeableImageView

class ThreeLevelExpandableAdapter(
    private val context: Context,
    private val parentList: List<ParentItem>
) : BaseExpandableListAdapter() {
    private val expandedChildPositions = mutableMapOf<Int, Boolean>() // Track expanded child states

    override fun getGroup(groupPosition: Int): ParentItem {
        return parentList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): ChildItem {
        return parentList[groupPosition].children[childPosition]
    }

    override fun getGroupCount(): Int {
        return parentList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return parentList[groupPosition].children.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val groupItem = getGroup(groupPosition)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.parent_item, parent, false)

        val tvGroupName: TextView = view.findViewById(R.id.tvGroupName)
        val cbGroupSelect: CheckBox = view.findViewById(R.id.cbGroupSelect)
        val imgArrow: ImageView = view.findViewById(R.id.imgArrow)
        tvGroupName.text = groupItem.name
        cbGroupSelect.isChecked = groupItem.isSelected

        // Handle arrow direction based on expanded state
        if (isExpanded) {
            imgArrow.setImageResource(R.drawable.ic_arrow_up) // Arrow pointing up when expanded
        } else {
            imgArrow.setImageResource(R.drawable.ic_arrow_down) // Arrow pointing down when collapsed
        }

        cbGroupSelect.setOnCheckedChangeListener { _, isChecked ->
            groupItem.isSelected = isChecked
            toggleChildren(groupPosition, isChecked)
            notifyDataSetChanged()
        }

        return view
    }

    @SuppressLint("SetTextI18n")
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val childItem = getChild(groupPosition, childPosition)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.child_item_with_grandchildren, parent, false)

        val tvChildName: TextView = view.findViewById(R.id.tvChildName)
        val cbChildSelect: CheckBox = view.findViewById(R.id.cbChildSelect)
        val containerGrandChildren: LinearLayout = view.findViewById(R.id.containerGrandChildren)
        val imgArrow: ImageView = view.findViewById(R.id.imgArrow)

        val sizeeee=childItem.grandChildren.size
        Log.e("Android","Android$"+sizeeee)
        tvChildName.text = childItem.name+"  ("+sizeeee+")"
        cbChildSelect.isChecked = childItem.isSelected
        val isChildExpanded = expandedChildPositions[childPosition + groupPosition * 100] ?: false // Unique key for child


        imgArrow.setImageResource(if (isChildExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down)
        containerGrandChildren.visibility = if (isChildExpanded) View.VISIBLE else View.GONE

        imgArrow.setOnClickListener {
            expandedChildPositions[childPosition + groupPosition * 100] = !isChildExpanded // Toggle the state
            notifyDataSetChanged() // Refresh to show/hide grandchildren
        }

        cbChildSelect.setOnCheckedChangeListener { _, isChecked ->
            childItem.isSelected = isChecked
            toggleGrandChildren(groupPosition, childPosition, isChecked)
            updateParentState(groupPosition)
            notifyDataSetChanged()
        }

        containerGrandChildren.removeAllViews()
        childItem.grandChildren.forEachIndexed { grandChildPosition, grandChildItem ->
            val grandChildView = LayoutInflater.from(context).inflate(R.layout.grandchild_item, containerGrandChildren, false)
            val tvGrandChildName = grandChildView.findViewById<TextView>(R.id.tvGrandChildName)
            val cbGrandChildSelect = grandChildView.findViewById<CheckBox>(R.id.cbSelect)
            val ivGrandChildImage=grandChildView.findViewById<ShapeableImageView>(R.id.ivGrandchildImage)
            val ivGrandChildJob=grandChildView.findViewById<TextView>(R.id.tvGrandChildJob)

            tvGrandChildName.text = grandChildItem.name
            cbGrandChildSelect.isChecked = grandChildItem.isSelected
            ivGrandChildJob.text = grandChildItem.job

            Log.e("Android :","hahaha ${grandChildItem.image}")
            Glide.with(context)
                .load(grandChildItem.image)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.dog)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivGrandChildImage)

            cbGrandChildSelect.setOnCheckedChangeListener { _, isChecked ->
                grandChildItem.isSelected = isChecked
                updateChildState(groupPosition, childPosition)
                updateParentState(groupPosition)
                notifyDataSetChanged()
            }

            containerGrandChildren.addView(grandChildView)
        }

        return view
    }

    // Toggle all children and grandchildren when parent is selected/unselected
    private fun toggleChildren(groupPosition: Int, isChecked: Boolean) {
        val children = parentList[groupPosition].children
        children.forEach { child ->
            child.isSelected = isChecked
            toggleGrandChildren(groupPosition, children.indexOf(child), isChecked)
        }
    }

    // Toggle all grandchildren when a child is selected/unselected
    private fun toggleGrandChildren(groupPosition: Int, childPosition: Int, isChecked: Boolean) {
        val grandChildren = getChild(groupPosition, childPosition).grandChildren
        grandChildren.forEach { it.isSelected = isChecked }
    }

    // Update parent selection state based on child states
    private fun updateParentState(groupPosition: Int) {
        val parentItem = getGroup(groupPosition)
        val allChildrenSelected = parentItem.children.all { child ->
            child.isSelected && child.grandChildren.all { it.isSelected }
        }
        parentItem.isSelected = allChildrenSelected
    }

    // Update child selection state based on grandchild states
    private fun updateChildState(groupPosition: Int, childPosition: Int) {
        val childItem = getChild(groupPosition, childPosition)
        val allGrandChildrenSelected = childItem.grandChildren.all { it.isSelected }
        childItem.isSelected = allGrandChildrenSelected
    }

}