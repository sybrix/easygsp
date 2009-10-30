import util.*

request.menuItems = findAllMenuItems()

def findAllMenuItems() {
        db.rows('SELECT menu, menu_item menuItem, name_id nameId FROM easy_documentation order by menu_order, menu_item')
}


def getDb() {
        DBUtil.getDb()

}