package com.github.igor_kudryashov.notesutils;

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

import java.util.Comparator;

import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;

/**
 * This class can be used for sorting the documents in the collection, depending on the field values of
 * documents.
 * 
 * @author Igor Kudryashov
 *
 */
public class DocumentComparator implements Comparator<Document> {

	String[] sortFields = null;

	/**
	 * @param sortFields
	 *            - an array of field names for document comparison
	 */
	public DocumentComparator(String[] sortFields) {
		this.sortFields = sortFields;
	}

	/**
	 * Compares two documents on fields values
	 * 
	 * @param doc1
	 *            - First document
	 * @param doc2
	 *            - Seconf document
	 * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
	 *         than the second.
	 */
	public int compare(Document doc1, Document doc2) {

		try {
			int compared = 0;
			// loop all sortFields
			for (String field : sortFields) {
				Item item1 = doc1.getFirstItem(field);
				Item item2 = doc2.getFirstItem(field);
				switch (item1.getType()) {
				case Item.TEXT:
				case Item.AUTHORS:
				case Item.NAMES:
				case Item.READERS:
					String val1 = doc1.getItemValueString(field);
					String val2 = doc2.getItemValueString(field);
					compared = val1.compareTo(val2);
					if (0 != compared) {
						item1.recycle();
						item2.recycle();
						return compared;
					}
					break;
				case Item.NUMBERS:
					Double d1 = doc1.getItemValueDouble(field);
					Double d2 = doc2.getItemValueDouble(field);
					compared = d1.compareTo(d2);
					if (0 != compared) {
						item1.recycle();
						item2.recycle();
						return compared;
					}
					break;
				case Item.DATETIMES:
					DateTime dt1 = item1.getDateTimeValue();
					DateTime dt2 = item2.getDateTimeValue();
					compared = dt2.timeDifference(dt1);
					if (0 != compared) {
						dt1.recycle();
						dt2.recycle();
						item1.recycle();
						item2.recycle();
						return compared;
					}
					break;
				}
				item1.recycle();
				item2.recycle();
			}
			return 0;
		} catch (NotesException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
