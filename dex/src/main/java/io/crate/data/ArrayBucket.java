/*
 * Licensed to Crate.io Inc. (Crate) under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file to
 * you under the Apache License, Version 2.0 (the "License");  you may not
 * use this file except in compliance with the License.  You may obtain a
 * copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, to use any modules in this file marked as "Enterprise Features",
 * Crate must have given you permission to enable and use such Enterprise
 * Features and you must have a valid Enterprise or Subscription Agreement
 * with Crate.  If you enable or use the Enterprise Features, you represent
 * and warrant that you have a valid Enterprise or Subscription Agreement
 * with Crate.  Your use of the Enterprise Features if governed by the terms
 * and conditions of your Enterprise or Subscription Agreement with Crate.
 */

package io.crate.data;

import com.google.common.base.MoreObjects;

import java.util.Iterator;

public class ArrayBucket implements Bucket {

    private final Object[][] rows;
    private final int numColumns;


    /**
     * Constructs a new ArrayBucket with rows of the given size, regardless of what length the row arrays is.
     *
     * @param rows       the backing array of this bucket
     * @param numColumns the size of rows emitted from this bucket
     */
    public ArrayBucket(Object[][] rows, int numColumns) {
        this.rows = rows;
        this.numColumns = numColumns;
    }

    public ArrayBucket(Object[][] rows) {
        this.rows = rows;
        if (rows.length > 0) {
            numColumns = rows[0].length;
        } else {
            numColumns = -1;
        }
    }

    @Override
    public int size() {
        return rows.length;
    }

    @Override
    public Iterator<Row> iterator() {
        return new Iterator<Row>() {
            int pos = 0;
            final RowN row = new RowN(numColumns);

            @Override
            public boolean hasNext() {
                return pos < rows.length;
            }

            @Override
            public Row next() {
                row.cells(rows[pos++]);
                return row;
            }

            @Override
            public void remove() {

            }
        };
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("numRows", size())
            .add("numColumns", numColumns)
            .toString();
    }
}