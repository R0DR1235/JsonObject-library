import java.awt.*
import java.awt.event.*
import javax.swing.*

class TableView(val model: JsonObject) : JPanel() {
    init {
        layout = GridLayout(0,1)
    }
    fun clear(){
        removeAll()
        revalidate()
        repaint()
    }
    fun addElementComponent(s: String, js: JsonString,observer: JsonObjectObserver):TableView.ElementComponent {
        val e = ElementComponent(s,js,observer)
        add(e)
        revalidate()
        repaint()
        return e
    }
    fun removeElementComponent(e:ElementComponent) {
        val find = components.find { it is ElementComponent && it.matches(e) }
        find?.let {
            remove(find)
        }
        revalidate()
        repaint()
    }

    fun addArray(s: String, ja: JsonArray, observer: JsonObjectObserver) :TableView.ArrayComponent{
        val a = ArrayComponent(s,ja,observer)
        add(a)
        revalidate()
        repaint()
        return a
    }
    fun removeArray(a:ArrayComponent) {
        remove(a)
        revalidate()
        repaint()
    }
    fun addSimpleElement(parent:ArrayComponent,s:JsonString,observer: JsonObjectObserver):TableView.SimpleElementComponent{
        val s = SimpleElementComponent(s,observer)
        parent.add(s)
        revalidate()
        repaint()
        return s
    }
    fun removeSimpleElement(parent:ArrayComponent, e:SimpleElementComponent){
        parent.remove(e)
        revalidate()
        repaint()
    }
    fun addArrayObject(parent:ArrayComponent,s:String,observer: JsonObjectObserver,obj:JsonObject):TableView.ObjectComponent{
        val o = ObjectComponent(s,observer,obj)
        parent.add(o)
        revalidate()
        repaint()
        return o
    }
    fun removeArrayObject(parent:ArrayComponent,o:ObjectComponent){
        parent.remove(o)
        revalidate()
        repaint()
    }
    fun addObjectElement(parent:ObjectComponent,s:String,js:JsonString,observer: JsonObjectObserver):TableView.ObjectElementComponent{
        val oe = ObjectElementComponent(s,js,observer,parent)
        parent.objectpanel.add(oe)
        revalidate()
        repaint()
        return oe
    }
    fun removeObjectElement(parent:ObjectComponent,e:ObjectElementComponent){
        parent.objectpanel.remove(e)
        revalidate()
        repaint()
    }
    fun addMap(s: String, jo: JsonObject, observer: JsonObjectObserver):TableView.MapComponent {
        val m = MapComponent(s,jo,observer)
        add(m)
        revalidate()
        repaint()
        return m
    }
    fun removeMap(map:TableView.MapComponent) {
        remove(map)
        revalidate()
        repaint()
    }
    fun addMapElement(parent:TableView.MapComponent,s:String, js:JsonString,observer: JsonObjectObserver):TableView.ElementComponent{
        val e = ElementComponent(s,js,observer)
        parent.add(e)
        revalidate()
        repaint()
        return e
    }
    fun removeMapElement(parent:TableView.MapComponent,e:ElementComponent){
        parent.remove(e)
        revalidate()
        repaint()
    }
    private fun button(text: String, action: () -> Unit): JButton {
        val button = JButton(text)
        button.addActionListener { action() }
        return button
    }

