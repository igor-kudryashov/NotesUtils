/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package com.github.igor_kudryashov.notesutils;

import java.util.Vector;

import org.apache.commons.lang.mutable.MutableDouble;

import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;

/**
 * NotesDocument class provides highly reusable static utility methods with adding value to the
 * lotus.domino Document.
 *
 * @author Igor Kudryashov
 */
public class NotesDocumentUtils {

    /**
     * Private default constructor for prevent of create instance of class 
     */
	private NotesDocumentUtils() {
    }

    /**
     * For a date-time item, returns a java.util.Date object representing the value of the item. For
     * items of other types, returns null.
     *
     * @param document
     *            - the Lotus Notes document.
     * @param item
     *            - the item of Lotus Notes document.
     * @return java.util.Date value of the item.
     * @throws NotesException
     */
    public static java.util.Date getItemValueDate(Document document, String item) throws NotesException {
        java.util.Date date = null;
        if (document != null) {
            Item it = document.getFirstItem(item);
            if (it != null) {
                date = getItemValueDate(it);
                it.recycle();
            }
        }
        return date;
    }

    /**
     * For a date-time item, returns a java.util.Date object representing the value of the item. For
     * items of other types, returns null.
     *
     * @param item
     *            - the item of Lotus Notes document.
     * @return java.util.Date value of the item.
     * @throws NotesException
     */
    public static java.util.Date getItemValueDate(Item item) throws NotesException {
        java.util.Date date = null;
        if (item != null) {
            if (item.getType() == Item.DATETIMES) {
                DateTime datetime = item.getDateTimeValue();
                if (datetime != null) {
                    if (datetime.getTimeOnly().length() < 1) {
                        // if lotus.domino.DateTime not contains a time part,
                        // you will get a java.util.Date with the current time.
                        // Fix it.
                        datetime.setLocalTime(datetime.getDateOnly() + " 00:00:00");
                    }
                    date = datetime.toJavaDate();
                    datetime.recycle();
                }
            }
        }
        return date;
    }

    /**
     * As lotus.domino.Document.replaceItemValue() method modifies the value of the document Lotus
     * Notes, but only if the new value is different from existing
     *
     * @param document
     *            - the Notes document.
     * @param item
     *            - the name of Notes item.
     * @param value
     *            - the new value of Notes item.
     * @return <code>true</code> if the value has been updated, <code>false</code> otherwise.
     * @throws NotesException
     */

    public static boolean updateItemValue(Document document, String item, Object value) throws NotesException {
        boolean ret;
        if (document.hasItem(item)) {
            Item it = document.getFirstItem(item);
            ret = updateItemValue(it, value);
            it.recycle();
        } else {
            document.replaceItemValue(item, value);
            ret = true;
        }
        return ret;
    }

    /**
     * As lotus.domino.Document.replaceItemValue() method modifies the value of the document Lotus
     * Notes, but only if the new value is different from existing
     *
     * @param item
     *            - the lotus.Domino.Item object.
     * @param value
     *            - the new value of Notes item.
     * @return <code>true</code> if the value has been updated, <code>false</code> otherwise.
     * @throws NotesException
     */
    @SuppressWarnings("unchecked")
    public static boolean updateItemValue(Item item, Object value) throws NotesException {
        Vector<Object> vec = item.getValues();
        if (value.getClass().getName().contains("Vector")) {
            if (vec.equals(value)) {
                return false;
            } else {
                item.setValues((Vector<Object>) value);
            }
        } else {
            if (vec.size() == 1) {
                if (vec.firstElement() instanceof Number) {
                    // because lotus.docmino.Item.getValues() alvays return java.util.Vector with
                    // Double elements for Numeric items,
                    // value parameter must be converted to Double
                    MutableDouble md = new MutableDouble((Number) value);
                    if (Double.compare((Double) vec.firstElement(), (Double) md.getValue()) == 0) {
                        return false;
                    }
                } else if (vec.firstElement() instanceof String) {
                    if (vec.firstElement().equals((String) value)) {
                        return false;
                    }
                } else {
                    if (vec.firstElement().equals(value)) {
                        return false;
                    }
                }
            }
            vec = new Vector<Object>();
            vec.add(value);
            item.setValues(vec);
        }

        return true;

    }

    /**
     * Returns a parent document of the specified document.
     *
     * @param document
     *            - the specified document.
     * @return the parent document or <code>null</code> if parent document not found.
     * @throws NotesException
     */
    public static Document getParentDocument(final Document document) throws NotesException {
        Document doc = null;
        if (document.isResponse()) {
            doc = document.getParentDatabase().getDocumentByUNID(document.getParentDocumentUNID());
        }
        return doc;
    }

    /**
     * Indicates whether a document is new. A document is new if it has not been saved.
     *
     * @param document
     *            - the Lotus Notes document.
     * @return <code>true</code> if the document was created, but has not been saved and
     *         <code>false</code> if the document has been saved
     * @throws NotesException
     */

    public static boolean isNewNote(Document document) throws NotesException {
        return "NT00000000".equals(document.getNoteID());
    }
}