
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

fun main() {
    Editor().open()
}

class Editor{
    private var model = JsonObject(mutableMapOf())
    private val undoStack = mutableListOf<Command>()
    private val srcArea = JTextArea()
    val frame = JFrame("Projeto do Josue").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = GridLayout(0, 2)
        size = Dimension(600, 600)
        val left = JPanel()
        left.layout = GridLayout()
        val scrollPane = JScrollPane(leftPanel(model)).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        }
        srcArea.isEditable=false
        left.add(scrollPane)
        add(left)
        rightPanel(model)
        val right = JPanel()
        right.layout = GridLayout()
        srcArea.tabSize = 2
        right.add(srcArea)
        add(right)

    }
    fun open() {
        frame.isVisible = true
    }

    fun rightPanel(model: JsonObject){
        val json = getJsonFormat()
        model.accept(json)
        srcArea.text = model.toString()
    }

    fun leftPanel(model: JsonObject): JPanel =
        JPanel().apply {
            val tableView = TableView(model)
            val undoButton = button("Undo"){
                if(undoStack.isNotEmpty()) {
                    val last = undoStack.removeLast()
                    last.undo()
                    rightPanel(model)
                }
            }
            val removeAllButton = button("Remove All"){
                val command = RemoveAllCommand(model,tableView)
                undoStack.add(command)
                command.run()
                rightPanel(model)
            }
            alignmentY = undoButton.alignmentY
            add(undoButton)
            add(removeAllButton)
            model.addItem("uc",JsonString("PA"))
            model.addItem("ects",JsonString("6.0"))
            model.addItem("exam",JsonString("N/A"))
            rightPanel(model)
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT
            val spacing = 10
            border = EmptyBorder(spacing, spacing, spacing, spacing)
            val jsonObjectObserver = object : JsonObjectObserver {
                override fun elementAdded(s:String,js:JsonString){
                    val command = AddElementCommand(model,s, js, this, tableView)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun elementRemoved(s:String,js:JsonString,e:TableView.ElementComponent) {
                    val command = DeleteElementCommand(model,s, js, e, tableView)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun elementModified(s:String,js:JsonString,e: TableView.ElementComponent){
                    val command = UpdateElementCommand(model,s, js,tableView,e)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun arrayAdded(s:String,ja:JsonArray) {
                    val command = AddArrayCommand(model,s, ja, this, tableView)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun arrayRemoved(s:String,ja:JsonArray,a: TableView.ArrayComponent) {
                    val command = DeleteArrayCommand(model,s, ja, a, tableView)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun simpleElementAdded(s:String, js:JsonString,parent:TableView.ArrayComponent) {
                    val command = AddSimpleElementCommand(model, s, js,this, tableView,parent)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun simpleElementRemoved(parent: TableView.ArrayComponent, e: TableView.SimpleElementComponent) {
                    val command = DeleteSimpleElementCommand(model,e, tableView,parent)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun simpleElementModified(s:String, js:JsonString, newValue:JsonString,se:TableView.SimpleElementComponent) {
                    val command = UpdateSimpleElementCommand(model,s,js,newValue,tableView,se)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun arrayObjectAdded(s:String,parent:TableView.ArrayComponent) {
                    val command = AddArrayObjectCommand(model,s,this,tableView,parent)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun arrayObjectRemoved(s:String,parent:TableView.ArrayComponent,obj:JsonObject,o:TableView.ObjectComponent) {
                    val command = RemoveArrayObjectCommand(model,s,obj,o,tableView,parent)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun objectElementAdded(s:String,js:JsonString,o: TableView.ObjectComponent,array:String,obj:JsonObject){
                    val command = AddObjectElementCommand(model, s,js,array,obj,this,tableView,o)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun objectElementRemoved(s: String, js:JsonString,array:String, obj:JsonObject,parent: TableView.ObjectComponent, e: TableView.ObjectElementComponent){
                    val command = DeleteObjectElementCommand(model, s,js,array,obj,tableView,parent,e)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun objectElementModified(s:String,js:JsonString,array:String,obj:JsonObject,e: TableView.ObjectElementComponent){
                    val command = UpdateObjectElementCommand(model, s,js,array,obj,tableView,e)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun mapAdded(s: String, jo: JsonObject) {
                    val command = AddMapCommand(model,s, jo, this, tableView)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun mapRemoved(s: String, jo: JsonObject,mapComponent: TableView.MapComponent) {
                    val command = DeleteMapCommand(model,s, jo, mapComponent, tableView)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun mapElementAdded(jo:JsonObject,s:String, js:JsonString,parent:TableView.MapComponent) {
                    val command = AddMapElementCommand(s,js,this,tableView,jo,parent)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun mapElementRemoved(parent:TableView.MapComponent,e: TableView.ElementComponent,s:String,js:JsonString) {
                    val command = DeleteMapElementCommand(s,js,tableView,parent.jo,parent,e)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
                override fun mapElementModified(parent:TableView.MapComponent,s:String, js:JsonString,e: TableView.ElementComponent) {
                    val command = UpdateMapElementCommand(parent,s,js,tableView,e)
                    undoStack.add(command)
                    command.run()
                    rightPanel(model)
                }
            }
            model.properties.forEach {it ->
                if(it.value is JsonString) {
                    tableView.addElementComponent(it.key, it.value as JsonString,jsonObjectObserver)
                }
            }
            model.addObserver(jsonObjectObserver)
            add(tableView)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val menu = JPopupMenu("Options")
                        val add = JButton("Add Element")
                        add.addActionListener {
                            val lab = JOptionPane.showInputDialog("Label")
                            if( lab != "") {
                                jsonObjectObserver.elementAdded(lab, JsonString("N/A"))
                                menu.isVisible = false
                                revalidate()
                                frame.repaint()
                            }
                        }
                        val addArrayButton = JButton("Add Array")
                        addArrayButton.addActionListener {
                            val lab = JOptionPane.showInputDialog("Label")
                            if( lab != "") {
                                menu.isVisible = false
                                jsonObjectObserver.arrayAdded(lab, JsonArray(mutableListOf()))
                                revalidate()
                                frame.repaint()
                            }
                        }
                        val addMapButton = JButton("Add Object")
                        addMapButton.addActionListener {
                            val name = JOptionPane.showInputDialog("Name")
                            if( name != "") {
                                menu.isVisible = false
                                jsonObjectObserver.mapAdded(name, JsonObject(mutableMapOf()))
                                revalidate()
                                frame.repaint()
                            }
                        }
                        menu.add(add)
                        menu.add(addArrayButton)
                        menu.add(addMapButton)
                        menu.show(this@apply,100, 100)
                    }
                }
            })
        }
}

interface Command{
    fun run()
    fun undo()
}

 class AddElementCommand(private val model: JsonObject, private val s:String, private val js:JsonString, private val observer: JsonObjectObserver, private val tableView: TableView) : Command{
     private var e :TableView.ElementComponent? = null
     override fun run (){
         model.addItem(s,js)
         e = tableView.addElementComponent(s,js,observer)
     }
     override fun undo (){
         model.removeItem(s,js)
         e?.let { tableView.removeElementComponent(it) }
     }
 }


 class DeleteElementCommand(private val model: JsonObject, private val s:String, private val js:JsonString,private val e :TableView.ElementComponent, private val tableView: TableView) : Command{
     private val element = e
     override fun run (){
         model.removeItem(s,js)
         tableView.removeElementComponent(e)
     }
     override fun undo (){
         model.addItem(s,js)
         tableView.add(element)
         tableView.revalidate()
         tableView.repaint()
     }
 }
 class UpdateElementCommand(private val model: JsonObject, private val s:String, private val js:JsonString,private val tableView:TableView,private val e:TableView.ElementComponent) : Command {
     private val old = model.getItem(s)
     private val oldfield = e.second
     override fun run() {
         model.addItem(s, js)
     }
     override fun undo() {
         if (old != null) {
             model.addItem(s, old)
             if(e.second is JTextField) {
                 (e.second as JTextField).text = old.toString()
             }
             else if((old as JsonString).toString()!="true" && old.toString()!="True" && old.toString()!="false" && old.toString()!="False" && e.second is JCheckBox) {
                 (oldfield as JTextField).text = old.toString()
                 e.second = oldfield
             }
             else{
                 (e.second as JCheckBox).isSelected = !(e.second as JCheckBox).isSelected
             }
             tableView.revalidate()
             tableView.repaint()
         }
     }
 }

 class AddArrayCommand(private val model: JsonObject, private val s:String, private val ja: JsonArray, private val observer: JsonObjectObserver, private val tableView: TableView) : Command{
     private var a :TableView.ArrayComponent? = null
     override fun run (){
         model.addItem(s,ja)
         a = tableView.addArray(s,ja,observer)
     }
     override fun undo (){
         model.removeItem(s,ja)
         a?.let { tableView.removeArray(it) }
     }
 }


 class DeleteArrayCommand(private val model: JsonObject, private val s:String, private val ja:JsonArray,private val a :TableView.ArrayComponent, private val tableView: TableView) : Command{
     private val array = a
     override fun run (){
         model.removeItem(s,ja)
         tableView.removeArray(a)
     }
     override fun undo (){
         model.addItem(s,ja)
         tableView.add(array)
         tableView.revalidate()
         tableView.repaint()
     }
 }

 class AddSimpleElementCommand(private val model: JsonObject, private val s:String, private val js:JsonString, private val observer: JsonObjectObserver, private val tableView: TableView,private val parent:TableView.ArrayComponent) : Command{
     private var se :TableView.SimpleElementComponent? = null
     override fun run (){
         model.addArrayItem(s,js)
         se = tableView.addSimpleElement(parent,js,observer)
     }
     override fun undo (){
         model.removeArrayItem(s,js)
         se?.let { tableView.removeSimpleElement(parent,it) }
     }
 }

 class DeleteSimpleElementCommand(private val model: JsonObject,private val se :TableView.SimpleElementComponent, private val tableView: TableView,private val parent:TableView.ArrayComponent) : Command{
     private val simpleElement = se
     override fun run (){
         model.removeArrayItem(parent.s,se.textfield)
         tableView.removeSimpleElement(parent,se)
     }
     override fun undo (){
         model.addArrayItem(parent.s,se.textfield)
         parent.add(simpleElement)
         tableView.revalidate()
         tableView.repaint()
     }
 }

 class UpdateSimpleElementCommand(private val model: JsonObject, private val s:String, private var js:JsonString, private var newValue:JsonString,private val tableView:TableView,private val se:TableView.SimpleElementComponent) : Command {
     override fun run() {
         model.modifyArrayItem(s,js,newValue)
     }
     override fun undo() {
         model.modifyArrayItem(s,newValue,js)
         se.elementTextField.text=js.toString()
         println(se.elementTextField.text)
         tableView.revalidate()
         tableView.repaint()
     }
 }

 class AddArrayObjectCommand(private val model: JsonObject, private val s:String, private val observer: JsonObjectObserver, private val tableView: TableView,private val parent:TableView.ArrayComponent) : Command{
     private var o :TableView.ObjectComponent? = null
     private val obj = JsonObject(mutableMapOf<String, JsonValue>())
     override fun run (){
         model.addArrayItem(s,obj)
         o = tableView.addArrayObject(parent,s,observer,obj)
     }
     override fun undo (){
         model.removeArrayItem(s,obj)
         o?.let { tableView.removeArrayObject(parent, it) }
     }
 }

 class RemoveArrayObjectCommand(private val model: JsonObject, private val s:String, private val obj:JsonObject,private val o:TableView.ObjectComponent, private val tableView: TableView,private val parent:TableView.ArrayComponent) : Command{
     private val arrayobject = o
     override fun run (){
         model.removeArrayItem(s,obj)
         tableView.removeArrayObject(parent,o)
     }
     override fun undo (){
         model.addArrayItem(s,obj)
         parent.add(arrayobject)
         tableView.revalidate()
         tableView.repaint()
     }
 }

 class AddObjectElementCommand(private val model: JsonObject, private val s:String, private val js:JsonString, private val array:String, private val obj:JsonObject, private val observer: JsonObjectObserver, private val tableView: TableView,private val o: TableView.ObjectComponent) : Command{
     private var oe :TableView.ObjectElementComponent? = null
     override fun run (){
         model.addElementToObject(s,js,array,obj)
         oe = tableView.addObjectElement(o,s,js,observer)
     }
     override fun undo (){
         model.removeElementToObject(s,js,array,obj)
         oe?.let { tableView.removeObjectElement(o,it) }
     }
 }

 class DeleteObjectElementCommand(private val model: JsonObject, private val s:String, private val js:JsonString, private val array:String,  private val obj:JsonObject,private val tableView: TableView,private val o: TableView.ObjectComponent, private val e: TableView.ObjectElementComponent) : Command{
     private val objectElement = e
     override fun run (){
         model.removeElementToObject(s,js,array,obj)
         tableView.removeObjectElement(o,e)
     }
     override fun undo (){
         model.addElementToObject(s,js,array,obj)
         o.objectpanel.add(objectElement)
         tableView.revalidate()
         tableView.repaint()
     }
 }

 class UpdateObjectElementCommand(private val model: JsonObject, private val s:String, private val js:JsonString, private val array:String,  private val obj:JsonObject,private val tableView: TableView, private val e: TableView.ObjectElementComponent) : Command {
     private val old = ((model.getItem(array) as JsonArray).getItem(obj) as JsonObject).properties[s]
     private val oldfield = e.second
     override fun run() {
         model.addElementToObject(s,js,array,obj)
     }
     override fun undo() {
         if (old != null) {
             model.addElementToObject(s,old,array,obj)
             if(e.second is JTextField) {
                 (e.second as JTextField).text = old.toString()
             }
             else if((old as JsonString).toString()!="true" && old.toString()!="True" && old.toString()!="false" && old.toString()!="False" && e.second is JCheckBox) {
                 (oldfield as JTextField).text = old.toString()
                 e.second = oldfield
             }
             else{
                 (e.second as JCheckBox).isSelected = !(e.second as JCheckBox).isSelected
             }
             tableView.revalidate()
             tableView.repaint()
         }
     }
 }

 class AddMapCommand(private val model: JsonObject, private val s:String, private val jo: JsonObject, private val observer: JsonObjectObserver, private val tableView: TableView) : Command{
     private var m :TableView.MapComponent? = null
     override fun run (){
         model.addItem(s,jo)
         m = tableView.addMap(s,jo,observer)
     }
     override fun undo (){
         model.removeItem(s,jo)
         m?.let { tableView.removeMap(it) }
     }
 }

 class DeleteMapCommand(private val model: JsonObject, private val s:String, private val jo:JsonObject, private val m :TableView.MapComponent, private val tableView: TableView) : Command{
     private val map = m
     override fun run (){
         model.removeItem(s,jo)
         tableView.removeMap(m)
     }
     override fun undo (){
         model.addItem(s,jo)
         tableView.add(map)
         tableView.revalidate()
         tableView.repaint()
     }
 }

 class AddMapElementCommand(private val s:String, private val js:JsonString, private val observer: JsonObjectObserver, private val tableView: TableView,private val jo:JsonObject,private val parent:TableView.MapComponent) : Command{
     private var e :TableView.ElementComponent? = null
     override fun run (){
         jo.addItem(s,js)
         e = tableView.addMapElement(parent,s,js,observer)
     }
     override fun undo (){
         jo.removeItem(s,js)
         e?.let { tableView.removeMapElement(parent,it) }
     }
 }

 class DeleteMapElementCommand(private val s:String, private val js:JsonString, private val tableView: TableView,private val jo:JsonObject,private val parent:TableView.MapComponent, private val e: TableView.ElementComponent) : Command{
     private val element = e
     override fun run (){
         jo.removeItem(s,js)
         tableView.removeMapElement(parent,e)
     }
     override fun undo (){
         jo.addItem(s,js)
         parent.add(element)
         tableView.revalidate()
         tableView.repaint()
     }
 }
 class UpdateMapElementCommand(private val parent:TableView.MapComponent, private val s:String, private val js:JsonString,private val tableView:TableView,private val e:TableView.ElementComponent) : Command {
     private val old = parent.jo.properties[s]
     override fun run() {
         parent.jo.addItem(s,js)
     }
     override fun undo() {
         if (old != null) {
             parent.jo.addItem(s, old)
             (e.second as JTextField).text=old.toString()
             tableView.revalidate()
             tableView.repaint()
         }
     }
 }

 class RemoveAllCommand(private var model: JsonObject,private var tableView: TableView) : Command {
    private val oldModel = model.properties.toMutableMap()
    private val oldTableView : MutableList<Component> = mutableListOf()
    override fun run() {
        model.properties.clear()
        tableView.components.forEach {component ->
            oldTableView.add(component)
        }
        tableView.clear()
    }

    override fun undo() {
        oldModel.forEach{it->
            model.addItem(it.key,it.value)
        }
        oldTableView.forEach { component ->
            tableView.add(component)
        }
        tableView.revalidate()
        tableView.repaint()
    }
 }





