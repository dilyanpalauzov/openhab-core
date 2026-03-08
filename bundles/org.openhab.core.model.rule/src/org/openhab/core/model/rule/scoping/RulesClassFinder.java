/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.model.rule.scoping;

import org.eclipse.xtext.common.types.access.impl.ClassFinder;
import org.eclipse.xtext.common.types.access.impl.ClassNameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a customized version of the {@link ClassFinder}.
 *
 * It allows for removing and updating classes in the cache when add-ons are installed or updated.
 *
 * @author Wouter Born - Initial contribution
 */
public class RulesClassFinder extends ClassFinder {

    private static class Null {
    }

    private static final Class<?> NULL_CLASS = Null.class;

    private final ClassLoader classLoader;
    private final ClassNameUtil classNameUtil = new ClassNameUtil();
    private final Logger logger = LoggerFactory.getLogger(RulesClassFinder.class);

    protected RulesClassFinder(ClassLoader classLoader) {
        super(classLoader);
        this.classLoader = classLoader;
    }

    @Override
    public Class<?> forName(String name) throws ClassNotFoundException {
        RulesClassCache cache = RulesClassCache.getInstance();
        Class<?> result = cache.get(name);
        if (result != null) {
            if (result == NULL_CLASS) {
                logger.error("CLASS {} NOT FOUND IN CACHE, returning CACHED_EXCEPTION", name);
                throw CACHED_EXCEPTION;
            }
            return result;
        }

        try {
            result = forName(classNameUtil.normalizeClassName(name), classLoader);
            cache.put(name, result);
            return result;
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundExcetion for {} - this is now NULL_CLASS", name);
            cache.put(name, NULL_CLASS);
            throw e;
        }
    }
}
