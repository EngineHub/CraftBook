package com.sk89q.craftbook.common;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class UUIDMappings {

    Connection db;

    public void enable() {

        createConnection();
    }

    public boolean areMappingsValid() {

        return db != null;
    }

    private void createConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            db = DriverManager.getConnection("jdbc:sqlite:plugins/CraftBook/uuid-mappings.db");

            DatabaseMetaData dbm = db.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "mappings", null);

            if(tables.next()) //We already have something in this table, don't create it!
                return;
            else {
                String createTable = "CREATE TABLE mappings (UUID CHAR(36) PRIMARY KEY, CBID CHAR(6) UNIQUE)";
                try {
                    Statement state = db.createStatement();
                    state.executeUpdate(createTable);
                } catch(SQLException e) {}
            }
        }
        catch(Exception e) {}
    }

    /**
     * Gets the UUID based on a CraftBook ID.
     * 
     * @param cbID The CraftBook ID.
     * @return The UUID, if any. (Can return null)
     */
    public UUID getUUID(String cbID) {

        UUID uuid = null;

        try {
            PreparedStatement statement = db.prepareStatement("SELECT UUID FROM mappings WHERE CBID = ?");
            statement.setString(1, cbID);

            ResultSet results = statement.executeQuery();

            if(!results.next())
                return uuid;

            uuid = UUID.fromString(results.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return uuid;
    }

    /**
     * Gets the CraftBook ID based off a UUID. This will create if necessary.
     * 
     * @param uuid The UUID.
     * @return The CB ID.
     */
    public String getCBID(UUID uuid) {

        String cbId = null;

        try {
            PreparedStatement statement = db.prepareStatement("SELECT CBID FROM mappings WHERE UUID = ?");
            statement.setString(1, uuid.toString());

            ResultSet results = statement.executeQuery();

            if(!results.next()) {

                //We need to generate one.
                boolean foundOne = false;
                while(!foundOne) {
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < 6; i++)
                        sb.append(Integer.toHexString(CraftBookPlugin.inst().getRandom().nextInt(16)));

                    sb.setLength(6); //Just makin' sure.

                    cbId = sb.toString();

                    statement = db.prepareStatement("SELECT UUID FROM mappings WHERE CBID = ?");
                    statement.setString(1, cbId);

                    results = statement.executeQuery();

                    if(!results.next()) {
                        foundOne = true;
                    } else
                        continue;

                    PreparedStatement insertStatement = db.prepareStatement("INSERT INTO mappings VALUES(?,?)");
                    insertStatement.setString(1, uuid.toString());
                    insertStatement.setString(2, cbId);

                    insertStatement.executeUpdate();
                }

                return cbId;
            }

            cbId = results.getString(1);
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return cbId;
    }

    public void disable() {

        try {
            if(db != null && !db.isClosed())
                db.close();
        } catch(SQLException e){}
    }
}