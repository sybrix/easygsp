package com.sybrix.easygsp.db

import groovy.sql.Sql

class CurrentSQLInstance {
        private static final ThreadLocal<Sql> _id = new ThreadLocal<Sql>() {
                protected Object initialValue() {
                        return null
                }
        }

        public static Sql get() {
                return _id.get()
        }

        protected static void set(Sql id) {
                _id.set(id)
        }
}