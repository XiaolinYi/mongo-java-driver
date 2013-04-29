/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeMapper {
    private static final List<String> EMPTY_PATH = Collections.emptyList();

    private final Map<List<String>, Class<? extends DBObject>> pathToClassMap;
    private final ReflectionDBObject.JavaWrapper wrapper;

    public TypeMapper(final Class<? extends DBObject> topLevelClass) {
        this(topLevelClass, new HashMap<String, Class<? extends DBObject>>());
    }

    public TypeMapper(final Class<? extends DBObject> topLevelClass,
                      final Map<String, Class<? extends DBObject>> stringPathToClassMap
    ) {
        this.pathToClassMap = createPathToClassMap(topLevelClass, stringPathToClassMap);

        if (ReflectionDBObject.class.isAssignableFrom(topLevelClass)) {
            wrapper = ReflectionDBObject.getWrapper(topLevelClass);
        } else {
            wrapper = null;
        }
    }

    public Class<? extends DBObject> getTopLevelClass() {
        return pathToClassMap.get(EMPTY_PATH);
    }

    public Map<List<String>, Class<? extends DBObject>> getPathToClassMap() {
        return pathToClassMap;
    }

    public DBObject getNewInstance(final List<String> path) {
        Class<? extends DBObject> newInstanceClass = null;
        try {
            newInstanceClass = pathToClassMap.get(path);
            if (newInstanceClass == null) {
                if (wrapper != null) {
                    newInstanceClass = wrapper.getInternalClass(path);
                }
                if (newInstanceClass == null) {
                    newInstanceClass = BasicDBObject.class;
                }
            }
            return newInstanceClass.newInstance();
        } catch (InstantiationException e) {
            throw new MongoInternalException("can't create a new instance of class " + newInstanceClass, e);
        } catch (IllegalAccessException e) {
            throw new MongoInternalException("can't create a new instance of class " + newInstanceClass, e);
        }
    }

    private Map<List<String>, Class<? extends DBObject>> createPathToClassMap(
            final Class<? extends DBObject> topLevelClass,
            final Map<String, Class<? extends DBObject>> stringPathToClassMap
    ) {
        final Map<List<String>, Class<? extends DBObject>> pathToClassMap
                = new HashMap<List<String>, Class<? extends DBObject>>();
        pathToClassMap.put(EMPTY_PATH, topLevelClass);
        for (final Map.Entry<String, Class<? extends DBObject>> cur : stringPathToClassMap.entrySet()) {
            final List<String> path = Arrays.asList(cur.getKey().split("\\."));
            pathToClassMap.put(path, cur.getValue());
        }

        return Collections.unmodifiableMap(pathToClassMap);
    }

}