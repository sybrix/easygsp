import util.*
import groovy.sql.Sql

WebUtil.process(this, ['save', 'delete'])

        def get() {
                loadData(toInt(params.name_id))
        }

        def loadData(nameId) {
                try {

                        findDocumentation(nameId)

                        request.menuItems = findAllMenuItems()
                } catch (Exception e) {
                        e.printStackTrace()
                }
        }

        def findAllMenuItems() {
                db.rows('SELECT menu, menu_item menuItem, name_id nameId FROM easy_documentation order by menu_order, menu_item')
        }

        def save() {
                def nameId = params.name_id

                if (!exists(params.name_id)) {
                        nameId = insert()
                        updateMenuOrder()

                        redirect 'index.gsp?menu=' + encode(params.menu) + "&menuOrder=" + params.menuOrder
                } else {
                        update(toInt(nameId))
                        nameId = toInt(nameId)
                        updateMenuOrder()
                }

                loadData(nameId)
        }


        def insert() {
                def title = params.title
                def documentation = params.documentation
                def menuItem = params.menuItem
                def menu = params.menu
                def created = new Date()
                def lastModified = new Date()
                def showEditor = params.showEditor == null?false:true
                
                def nameId
                db.execute """
                                INSERT INTO easy_documentation ( title, documentation, created, last_modified, menu_item, menu, show_editor) values(
                                $title, $documentation, $created, $lastModified, $menuItem, $menu, $showEditor);
                               """

                db.eachRow("SELECT max(name_id) nameId FROM easy_documentation") {
                        nameId = it.nameId
                }

                return nameId
        }

        def exists(nameId) {
                boolean status = false
                if (nameId == null)
                        return status

                db.query("SELECT * from easy_documentation where name_id = $nameId") {
                        if (it.next()) {
                                status = true
                        }
                }

                return status;
        }

        def update(nameId) {

                def title = params.title
                def documentation = params.documentation
                def menuItem = params.menuItem
                def menu = params.menu
                def showEditor = params.showEditor == null?false:true
                
                def lastModified = new Date()
                cout "showEditor=" + showEditor
                cout documentation
                db.execute """
                                UPDATE easy_documentation SET  title = $title, documentation = $documentation, last_modified = $lastModified, menu = $menu, menu_item = $menuItem, show_editor = $showEditor
                                where name_id =  $nameId
                               """
        }

        def updateMenuOrder() {
                def menuOrder = params.menuOrder
                def menu = params.menu

                db.execute """
                                UPDATE easy_documentation SET  menu_order = $menuOrder where menu = $menu
                               """
        }

        def delete() {
                def nameId = params.name_id

                db.execute """
                                DELETE FROM easy_documentation WHERE name_id = $nameId
                               """

                redirect 'index.gsp'
        }

        def findAllNameIds() {
                db.rows('SELECT name_id nameId, title, documentation, created, last_modified lastModified, menu, menu_item menuItem FROM easy_documentation order by name_id')
        }

        def findDocumentation(Integer nameId) {
                if (nameId == null) {
                        bind 'title', ''
                        bind 'documentation', ''
                        bind 'nameId', ''
                        bind 'menuItem', ''
                        bind 'menu', params.menu ?: ''
                        bind 'menuOrder', params.menuOrder ?: '0'
                        bind 'showEditorChecked', '';
                        bind 'showEditor', true
                        return
                }


                db.eachRow("SELECT title, documentation, name_id nameId, menu, menu_item menuItem, menu_order menuOrder,show_editor showEditor from easy_documentation where name_id = $nameId") {
                        bind 'title', it.title
                        bind 'documentation', it.documentation
                        bind 'nameId', it.nameId
                        bind 'menu', it.menu
                        bind 'menuItem', it.menuItem
                        bind 'menuOrder', it.menuOrder
                        bind 'showEditorChecked', it.showEditor == true?'checked':'';
                        bind 'showEditor', it.showEditor
                        
                }
        }

        def getDb() {
                DBUtil.getDb()
        }

