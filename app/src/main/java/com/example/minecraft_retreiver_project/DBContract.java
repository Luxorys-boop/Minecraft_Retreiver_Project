package com.example.minecraft_retreiver_project;

public final class DBContract {

    public static class FormUsers {
        public static final String TABLE_NAME = "Users";
        public static final String _USERID = "userid";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWD = "password";
        public static final String COLUMN_PSEUDO = "pseudo";

    }

    public static class FormServers {
        public static final String TABLE_NAME = "Servers";
        public static final String _SERVERID = "serverid";
        public static final String COLUMN_NAME = "nom";
        public static final String COLUMN_IP= "ip";
        public static final String COLUMN_MOTD = "motd";
    }

    public static class UserServerRelation {
        public static final String TABLE_NAME = "UserServerRelation";
        public static final String _ID = "id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_SERVER_ID = "server_id";
    }
}
