package com.sybrix.easygsp.http;

import com.sybrix.easygsp.server.EasyGServer;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

/**
 * CacheKeyManager <br/>
 *
 * @author David Lee
 */
public class CacheKeyManager {
        private static final Logger log = Logger.getLogger(CacheKeyManager.class.getName());
        private static String dbUrl;
        private static String dbUid;
        private static String dbPwd;
        private static boolean enabled;

        static {
                try {
                        enabled = EasyGServer.propertiesFile.getBoolean("key.cache.enabled");
                        if (enabled) {
                                dbUrl = EasyGServer.propertiesFile.getString("key.cache.db.url");
                                dbUid = EasyGServer.propertiesFile.getString("key.cache.db.username");
                                dbPwd = EasyGServer.propertiesFile.getString("key.cache.db.password");

                                Class.forName(EasyGServer.propertiesFile.getString("key.cache.db.driverClassName"));
                        }
                } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Class not found exception on CacheKeyManager static init().  Make sure db driver jar is in classpath", e);
                }
        }

        public static void init() {
                if (!enabled) {
                        try {
                                log.fine("CacheKeyManager not enabled, clearing caches.");
                                JCS.getInstance("appCache").clear();
                                JCS.getInstance("sessionCache").clear();

                                removeAll();
                        } catch (CacheException e) {

                        }
                } else {
                        log.fine("CacheKeyManager initialized.");
                }
        }


        public static Connection getConnection() throws SQLException {
                return java.sql.DriverManager.getConnection(dbUrl, dbUid, dbPwd);
        }


        public static void setAppKey(String appId, String key) {
                if (!enabled)
                        return;

                Connection conn = null;
                Statement ps = null;

                try {
                        conn = getConnection();
                        String sql = "INSERT INTO easygsp_keys (app_id, key_id) VALUES('" + appId + "','" + key + "')";
                        String deleteSql = "DELETE FROM easygsp_keys WHERE app_id = '" + appId + "' and key_id='" + key + "'";

                        ps = conn.createStatement();
                        ps.addBatch(deleteSql);
                        ps.addBatch(sql);

                        ps.executeBatch();
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                } finally {
                        close(ps);
                        close(conn);
                }
        }


        public static void setSessionKey(String appId, String sessionId, String keyId) {
                if (!enabled)
                        return;

                Connection conn = null;
                Statement ps = null;

                try {
                        conn = getConnection();
                        String deleteSql = "DELETE FROM easygsp_session_keys WHERE app_id='" + appId + "' and key_id ='" + keyId + "'";
                        String sql = "INSERT INTO easygsp_session_keys (app_id, session_id, key_id) VALUES('" + appId + "','" + sessionId + "','" + keyId + "')";

                        ps = conn.createStatement();
                        ps.addBatch(deleteSql);
                        ps.addBatch(sql);

                        ps.executeBatch();
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                } finally {
                        close(ps);
                        close(conn);
                }

//                String cacheKey = sessionId + "_" + key;
//                insertSession(appId, cacheKey, "s");
        }

