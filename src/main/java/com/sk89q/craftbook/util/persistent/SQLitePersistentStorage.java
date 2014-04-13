package com.sk89q.craftbook.util.persistent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class SQLitePersistentStorage extends PersistentStorage {

    Connection db;

    @Override
    public void open () {

        createConnection();
    }

    public void createConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            db = DriverManager.getConnection("jdbc:sqlite:plugins/CraftBook/persistance.db");

            DatabaseMetaData dbm = db.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "PersistentData", null);

            if(tables.next()) //We already have something in this table, don't create it!
                return;
            else {
                String createTable = "CREATE TABLE PersistentData (KEY VARCHAR(255) PRIMARY KEY, VALUE TEXT)";
                try {
                    Statement state = db.createStatement();
                    state.executeUpdate(createTable);
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close () {
        try {
            if(!db.isClosed())
                db.close();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public String getType () {
        return "SQLite";
    }

    @Override
    public Object get (String location) {

        try {
            PreparedStatement statement = db.prepareStatement("SELECT * FROM PersistentData WHERE KEY = ?");
            statement.setString(1, location);
            ResultSet results = statement.executeQuery();

            if(!results.next()) return null;

            return fromString(results.getString(2));
        } catch(SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void set (String location, Object data) {

        if(!(data instanceof Serializable)) {
            CraftBookPlugin.logger().warning("Failed to put item in db! " + data.getClass().getSimpleName() + " is NOT serializable!");
            return;
        }

        try {
            PreparedStatement statement = db.prepareStatement("INSERT OR REPLACE INTO PersistentData VALUES(?,?)");
            statement.setString(1, location);
            statement.setObject(2, toString((Serializable) data));

            statement.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean has (String location) {

        try {
            PreparedStatement statement = db.prepareStatement("SELECT * FROM PersistentData WHERE KEY = ?");
            statement.setString(1, location);
            ResultSet results = statement.executeQuery();

            return results.next();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isValid () {
        try {
            return db != null && !db.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getVersion () {

        try {
            PreparedStatement statement = db.prepareStatement("SELECT * FROM PersistentData WHERE KEY = ?");
            statement.setString(1, "VERSION");
            ResultSet results = statement.executeQuery();

            if(!results.next()) {
                statement = db.prepareStatement("INSERT INTO PersistentData VALUES(?,?)");
                statement.setString(1, "VERSION");
                statement.setInt(2, getCurrentVersion());
            } else {
                return results.getInt(2);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return getCurrentVersion();
    }

    @Override
    public int getCurrentVersion () {
        return 1;
    }

    @Override
    public void convertVersion (int version) {
        //Not needed atm.
    }

    @Override
    public void importData (Map<String, Object> data, boolean replace) {

        try {
            if(replace)
                db.prepareStatement("DELETE FROM PersistentData").execute();
            for(Entry<String, Object> dat : data.entrySet())
                set(dat.getKey(), dat.getValue());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Object> exportData () {

        Map<String, Object> data = new HashMap<String, Object>();

        try {
            PreparedStatement statement = db.prepareStatement("SELECT * FROM PersistentData");
            ResultSet results = statement.executeQuery();
            while(results.next()) {
                data.put(results.getString(1), results.getObject(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    private static Object fromString(String s) throws IOException, ClassNotFoundException {
        byte[] data = s.getBytes();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    private static String toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return new String(baos.toByteArray());
    }
}