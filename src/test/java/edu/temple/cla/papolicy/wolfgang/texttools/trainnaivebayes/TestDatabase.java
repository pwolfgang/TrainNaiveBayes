/*
 * Copyright (c) 2018, Temple University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * All advertising materials features or use of this software must display 
 *   the following  acknowledgement
 *   This product includes software developed by Temple University
 * * Neither the name of the copyright holder nor the names of its 
 *   contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.temple.cla.papolicy.wolfgang.texttools.trainnaivebayes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to create a test database.
 * @author Paul
 */
public class TestDatabase {

    public static void createTestTable() throws RuntimeException {
        try (
                final Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/testdb", "paul", "secret"); 
                final Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS TestTable");
            String query = "CREATE TABLE TestTable (ID varchar(15) primary key, Abstract varchar(32670), Code int)";
            stmt.executeUpdate(query);
            String enterData = "INSERT INTO TestTable values " 
                    + "('20151HB0001', 'The quick brown fox jumps over the lazy dog', 1200)," 
                    + "('20151HB0002', 'Now is the time for all good men to come to the aid of the party', 1000)," 
                    + "('20151HB0003', 'The slow brown fox cannot jump over the fast dog.', 1200)," 
                    + "('20151HB0004', 'It''s party time!', 1000)";
            stmt.executeUpdate(enterData);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<Map<String, Object>> buildExpectedResult() {
        Map<String, Object> amap;
        List<Map<String, Object>> list = new ArrayList<>();
        amap = new LinkedHashMap<>();
        amap.put("ID", "20151HB0001");
        amap.put("Abstract", "The quick brown fox jumps over the lazy dog");
        amap.put("Code", 1200);
        list.add(amap);
        amap = new LinkedHashMap<>();
        amap.put("ID", "20151HB0002");
        amap.put("Abstract", "Now is the time for all good men to come to the aid of the party");
        amap.put("Code", 1000);
        list.add(amap);
        amap = new LinkedHashMap<>();
        amap.put("ID", "20151HB0003");
        amap.put("Abstract", "The slow brown fox cannot jump over the fast dog.");
        amap.put("Code", 1200);
        list.add(amap);
        amap = new LinkedHashMap<>();
        amap.put("ID", "20151HB0004");
        amap.put("Abstract", "It's party time!");
        amap.put("Code", 1000);
        list.add(amap);
        return list;
    }

    public static List<Map<String, Object>> buildExpectedResultMinorCode() {
        Map<String, Object> amap;
        List<Map<String, Object>> list = new ArrayList<>();
        amap = new LinkedHashMap<>();
        amap.put("theID", "20151HB0001");
        amap.put("theText", "The quick brown fox jumps over the lazy dog");
        amap.put("theCode", 1200);
        list.add(amap);
        amap = new LinkedHashMap<>();
        amap.put("theID", "20151HB0002");
        amap.put("theText", "Now is the time for all good men to come to the aid of the party");
        amap.put("theCode", 1000);
        list.add(amap);
        amap = new LinkedHashMap<>();
        amap.put("theID", "20151HB0003");
        amap.put("theText", "The slow brown fox cannot jump over the fast dog.");
        amap.put("theCode", 1200);
        list.add(amap);
        amap = new LinkedHashMap<>();
        amap.put("theID", "20151HB0004");
        amap.put("theText", "It's party time!");
        amap.put("theCode", 1000);
        list.add(amap);
        return list;
    }

    public static List<Map<String, Object>> buildExpectedResultMajorCode() {
        Map<String, Object> amap;
        List<Map<String, Object>> list = new ArrayList<>();
        amap = new LinkedHashMap<>();
        amap.put("theID", "20151HB0001");
        amap.put("theText", "The quick brown fox jumps over the lazy dog");
        amap.put("theCode", 12);
        list.add(amap);
        amap = new LinkedHashMap<>();
        amap.put("theID", "20151HB0002");
        amap.put("theText", "Now is the time for all good men to come to the aid of the party");
        amap.put("theCode", 10);
        list.add(amap);
        amap = new LinkedHashMap<>();
        amap.put("theID", "20151HB0003");
        amap.put("theText", "The slow brown fox cannot jump over the fast dog.");
        amap.put("theCode", 12);
        list.add(amap);
        amap = new LinkedHashMap<>();
        amap.put("theID", "20151HB0004");
        amap.put("theText", "It's party time!");
        amap.put("theCode", 10);
        list.add(amap);
        return list;
    }

}
