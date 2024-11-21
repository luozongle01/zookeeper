/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jute.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.jute.compiler.generated.ParseException;
import org.apache.jute.compiler.generated.Rcc;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"unchecked", "SameParameterValue"})
public class JRecordTest {

    @Test
    public void testEndOfLineComments() throws ParseException, NoSuchFieldException, IllegalAccessException {
        String juteStr = "module org.apache.zookeeper.data {\n"
                + "    // information explicitly stored by the server persistently\n"
                + "    class StatPersisted {\n"
                + "        long czxid;      // created zxid\n"
                + "        long mzxid;      // last modified zxid\n"
                + "        long ctime;      // created\n"
                + "        long mtime;      // last modified\n"
                + "    }\n"
                + "}";

        try (StringReader stringReader = new StringReader(juteStr)) {
            Rcc parser = new Rcc(stringReader);
            JFile jFile = parser.Input();
            List<JRecord> mRecords = getField(jFile, "mRecords", List.class);
            assertEquals(1, mRecords.size());

            JRecord jRecord = mRecords.get(0);
            assertEquals("StatPersisted", jRecord.getName());
            List<JField> fields = jRecord.getFields();
            assertFiled(fields);

            assertEquals("\n// information explicitly stored by the server persistently\n", jRecord.getRecordComments());
            assertEquals("\n  // created zxid\n", jRecord.getJavaFieldComments(fields.get(0)));
            assertEquals("\n  // last modified zxid\n", jRecord.getJavaFieldComments(fields.get(1)));
            assertEquals("\n  // created\n", jRecord.getJavaFieldComments(fields.get(2)));
            assertEquals("\n  // last modified\n", jRecord.getJavaFieldComments(fields.get(3)));
        }
    }

    @Test
    public void testCommentBeforeLine() throws ParseException, NoSuchFieldException, IllegalAccessException {
        String juteStr = "module org.apache.zookeeper.data {\n"
                + "    // information explicitly stored by the server persistently\n"
                + "    class StatPersisted {\n"
                + "        // created zxid\n"
                + "        long czxid;\n"
                + "        // last modified zxid\n"
                + "        long mzxid;\n"
                + "        // created\n"
                + "        long ctime;\n"
                + "        // last modified\n"
                + "        long mtime;\n"
                + "    }\n"
                + "}";
        try (StringReader stringReader = new StringReader(juteStr)) {
            Rcc parser = new Rcc(stringReader);
            JFile jFile = parser.Input();
            List<JRecord> mRecords = getField(jFile, "mRecords", List.class);
            assertEquals(1, mRecords.size());

            JRecord jRecord = mRecords.get(0);
            assertEquals("StatPersisted", jRecord.getName());
            List<JField> fields = jRecord.getFields();
            assertFiled(fields);

            assertEquals("\n// information explicitly stored by the server persistently\n", jRecord.getRecordComments());
            assertEquals("\n  // created zxid\n", jRecord.getJavaFieldComments(fields.get(0)));
            assertEquals("\n  // last modified zxid\n", jRecord.getJavaFieldComments(fields.get(1)));
            assertEquals("\n  // created\n", jRecord.getJavaFieldComments(fields.get(2)));
            assertEquals("\n  // last modified\n", jRecord.getJavaFieldComments(fields.get(3)));
        }
    }

    @Test
    public void testMultiLineComments() throws ParseException, NoSuchFieldException, IllegalAccessException {
        String juteStr = "module org.apache.zookeeper.data {\n"
                + "    // information explicitly stored by the server persistently\n"
                + "    class StatPersisted {\n"
                + "        /**\n"
                + "         * created zxid\n"
                + "         */\n"
                + "        long czxid;\n"
                + "        /* last modified zxid */\n"
                + "        long mzxid;\n"
                + "        /*\n"
                + "         * created\n"
                + "         */\n"
                + "        long ctime;\n"
                + "        /*\n"
                + "         last modified\n"
                + "         */"
                + "        long mtime;\n"
                + "    }\n"
                + "}";
        try (StringReader stringReader = new StringReader(juteStr)) {
            Rcc parser = new Rcc(stringReader);
            JFile jFile = parser.Input();
            List<JRecord> mRecords = getField(jFile, "mRecords", List.class);
            assertEquals(1, mRecords.size());

            JRecord jRecord = mRecords.get(0);
            assertEquals("StatPersisted", jRecord.getName());
            List<JField> fields = jRecord.getFields();
            assertFiled(fields);

            assertEquals("\n// information explicitly stored by the server persistently\n", jRecord.getRecordComments());
            assertEquals("\n  /**\n  * created zxid\n  */\n", jRecord.getJavaFieldComments(fields.get(0)));
            assertEquals("\n  /* last modified zxid */\n", jRecord.getJavaFieldComments(fields.get(1)));
            assertEquals("\n  /*\n  * created\n  */\n", jRecord.getJavaFieldComments(fields.get(2)));
            assertEquals("\n  /*\n  last modified\n  */\n", jRecord.getJavaFieldComments(fields.get(3)));
        }
    }

    private void assertFiled(List<JField> fields) {
        assertEquals(4, fields.size());
        assertEquals("long", fields.get(0).getType().getJavaType());
        assertEquals("czxid", fields.get(0).getName());
        assertEquals("long", fields.get(1).getType().getJavaType());
        assertEquals("mzxid", fields.get(1).getName());
        assertEquals("long", fields.get(2).getType().getJavaType());
        assertEquals("ctime", fields.get(2).getName());
        assertEquals("long", fields.get(3).getType().getJavaType());
        assertEquals("mtime", fields.get(3).getName());
    }

    private <T> T getField(final Object target,
                           final String fieldName,
                           final Class<T> fieldClassType) throws NoSuchFieldException, IllegalAccessException {
        Class<?> targetClazz = target.getClass();
        Field field = targetClazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return fieldClassType.cast(field.get(target));
    }
}
