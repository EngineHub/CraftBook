package com.sk89q.craftbook.util.persistent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class SQLitePersistentStorage extends PersistentStorage {

    Connection db;

    @Override
    public void open () {

        createConnection();
    }

    public void createConnection() {
        File file = new File(CraftBookPlugin.inst().getDataFolder(), "persistance.db");
        if(file.exists()) {
            file.renameTo(new File(CraftBookPlugin.inst().getDataFolder(), "persistence.db"));
            file.delete();
        }
        try {
            Class.forName("org.sqlite.JDBC");
            db = DriverManager.getConnection("jdbc:sqlite:plugins/CraftBook/persistence.db");

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

    private void close(ResultSet results) {

        if(results != null) {
            try {
                results.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void close(PreparedStatement statement) {

        if(statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Object get (String location) {

        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            statement = db.prepareStatement("SELECT * FROM PersistentData WHERE KEY = ?");
            statement.setString(1, location);
            results = statement.executeQuery();

            if(!results.next()) return null;

            return fromString(results.getString(2));
        } catch(SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            close(results);
            close(statement);
        }
        return null;
    }

    @Override
    public void set (String location, Object data) {

        if(!(data instanceof Serializable) && !(data instanceof ConfigurationSerializable)) {
            CraftBookPlugin.logger().warning("Failed to put item in db! " + data.getClass().getSimpleName() + " is NOT serializable!");
            return;
        }

        PreparedStatement statement = null;

        try {
            statement = db.prepareStatement("INSERT OR REPLACE INTO PersistentData VALUES(?,?)");
            statement.setString(1, location);
            statement.setObject(2, toString(data));

            statement.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(statement);
        }
    }

    @Override
    public boolean has (String location) {

        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            statement = db.prepareStatement("SELECT * FROM PersistentData WHERE KEY = ?");
            statement.setString(1, location);
            results = statement.executeQuery();

            return results.next();
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            close(statement);
            close(results);
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

        PreparedStatement statement = null;
        PreparedStatement insertStatement = null;
        ResultSet results = null;

        try {
            statement = db.prepareStatement("SELECT * FROM PersistentData WHERE KEY = ?");
            statement.setString(1, "VERSION");
            results = statement.executeQuery();

            if(!results.next()) {
                insertStatement = db.prepareStatement("INSERT INTO PersistentData VALUES(?,?)");
                insertStatement.setString(1, "VERSION");
                insertStatement.setInt(2, getCurrentVersion());

                insertStatement.executeUpdate();
            } else {
                return results.getInt(2);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            close(statement);
            close(insertStatement);
            close(results);
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

        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            statement = db.prepareStatement("SELECT * FROM PersistentData");
            results = statement.executeQuery();
            while(results.next()) {
                data.put(results.getString(1), results.getObject(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(statement);
            close(results);
        }
        return data;
    }

    private static Object fromString(String s) throws IOException, ClassNotFoundException {
        byte[] data = s.getBytes();
        BukkitObjectInputStream ois = new BukkitObjectInputStream(new ByteArrayInputStream(data));
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    private static String toString(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return new String(baos.toByteArray());
    }
}