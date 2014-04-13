package com.sk89q.craftbook.util.persistent;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
            ResultSet tables = dbm.getTables(null, null, "persistent-data", null);

            if(tables.next()) //We already have something in this table, don't create it!
                return;
            else {
                String createTable = "CREATE TABLE persistent-data (KEY VARCHAR(255) PRIMARY KEY, VALUE TEXT, TYPE VARCHAR(16))";
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
            PreparedStatement statement = db.prepareStatement("SELECT * FROM persistent-data WHERE KEY = ?");
            statement.setString(1, location);
            ResultSet results = statement.executeQuery();

            if(!results.next()) return null;

            String dataType = results.getString(3).toUpperCase();
            if(dataType.equals("INTEGER"))
                return results.getInt(2);
            else if(dataType.equals("STRING"))
                return results.getString(2);
            else if(dataType.equals("DOUBLE"))
                return results.getDouble(2);
            else if(dataType.equals("BOOLEAN"))
                return results.getBoolean(2);
            else if(dataType.equals("SET"))
                return new HashSet<String>(Arrays.asList(results.getString(2).split("`")));
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void set (String location, Object data) {

        try {
            PreparedStatement statement = db.prepareStatement("INSERT INTO persistent-data VALUES(?,?,?)");
            statement.setString(1, location);
            statement.setObject(2, data);
            String dataType = null;

            if(data instanceof Integer)
                dataType = "INTEGER";
            else if(data instanceof String)
                dataType = "STRING";
            else if(data instanceof Double)
                dataType = "DOUBLE";
            else if(data instanceof Boolean)
                dataType = "BOOLEAN";
            else if(data instanceof Set)
                dataType = "SET";

            statement.setString(3, dataType);
            statement.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean has (String location) {

        try {
            PreparedStatement statement = db.prepareStatement("SELECT * FROM persistent-data WHERE KEY = ?");
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
            return db.isValid(3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getVersion () {

        try {
            PreparedStatement statement = db.prepareStatement("SELECT * FROM persistent-data WHERE KEY = ?");
            statement.setString(1, "VERSION");
            ResultSet results = statement.executeQuery();

            if(!results.next()) {
                statement = db.prepareStatement("INSERT INTO persistent-data VALUES(?,?,?)");
                statement.setString(1, "VERSION");
                statement.setInt(2, getCurrentVersion());
                statement.setString(3, "INTEGER");
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
                db.prepareStatement("DELETE FROM persistent-data").execute();
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
            PreparedStatement statement = db.prepareStatement("SELECT * FROM persistent-data");
            ResultSet results = statement.executeQuery();
            while(results.next()) {
                data.put(results.getString(1), results.getObject(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
}