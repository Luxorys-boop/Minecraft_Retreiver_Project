package com.example.minecraft_retreiver_project;

import static com.example.minecraft_retreiver_project.DBContract.FormServers.COLUMN_IP;
import static com.example.minecraft_retreiver_project.DBContract.FormServers.COLUMN_MOTD;
import static com.example.minecraft_retreiver_project.DBContract.FormServers.COLUMN_NAME;
import static com.example.minecraft_retreiver_project.DBContract.FormServers._SERVERID;
import static com.example.minecraft_retreiver_project.DBContract.UserServerRelation.COLUMN_SERVER_ID;
import static com.example.minecraft_retreiver_project.DBContract.UserServerRelation.COLUMN_USER_ID;
import static com.example.minecraft_retreiver_project.DBContract.UserServerRelation._ID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    //change version when upgraded
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "minecraft.db";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override

    /**
     * Crée les tables de la base de données lors de la première installation
     * @param db La base de données SQLite
     */
    public void onCreate(SQLiteDatabase db) {
        String query =  "CREATE TABLE " + DBContract.FormUsers.TABLE_NAME + " (" +
                DBContract.FormUsers._USERID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DBContract.FormUsers.COLUMN_EMAIL + " TEXT," +
                DBContract.FormUsers.COLUMN_PASSWD+ " TEXT," +
                DBContract.FormUsers.COLUMN_PSEUDO + " TEXT)";
        db.execSQL(query);

        String query2 =  "CREATE TABLE " + DBContract.FormServers.TABLE_NAME + " (" +
                _SERVERID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME + " TEXT," +
                COLUMN_IP+ " TEXT," +
                COLUMN_MOTD + " TEXT)";
        db.execSQL(query2);

        String query3 = "CREATE TABLE " + DBContract.UserServerRelation.TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_USER_ID + " BIGINT," +
                COLUMN_SERVER_ID + " BIGINT," +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                DBContract.FormUsers.TABLE_NAME + "(" + DBContract.FormUsers._USERID + ")," +
                "FOREIGN KEY(" + COLUMN_SERVER_ID + ") REFERENCES " +
                DBContract.FormServers.TABLE_NAME + "(" + _SERVERID + "))";
        db.execSQL(query3);
    }

    /**
     * Met à jour la base de données lors d'un changement de version
     * @param db La base de données SQLite
     * @param oldVersion Ancienne version de la base
     * @param newVersion Nouvelle version de la base
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Suppression de toutes les tables
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.UserServerRelation.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.FormUsers.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.FormServers.TABLE_NAME);
        // Recréation de la base de données
        onCreate(db);
    }

    /**
     * Enregistre un nouvel utilisateur dans la base de données
     * @param email L'email de l'utilisateur
     * @param plainPassword Le mot de passe en clair (sera hashé)
     * @param pseudo Le pseudonyme de l'utilisateur
     * @return true si l'inscription a réussi, false sinon
     */
    public boolean registerUser(String email, String plainPassword, String pseudo) {
        // Chiffrer le mot de passe
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContract.FormUsers.COLUMN_EMAIL, email);
        values.put(DBContract.FormUsers.COLUMN_PASSWD, hashedPassword); // Stocker le hash
        values.put(DBContract.FormUsers.COLUMN_PSEUDO, pseudo);

        long result = db.insert(DBContract.FormUsers.TABLE_NAME, null, values);
        return result != -1;
    }

    /**
     * Vérifie les identifiants de connexion d'un utilisateur
     * @param email L'email de l'utilisateur
     * @param plainPassword Le mot de passe en clair à vérifier
     * @return true si les identifiants sont valides, false sinon
     */
    public boolean checkUser(String email, String plainPassword) {

        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {DBContract.FormUsers.COLUMN_PASSWD};
        String selection = DBContract.FormUsers.COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};


        Cursor cursor = db.query(
                DBContract.FormUsers.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null, null, null);

        if (cursor.moveToFirst()) {
            String hashedPassword = cursor.getString(0);
            Log.d("HASHED PASSWD", hashedPassword);
            Log.d("JE PASSE ICI", "JE PASSE ICI ENFT");
            return BCrypt.checkpw(plainPassword, hashedPassword);
        }

        cursor.close();
        return false;
    }

    /**
     * Récupère la liste des serveurs associés à un utilisateur
     * @param userEmail L'email de l'utilisateur
     * @return Liste des serveurs de l'utilisateur
     */
    public List<Servers> getUserServers(String userEmail) {
        List<Servers> serversList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT s.* FROM " + DBContract.FormServers.TABLE_NAME + " s " +
                    "INNER JOIN " + DBContract.UserServerRelation.TABLE_NAME + " usr ON s." +
                    DBContract.FormServers._SERVERID + " = usr." + DBContract.UserServerRelation.COLUMN_SERVER_ID + " " +
                    "INNER JOIN " + DBContract.FormUsers.TABLE_NAME + " u ON usr." +
                    DBContract.UserServerRelation.COLUMN_USER_ID + " = u." + DBContract.FormUsers._USERID + " " +
                    "WHERE u." + DBContract.FormUsers.COLUMN_EMAIL + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{userEmail});

            if (cursor.moveToFirst()) {
                do {
                    Servers server = new Servers();
                    server.setNom(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.FormServers.COLUMN_NAME)));
                    server.setIp(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.FormServers.COLUMN_IP)));
                    server.setMotd(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.FormServers.COLUMN_MOTD)));
                    serversList.add(server);
                    Log.d("DBHandler", "Server ajouté a liste: " + server.getNom());
                } while (cursor.moveToNext());
            } else {
                Log.d("DBHandler", "Aucun serveur pour l'utilisateur : " + userEmail);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("DB_ERROR", "Erreur de récupération données utilisateur", e);
        }
        return serversList;
    }

    /**
     * Récupère l'ID d'un utilisateur à partir de son email
     * @param email L'email de l'utilisateur
     * @return L'ID de l'utilisateur ou -1 si non trouvé
     */
    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {DBContract.FormUsers._USERID};
        String selection = DBContract.FormUsers.COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(
                DBContract.FormUsers.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null, null, null);

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    /**
     * Crée un utilisateur exemple dans la base de données
     */
    public void createExampleUser() {
        if (!userExists("example@gmail.com")) {
            registerUser("example@gmail.com", "haha", "ExempleUser");
        }
    }

    /**
     * Vérifie si un utilisateur existe dans la base
     * @param email L'email à vérifier
     * @return true si l'utilisateur existe, false sinon
     */
    public boolean userExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + DBContract.FormUsers.TABLE_NAME +
                " WHERE " + DBContract.FormUsers.COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Ajoute un serveur exemple dans la base de données
     */
    public void addExampleServer() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Vérifier si le serveur existe déjà
        if (!serverExists("Hypixel")) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, "Hypixel");
            values.put(COLUMN_IP, "play.hypixel.net");
            values.put(COLUMN_MOTD, "The largest Minecraft server!");
            long serverId = db.insert(DBContract.FormServers.TABLE_NAME, null, values);

            // Associer au compte exemple
            int userId = getUserIdByEmail("example@gmail.com");
            if (userId != -1 && serverId != -1) {
                ContentValues relationValues = new ContentValues();
                relationValues.put(COLUMN_USER_ID, userId);
                relationValues.put(COLUMN_SERVER_ID, serverId);
                db.insert(DBContract.UserServerRelation.TABLE_NAME, null, relationValues);
            }
        }
    }

    /**
     * Vérifie si un serveur existe dans la base
     * @param serverName Le nom du serveur à vérifier
     * @return true si le serveur existe, false sinon
     */
    public boolean serverExists(String serverName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + DBContract.FormServers.TABLE_NAME +
                " WHERE " + COLUMN_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{serverName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Ajoute un nouveau serveur à la base et l'associe à un utilisateur
     * @param name Le nom du serveur
     * @param ip L'adresse IP du serveur
     * @param motd Le message du serveur (MOTD)
     * @param userId L'ID de l'utilisateur propriétaire
     * @return L'ID du serveur créé ou -1 en cas d'erreur
     */
    public long addServer(String name, String ip, String motd, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long serverId = -1;

        try {
            // Vérifier si le serveur existe déjà
            Cursor cursor = db.query(
                    DBContract.FormServers.TABLE_NAME,
                    new String[]{_SERVERID},
                    COLUMN_IP + " = ?",
                    new String[]{ip},
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                // Serveur existe déjà, récupérer son ID
                serverId = cursor.getLong(0);
            } else {
                // Ajouter le nouveau serveur
                ContentValues serverValues = new ContentValues();
                serverValues.put(COLUMN_NAME, name);
                serverValues.put(COLUMN_IP, ip);
                serverValues.put(COLUMN_MOTD, motd);
                serverId = db.insert(DBContract.FormServers.TABLE_NAME, null, serverValues);
            }
            cursor.close();

            // Créer la relation utilisateur-serveur si elle n'existe pas déjà
            if (serverId != -1) {
                ContentValues relationValues = new ContentValues();
                relationValues.put(COLUMN_USER_ID, userId);
                relationValues.put(COLUMN_SERVER_ID, serverId);

                // Vérifier si la relation existe déjà
                cursor = db.query(
                        DBContract.UserServerRelation.TABLE_NAME,
                        new String[]{_ID},
                        COLUMN_USER_ID + " = ? AND " + COLUMN_SERVER_ID + " = ?",
                        new String[]{String.valueOf(userId), String.valueOf(serverId)},
                        null, null, null
                );

                if (!cursor.moveToFirst()) {
                    // La relation n'existe pas, on l'ajoute
                    db.insert(DBContract.UserServerRelation.TABLE_NAME, null, relationValues);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return serverId;
    }

    /**
     * Récupère une liste de serveurs suggérés/prédéfinis
     * @return Liste des serveurs populaires
     */
    public List<Servers> getServeursSuggeres() {
        List<Servers> serveursSugerres = new ArrayList<>();

        // Liste de serveurs populaires prédéfinis
        String[][] popularServers = {
                {"Hypixel", "play.hypixel.net", "Le plus grand serveur Minecraft!"},
                {"Mineplex", "us.mineplex.com", "Serveur mini-jeux populaire"},
                {"Badlion", "badlion.net", "Client et serveur compétitif"},
                {"Cubecraft", "play.cubecraft.net", "Serveur mini-jeux européen"},
                {"The Hive", "play.hivemc.com", "Serveur mini-jeux populaire"}
        };

        for (String[] server : popularServers) {
            Servers s = new Servers();
            s.setNom(server[0]);
            s.setIp(server[1]);
            s.setMotd(server[2]);
            serveursSugerres.add(s);
        }

        return serveursSugerres;
    }


    /**
     * Supprime un serveur de la base de données par son adresse IP
     * @param serverIp L'adresse IP du serveur à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteServerByIp(String serverIp) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;

        try {
            // Trouver l'id du serveur en utilisant l'adresse IP
            String[] columns = {_SERVERID};
            String selection = COLUMN_IP + " = ?";
            String[] selectionArgs = {serverIp};

            Cursor cursor = db.query(
                    DBContract.FormServers.TABLE_NAME,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                long serverId = cursor.getLong(0);

                // Supprimer la relation de UserServerRelation
                int affectedRelations = db.delete(
                        DBContract.UserServerRelation.TABLE_NAME,
                        COLUMN_SERVER_ID + " = ?",
                        new String[]{String.valueOf(serverId)}
                );

                // Supprimer le serveur de la table Servers
                int affectedServers = db.delete(
                        DBContract.FormServers.TABLE_NAME,
                        _SERVERID + " = ?",
                        new String[]{String.valueOf(serverId)}
                );

                // Vérification de si la suppression est un succès.
                success = affectedRelations > 0 && affectedServers > 0;
            }

            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return success;
    }
}