//        public static void saveSessionState(String appId, SessionImpl session) {
//                if (!enabled)
//                        return;
//
//                Connection conn = null;
//                Statement ps = null;
//
//                try {
//                        conn = getConnection();
//                        String deleteSql = "DELETE FROM easygsp_session_keys WHERE app_id='" + appId + "' and key_id ='" + keyId + "'";
//                        String sql = "INSERT INTO easygsp_session_keys (app_id, session_id, key_id) VALUES('" + appId + "','" + sessionId + "','" + keyId + "')";
//
//                        ps = conn.createStatement();
//                        ps.addBatch(deleteSql);
//                        ps.addBatch(sql);
//
//                        ps.executeBatch();
//                } catch (SQLException e) {
//                        throw new RuntimeException(e);
//                } finally {
//                        close(ps);
//                        close(conn);
//                }
//
////                String cacheKey = sessionId + "_" + key;
////                insertSession(appId, cacheKey, "s");
//        }


        public static void removeAppKey(String appId, String key) {
                if (!enabled)
                        return;

                Connection conn = null;
                PreparedStatement ps = null;

                try {
                        conn = getConnection();
                        String sql = "DELETE FROM easygsp_keys WHERE app_id = ? and key_id=?";

                        ps = conn.prepareStatement(sql);
                        ps.setString(1, appId);
                        ps.setString(2, key);
                        ps.executeUpdate();


                } catch (SQLException e) {
                        throw new RuntimeException(e);
                } finally {
                        close(ps);
                        close(conn);
                }
        }

        public static void removeApp(String appId) {
                if (!enabled)
                        return;

                Connection conn = null;
                Statement ps = null;

                try {
                        conn = getConnection();
                        String sql = "DELETE FROM easygsp_session_keys WHERE app_id='" + appId + "'";
                        String deleteSql = "DELETE FROM easygsp_keys WHERE app_id='" + appId + "'";

                        ps = conn.createStatement();
                        ps.addBatch(sql);
                        ps.addBatch(deleteSql);

                        ps.executeBatch();
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                } finally {
                        close(ps);
                        close(conn);
                }

//                String cacheKey = sessionId + "_" + key;
//                insertSession(appId, cacheKey, "s");
        }


        public static void removeAll() {
                if (!enabled)
                        return;

                Connection conn = null;
                Statement ps = null;

                try {
                        conn = getConnection();
                        String sql = "DELETE FROM easygsp_session_keys";
                        String deleteSql = "DELETE FROM easygsp_keys";

                        ps = conn.createStatement();
                        ps.addBatch(sql);
                        ps.addBatch(deleteSql);

                        ps.executeBatch();
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                } finally {
                        close(ps);
                        close(conn);
                }
        }

        public static void removeSession(String appId, String sessionId) {
                if (!enabled)
                        return;

                Connection conn = null;
                PreparedStatement ps = null;

                try {
                        conn = getConnection();
                        String sql = "DELETE FROM easygsp_session_keys WHERE app_id=? and session_id=?";

                        ps = conn.prepareStatement(sql);
                        ps.setString(1, appId);
                        ps.setString(2, sessionId);

                        ps.executeUpdate();
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                } finally {
                        close(ps);
                        close(conn);
                }

//                String cacheKey = sessionId + "_" + key;
//                insertSession(appId, cacheKey, "s");
        }

        public static void removeSessionKey(String appId, String sessionId) {
                if (!enabled)
                        return;

                Connection conn = null;
                PreparedStatement ps = null;

                try {
                        conn = getConnection();
                        String sql = "DELETE FROM easygsp_session_keys WHERE app_id=? and session_id=?";

                        ps = conn.prepareStatement(sql);
                        ps.setString(1, appId);
                        ps.setString(2, sessionId);
                        ps.executeUpdate();

                } catch (SQLException e) {
                        throw new RuntimeException(e);
                } finally {
                        close(ps);
                        close(conn);
                }

        }

        public static List getAllAppKeys(String appId) {
                if (!enabled)
                        return new ArrayList();

                Connection conn = null;
                PreparedStatement ps = null;
                ResultSet rs = null;

                List data = new ArrayList();
                try {
                        conn = getConnection();
                        String sql = "SELECT key_id FROM easygsp_keys where app_id = ?";

                        ps = conn.prepareStatement(sql);
                        ps.setString(1, appId);
                        rs = ps.executeQuery();

                        while (rs.next()) {
                                data.add(rs.getString(1));
                        }

                } catch (SQLException e) {
                        throw new RuntimeException(e);
                } finally {
                        close(rs);
                        close(ps);
                        close(conn);
                }

                return data;

        }

        public static List getAllSessionKeys(String appId, String sessionId) {
                if (!enabled)
                        return new ArrayList();

                Connection conn = null;
                PreparedStatement ps = null;
                ResultSet rs = null;

                List data = new ArrayList();
                try {
                        conn = getConnection();
                        String sql = "SELECT key_id FROM easygsp_session_keys where app_id = ? and session_id = ?";

                        ps = conn.prepareStatement(sql);
                        ps.setString(1, appId);
                        ps.setString(2, sessionId);
                        rs = ps.executeQuery();

                        while (rs.next()) {
                                data.add(rs.getString(1));
                        }

                } catch (SQLException e) {
                        throw new RuntimeException(e);
                } finally {
                        close(rs);
                        close(ps);
                        close(conn);
                }

                return data;
        }


        public static List getAllSessionIds(String appId) {
                if (!enabled)
                        return new ArrayList();

                Connection conn = null;
                PreparedStatement ps = null;
                ResultSet rs = null;

                List data = new ArrayList();
                try {
                        conn = getConnection();
                        String sql = "SELECT DISTINCT session_id FROM easygsp_session_keys where app_id = ?";

                        ps = conn.prepareStatement(sql);
                        ps.setString(1, appId);
                        rs = ps.executeQuery();

                        while (rs.next()) {
                                data.add(rs.getString(1));
                        }

                } catch (SQLException e) {
                        throw new RuntimeException(e);
                } finally {
                        close(rs);
                        close(ps);
                        close(conn);
                }

                return data;
        }

        public static boolean sessionIdExist(String appId, String session_id) {
                if (!enabled)
                        return false;

                Connection conn = null;
                PreparedStatement ps = null;
                ResultSet rs = null;

                try {
                        conn = getConnection();
                        String sql = "SELECT DISTINCT session_id FROM easygsp_session_keys where app_id = ? and session_id=?";

                        ps = conn.prepareStatement(sql);
                        ps.setString(1, appId);
                        ps.setString(2, session_id);
                        rs = ps.executeQuery();

                        if (rs.next()) {
                                return true;
                        } else {
                                return false;
                        }

                } catch (SQLException e) {
                        throw new RuntimeException(e);
                } finally {
                        close(rs);
                        close(ps);
                        close(conn);
                }
        }

        public static void close(Object sqlObj) {
                try {
                        if (sqlObj instanceof java.sql.Connection) {
                                if (((Connection) sqlObj).getAutoCommit() == true) {
                                        ((Connection) sqlObj).close();
                                }
                        } else if (sqlObj instanceof java.sql.ResultSet) {
                                ((ResultSet) sqlObj).close();
                        } else if (sqlObj instanceof java.sql.Statement) {
                                ((Statement) sqlObj).close();
                        }

                } catch (Exception e) {

                }
        }


}
