/*
 * This Source Code Form is subject to the terms of the MIT License.
 * Copyright (c) 2023 Draegerwerk AG & Co. KGaA.
 *
 * SPDX-License-Identifier: MIT
 */

package com.draeger.medical.sdccc.util;

import com.draeger.medical.sdccc.messages.HibernateConfigBase;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.UUID;

/**
 * Hibernate configuration using an in memory database.
 */
@Singleton
public class HibernateConfigInMemoryImpl extends HibernateConfigBase {
    @Inject
    HibernateConfigInMemoryImpl() {
        super("memory:" + UUID.randomUUID().toString());
    }
}