    inner class ElementComponent(var s: String, var js: JsonString, observer: JsonObjectObserver) : JPanel() {
        private val first = JLabel(s)
        var second : JComponent = JTextField(js.toString())
            set(value) {
                field = value
                removeAll()
                add(first, BorderLayout.WEST)
                add(value, BorderLayout.CENTER)
                add(deleteButton, BorderLayout.EAST)
                revalidate()
                repaint()
            }
        private val deleteButton = button("delete") {
            if(second is JTextField){ js = JsonString((second as JTextField).text.toString())}
            if(second is JCheckBox){
                js = if((second as JCheckBox).isSelected){ JsonString("true") }
                else{ JsonString("false") }
            }
            if(this@ElementComponent.parent==this@TableView){
                observer.elementRemoved(s, js, this@ElementComponent)
            }
            else {
                observer.mapElementRemoved(this@ElementComponent.parent as MapComponent, this@ElementComponent,s,js)
            }
        }
        init {
            if (js.toString() == "true"  || js.toString() == "True"){
                second = JCheckBox("",true)

            }
            else if(js.toString() == "false"  || js.toString() == "False"){
                second = JCheckBox()
            }
            layout = BorderLayout(10, 0)
            border = BorderFactory.createLineBorder(Color.BLACK, 2)

            add(first, BorderLayout.WEST)
            second.isEnabled = true
            add(second, BorderLayout.CENTER)

            deleteButton.alignmentX = Component.RIGHT_ALIGNMENT
            add(deleteButton, BorderLayout.EAST)
            second.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        if (this@ElementComponent.parent == this@TableView) {
                            observer.elementModified(s, JsonString((second as JTextField).text.toString()),this@ElementComponent)
                        } else {
                            observer.mapElementModified(this@ElementComponent.parent as MapComponent, s, JsonString((second as JTextField).text.toString()),this@ElementComponent)
                        }
                        if ((second as JTextField).text == "true" || (second as JTextField).text == "True") {
                            remove(second)
                            second = JCheckBox("", true)
                            add(second)

                        } else if ((second as JTextField).text == "false" || (second as JTextField).text.toString() == "False") {
                            remove(second)
                            second = JCheckBox()
                            add(second)

                        }
                        if(second is JCheckBox) {
                            (second as JCheckBox).addItemListener { e ->
                                if (e.stateChange == ItemEvent.SELECTED) {
                                    if (this@ElementComponent.parent == this@TableView) {
                                        observer.elementModified(s, JsonString("true"),this@ElementComponent)
                                    } else {
                                        observer.mapElementModified(this@ElementComponent.parent as MapComponent, s, JsonString("true"),this@ElementComponent)
                                    }
                                } else if (e.stateChange == ItemEvent.DESELECTED) {
                                    if (this@ElementComponent.parent == this@TableView) {
                                        observer.elementModified(s, JsonString("false"),this@ElementComponent)
                                    } else {
                                        observer.mapElementModified(this@ElementComponent.parent as MapComponent, s, JsonString("false"),this@ElementComponent)
                                    }
                                }
                            }
                        }
                    }
                }
            })

        }

        fun matches(e:ElementComponent) = e == this@ElementComponent
    }

    inner class ArrayComponent(val s: String, val ja: JsonArray, val observer: JsonObjectObserver) : JPanel() {
        inner class MouseClick(val first: Boolean) : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val menu = JPopupMenu("Options")
                    val deleteArray = JButton("Delete Array")
                    deleteArray.addActionListener{
                        observer.arrayRemoved(s, ja, this@ArrayComponent)
                        menu.isVisible=false
                    }
                    menu.add(deleteArray)
                    menu.show(e.component,e.x,e.y)
                }
            }
        }
        init {
            var addObjectButton: Component? = null
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createLineBorder(Color.ORANGE, 2)
            val addElementButton = button("Add Element") {
                val text = JOptionPane.showInputDialog("Value")
                if(text != "") {
                    observer.simpleElementAdded(s, JsonString(text), this@ArrayComponent)
                }
                else{
                    observer.simpleElementAdded(s, JsonString("N/A"), this@ArrayComponent)
                }
                remove(addObjectButton)
            }
            addObjectButton = button("Add Object") {
                observer.arrayObjectAdded(s,this@ArrayComponent)
                remove(addElementButton)
            }
            addElementButton.alignmentX = Component.LEFT_ALIGNMENT
            addObjectButton.alignmentX = Component.LEFT_ALIGNMENT
            val arrayLabel = JLabel(s)
            arrayLabel.font = Font(arrayLabel.font.fontName, Font.BOLD, 14)
            arrayLabel.addMouseListener(MouseClick(true))
            add(arrayLabel)
            add(addElementButton)
            add(addObjectButton)
        }
    }

    inner class SimpleElementComponent(var textfield:JsonString, val observer: JsonObjectObserver) :JPanel(){
        private val deleteButton = button("Delete") {
            val parentComponent = this@SimpleElementComponent.parent as ArrayComponent
            observer.simpleElementRemoved(parentComponent,this@SimpleElementComponent)
        }
        var elementTextField = JTextField(textfield.toString())
            set(value) {
                field = value
                removeAll()
                add(value, BorderLayout.CENTER)
                add(deleteButton, BorderLayout.EAST)
                revalidate()
                repaint()
            }
        init {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT
            maximumSize = Dimension(Integer.MAX_VALUE, 30)
            add(elementTextField)
            add(deleteButton)
            elementTextField.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        val parentComponent = this@SimpleElementComponent.parent as ArrayComponent
                        observer.simpleElementModified(parentComponent.s,textfield,JsonString(elementTextField.text),this@SimpleElementComponent)
                        textfield = JsonString(elementTextField.text)
                    }
                }
            })
        }
    }

    inner class ObjectComponent(val s:String,val observer: JsonObjectObserver, val obj:JsonObject) :JPanel(){
        val objectpanel = JPanel()
        private val label = JLabel("N/A")
        inner class MouseClick(val first: Boolean) : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val menu = JPopupMenu("Options")
                    val addElement = JButton("Add Element")
                    val deleteObject = JButton("Delete Object")
                    addElement.addActionListener {
                        val lab = JOptionPane.showInputDialog("Label")
                        if(lab != "") {
                            observer.objectElementAdded(lab, JsonString("N/A"), this@ObjectComponent, s, obj)
                            label.isVisible = false
                            menu.isVisible = false
                            revalidate()
                            repaint()
                        }
                    }
                    deleteObject.addActionListener {
                        observer.arrayObjectRemoved(s,(this@ObjectComponent.parent as ArrayComponent),obj,this@ObjectComponent)
                        menu.isVisible=false
                        revalidate()
                        repaint()
                    }
                    menu.add(addElement)
                    menu.add(deleteObject)
                    menu.show(e.component,e.y,e.y)
                }
            }
        }
        init {
            objectpanel.layout = BoxLayout(objectpanel, BoxLayout.Y_AXIS)
            val border = BorderFactory.createLineBorder(Color.GRAY, 1)
            objectpanel.border = border
            objectpanel.add(label)
            objectpanel.addMouseListener(MouseClick(true))
            add(objectpanel)
        }
    }

    inner class ObjectElementComponent(val s: String, var js:JsonString, val observer: JsonObjectObserver, parent:ObjectComponent) : JPanel() {
        private val first = JLabel(s)
        var second : JComponent = JTextField(js.toString())
            set(value) {
                field = value
                removeAll()
                add(first, BorderLayout.WEST)
                add(value, BorderLayout.CENTER)
                add(deleteButton, BorderLayout.EAST)
                revalidate()
                repaint()
            }
        val deleteButton = button("delete") {
            if(second is JTextField){ js = JsonString((second as JTextField).text.toString())}
            if(second is JCheckBox){
                js = if((second as JCheckBox).isSelected){ JsonString("true") }
                else{ JsonString("false") }
            }
            observer.objectElementRemoved(s,js,parent.s,parent.obj,parent,this@ObjectElementComponent)
        }
        init {
            if (js.toString() == "true"  || js.toString() == "True"){
                second = JCheckBox("",true)

            }
            else if(js.toString() == "false"  || js.toString() == "False"){
                second = JCheckBox()
            }
            layout = BorderLayout(10, 0)
            border = BorderFactory.createLineBorder(Color.BLACK, 2)

            add(first, BorderLayout.WEST)
            second.isEnabled = true
            add(second, BorderLayout.CENTER)

            deleteButton.alignmentX = Component.RIGHT_ALIGNMENT
            add(deleteButton, BorderLayout.EAST)
            second.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        observer.objectElementModified(s, JsonString((second as JTextField).text), parent.s, parent.obj,this@ObjectElementComponent)
                    }
                    if ((second as JTextField).text == "true" || (second as JTextField).text == "True") {
                        remove(second)
                        second = JCheckBox("", true)
                        add(second)

                    } else if ((second as JTextField).text == "false" || (second as JTextField).text.toString() == "False") {
                        remove(second)
                        second = JCheckBox()
                        add(second)

                    }
                    if(second is JCheckBox) {
                        (second as JCheckBox).addItemListener { e ->
                            if (e.stateChange == ItemEvent.SELECTED) {
                                observer.objectElementModified(s, JsonString("true"), parent.s, parent.obj,this@ObjectElementComponent)
                            } else if (e.stateChange == ItemEvent.DESELECTED) {
                                observer.objectElementModified(s, JsonString("false"), parent.s, parent.obj,this@ObjectElementComponent)
                            }
                        }
                    }
                }
            })
        }
    }

    inner class MapComponent(val s: String, val jo:JsonObject, val observer: JsonObjectObserver) : JPanel() {
        inner class MouseClick(val first: Boolean) : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val menu = JPopupMenu("Options")
                    val deleteMap = JButton("Delete Object")
                    deleteMap.addActionListener{
                        observer.mapRemoved(s, jo, this@MapComponent)
                        menu.isVisible=false
                    }
                    menu.add(deleteMap)
                    menu.show(e.component,e.x,e.y)
                }
            }
        }
        init {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createLineBorder(Color.BLUE, 2)
            val addElementButton = button("Add Element") {
                val lab = JOptionPane.showInputDialog("label")
                if(lab != "") {
                    observer.mapElementAdded(jo, lab, JsonString("N/A"), this@MapComponent)
                }
            }
            addElementButton.alignmentX = Component.LEFT_ALIGNMENT
            val mapLabel = JLabel(s)
            mapLabel.font = Font(mapLabel.font.fontName, Font.BOLD, 14)
            mapLabel.addMouseListener(MouseClick(true))
            add(mapLabel)
            add(addElementButton)
        }
    }


}

