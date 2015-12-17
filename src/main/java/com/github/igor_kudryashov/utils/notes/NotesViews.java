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

package com.github.igor_kudryashov.utils.notes;

import java.util.HashMap;
import java.util.Map;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.View;

/**
 * NotesViews class provides a storage of Notes Views. This storage allows not
 * open the view again if you already use it in the your application that
 * accelerates the your application.
 * 
 * @author Igor Kudryashov
 *
 */
public class NotesViews {

	private static volatile NotesViews instance;
	private Map<String, View> views;

	//singleton
	public static NotesViews getInstance() {
		if (instance == null) {
			synchronized (NotesViews.class) {
				NotesViews inst = instance;
				if (inst == null) {
					synchronized (NotesViews.class) {
						inst = new NotesViews();
					}
					instance = inst;
				}
			}
		}
		return instance;
	}

	/**
	 * Private constructor for singleton class. 
	 */
	private NotesViews() {
		views = new HashMap<String, View>();
	}

	/**
	 * Return lotus.domino.View object by lotus.domino.Database object and view
	 * name.
	 * 
	 * @param database
	 *            - lotus.domino.Database object
	 * @param viewName
	 *            - The case-insensitive name of a view or folder in a database.
	 *            Use either the entire name of the view or folder (including
	 *            backslashes for cascading views and folders), or an alias.
	 * @return lotus.domino.View object.
	 * @throws NotesException
	 */
	public View getView(Database database, String viewName) throws NotesException {
		View view = null;
		if (database.isOpen()) {
			String key = database.getReplicaID() + "." + viewName;
			if (views.containsKey(key)) {
				// if view already exist in storage then get it.
				view = views.get(key);
				key = database.getReplicaID() + "." + view.getName();
				if (!views.containsKey(key)) {
					views.put(key, view);
				}
			} else {
				view = database.getView(viewName);
				// add new view into storage
				views.put(key, view);
				key = database.getReplicaID() + "." + view.getName();
				if (!views.containsKey(key)) {
					views.put(key, view);
				}
			}
		}
		return view;
	}

	/**
	 * The recycle method unconditionally destroys all views in storage and
	 * returns its memory to the system
	 * @throws NotesException
	 */
	public synchronized void recycle() throws NotesException {
		if (views != null) {
			for (Map.Entry<String, View> entry : views.entrySet()) {
				View view = entry.getValue();
				if (view != null) {
					view.recycle();
				}
			}
		}
	}
}
