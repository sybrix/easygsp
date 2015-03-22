Back: [Retrieving Data](easyom_query.md)  |  Next: [Persisting Data](easyom_persist.md)

# StringQueries #
StringQueries add methods to the String class and allow you to invoke one of three execute methods on any String that happens to be a query.


The following methods are added to the String class
| def executeQuery | Executes a SELECT query |
|:-----------------|:------------------------|
| def executeUpdate | Used to execute insert, update and delete queries  |
| def executeScalar | Executes a SELECT query and returns a single value (column 1 of the first row)|

StringQueries can be executed with queries parameters, paging options and/or the object return type.


### def executeQuery(Object`[]` parameters) ###
Executes a SELECT query and returns a list or GroovyResultRow a specified object type.

When no parameters result type is specified, a list of GroovyResultRow objects is returned.  Then GroovyResultRow behaves very much like a bean:
```

    def users = "SELECT username, first_name firstName, last_name lastName FROM tblUser".executeQuery()
    users.each {user ->
       println "username: $user.username, real name:  $user.lastName, $user.firstName"
    }

```


executeQuery() with return type specified. Returns a list of User objects
```

    def users = "SELECT username, first_name firstName, last_name lastName FROM tblUser".executeQuery(User.class)
    users.each {user ->
       println "username: $user.username, real name:  $user.lastName, $user.firstName"
    }

```


executeQuery() with parameter. One or parameters can be specified as arguments for executeQuery().

```
    String lastName = 'Smith'
    def users

    users = "SELECT username, first_name firstName, last_name lastName FROM tblUser WHERE last_name= ${lastName}".executeQuery()
  

    users.each {user ->
       println "username: $user.username, real name:  $user.lastName, $user.firstName"
    }

```

executeQuery() with parameter and result type.  When both are specified, result type should be the first argument, followed by the parameter values.

```
    String lastName = 'Smith'
    def users = "SELECT username, first_name firstName, last_name lastName FROM tblUser WHERE last_name= ${lastName}".executeQuery(User.class)  

```

## Paging ##

To specify paging, the query must have an ORDER BY clause, the parameter list must be a map with the following attributes.

**Parameter Map:**
| page | The page being requested. Required. |
|:-----|:------------------------------------|
| pageSize | The number of records per page. Required.|
| resultClass | The type of object in the results list. Optional.|

When paging is specified, the return object is a map.  The map contains 4 attributes w/values:

**Return Object:**
| recordCount | the total number of records in the DB for the specified query |
|:------------|:--------------------------------------------------------------|
| page | The current page of the result set |
| pageCount | The total number of pages in the query results |
| results | A list of GroovyRowResult objects, or a list of the specified return type |

executeQuery() with paging.
```
    def pagedResults = "SELECT username, first_name firstName, last_name lastName FROM tblUser ORDER BY last_name".executeQuery([page: 2, pageSize: 10])

    assertTrue(pagedResults.recordCount > 0)
    assertTrue(pagedResults.page == 2)
    assertTrue(pagedResults.pageCount > 0)
    assertTrue(pagedResults.results.size() > 0)
```

Another example with paging and result type specified.
```
    def pagedResults = "SELECT username, first_name firstName, last_name lastName FROM tblUser ORDER BY last_name ".executeQuery([page: 2, pageSize: 10, resultClass: User.class])

```

Another example with paging, result type specified and query parameter values
```
    def param1 = 'Smith'
    def pagedResults = "SELECT username, first_name firstName, last_name lastName FROM tblUser WHERE last_name = ${param1} ORDER BY last_name".executeQuery([page: 2, pageSize: 10, resultClass: User.class])

```


### Integer executeUpdate(List values) ###
Executes an INSERT, UPDATE or DELETE query.
```
    def createNewUser(username, lastName, firstName){
       "INSERT INTO tbluser (username, first_name, last_name) VALUES($username,$lastName, $firstName)".executeUpdate()
    
    }
```

Another executeUpdate example

```
    def deleteUser(username){
       "DELETE FROM tbluser WHERE username = ${username}".executeUpdate()
    }
```


### def executeScalar(List values) ###
Returns a single value

```
    def getTotalNumberUsers(username){
       "SELECT count(*) FROM tblUser".executeScalar()
    }
```


## StringQueries ##
String queries allow you to do call an "execute" query method on a simple String.

```
    // returns the total number of records in table tblUser
    def getTotalRecordCount(){
       "SELECT count(*) FROM tblUser".executeScalar()
    }
```