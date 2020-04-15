/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.util;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

import java.sql.*;
import java.util.UUID;

public final class UUIDMappings {

    private Connection db;

    public void enable() {
        createConnection();
    }

    private void createConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            db = DriverManager.getConnection("jdbc:sqlite:plugins/CraftBook/uuid-mappings.db");

            DatabaseMetaData dbm = db.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "mappings", null);

            if(!tables.next()) { //We already have something in this table, don't create it!
                String createTable = "CREATE TABLE mappings (UUID CHAR(36) PRIMARY KEY, CBID CHAR(6) UNIQUE)";
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

    private static void close(ResultSet results) {

        if(results != null) {
            try {
                results.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void close(PreparedStatement statement) {

        if(statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the UUID based on a CraftBook ID.
     *
     * @param cbID The CraftBook ID.
     * @return The UUID, if any. (Can return null)
     */
    public UUID getUUID(String cbID) {

        UUID uuid = null;

        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            statement = db.prepareStatement("SELECT UUID FROM mappings WHERE CBID = ?");
            statement.setString(1, cbID);

            results = statement.executeQuery();

            if(!results.next())
                return null;

            uuid = UUID.fromString(results.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(statement);
            close(results);
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

        PreparedStatement statement = null;
        PreparedStatement insertStatement = null;
        ResultSet results = null;

        try {
            statement = db.prepareStatement("SELECT CBID FROM mappings WHERE UUID = ?");
            statement.setString(1, uuid.toString());

            results = statement.executeQuery();

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

                    close(results);
                    results = statement.executeQuery();

                    if(!results.next()) {
                        foundOne = true;
                    } else
                        continue;

                    insertStatement = db.prepareStatement("INSERT INTO mappings VALUES(?,?)");
                    insertStatement.setString(1, uuid.toString());
                    insertStatement.setString(2, cbId);

                    insertStatement.executeUpdate();
                }

                return cbId;
            }

            cbId = results.getString(1);
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            close(statement);
            close(insertStatement);
            close(results);
        }

        return cbId;
    }

    public void disable() {
        try {
            if(db != null && !db.isClosed())
                db.close();
        } catch(SQLException ignored){}
    }
}