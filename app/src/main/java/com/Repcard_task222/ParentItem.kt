package expendable

data class ParentItem(
    val name: String,
    var isSelected: Boolean,
    val children: List<ChildItem>
)

data class ChildItem(
    val name: String,
    var isSelected: Boolean,
    val grandChildren: List<GrandChildItem>
)

data class GrandChildItem(
    val name: String,
    val job:String,
    val image:String,
    var isSelected: Boolean
)