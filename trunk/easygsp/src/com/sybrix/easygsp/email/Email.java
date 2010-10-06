package com.sybrix.easygsp.email;

import com.sybrix.easygsp.http.ServletContextImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Email <br/>
 *
 * @author David Lee
 */
public class Email {
        private String host;
        private int port;
        private String from;
        private List<String> recipients = new ArrayList();
        private List<String> bcc= new ArrayList();;
        private List<String> cc= new ArrayList();;
        private String contextType;
        private String body;
        private String htmlBody;
        private String subject;
        private String username;
        private String password;
        private boolean authenticationRequired;
        private boolean secure;
        private Map attachments = new HashMap();
        private ServletContextImpl app;

        public String getHtmlBody() {
                return htmlBody;
        }
        public void setHtmlBody(String htmlBody) {
                this.htmlBody = htmlBody;
        }
        public String getHost() {
                return host;
        }
        public void setHost(String host) {
                this.host = host;
        }
        public int getPort() {
                return port;
        }
        public void setPort(int port) {
                this.port = port;
        }
        public String getFrom() {
                return from;
        }
        public void setFrom(String from) {
                this.from = from;
        }

        public List<String> getRecipients() {
                return recipients;
        }
        public void setRecipients(List<String> recipients) {
                this.recipients = recipients;
        }
        public List<String> getBcc() {
                return bcc;
        }
        public void setBcc(List<String> bcc) {
                this.bcc = bcc;
        }
        public List<String> getCc() {
                return cc;
        }
        public void setCc(List<String> cc) {
                this.cc = cc;
        }
        public String getContextType() {
                return contextType;
        }
        public void setContextType(String contextType) {
                this.contextType = contextType;
        }
        public String getBody() {
                return body;
        }
        public void setBody(String body) {
                this.body = body;
        }
        public String getSubject() {
                return subject;
        }
        public void setSubject(String subject) {
                this.subject = subject;
        }

        public boolean isAuthenticationRequired() {
                return authenticationRequired;
        }
        public void setAuthenticationRequired(boolean authenticationRequired) {
                this.authenticationRequired = authenticationRequired;
        }
        public String getUsername() {
                return username;
        }
        public void setUsername(String username) {
                this.username = username;
        }
        public String getPassword() {
                return password;
        }
        public void setPassword(String password) {
                this.password = password;
        }

        public boolean isSecure() {
                return secure;
        }

        public void setSecure(boolean secure) {
                this.secure = secure;
        }

        public Map<String, Object> getAttachments() {
                return attachments;
        }

        public void setAttachments(Map attachments) {
                this.attachments = attachments;
        }

        public ServletContextImpl getApp() {
                return app;
        }

        public void setApp(ServletContextImpl app) {
                this.app = app;     
        }

        @Override
        public String toString() {
                return "Email{" +
                        "host='" + host + '\'' +
                        ", port=" + port +
                        ", from='" + from + '\'' +
                        ", recipients=" + recipients +
                        ", bcc=" + bcc +
                        ", cc=" + cc +
                        ", contextType='" + contextType + '\'' +
                        ", body='" + body + '\'' +
                        ", htmlBody='" + htmlBody + '\'' +
                        ", subject='" + subject + '\'' +
                        ", username='" + username + '\''+ 
                        ", password='" + password + '\'' +
                        ", authenticationRequired=" + authenticationRequired +
                        ", secure=" + secure +
                        ", attachments=" + attachments +
                        '}';
        }
}
