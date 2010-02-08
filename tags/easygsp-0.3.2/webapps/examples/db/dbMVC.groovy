        import groovy.sql.Sql

        def sql = Sql.newInstance("jdbc:mysql://localhost:3306/sybrixApps","root", "root", "com.mysql.jdbc.Driver")
        def limit = 10

        def data = sql.rows("select profile_id profileId, last_name lastName, first_name firstName from profile where profile_id < ${limit}")

        bind 'profiles', data