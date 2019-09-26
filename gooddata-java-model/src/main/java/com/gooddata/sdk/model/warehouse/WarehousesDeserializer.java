/*
 * Copyright (C) 2004-2019, GoodData(R) Corporation. All rights reserved.
 * This source code is licensed under the BSD-style license found in the
 * LICENSE.txt file in the root directory of this source tree.
 */
package com.gooddata.sdk.model.warehouse;

import com.gooddata.collections.PageDeserializer;
import com.gooddata.collections.Paging;

import java.util.List;
import java.util.Map;

class WarehousesDeserializer extends PageDeserializer<Warehouses, Warehouse> {

    protected WarehousesDeserializer() {
        super(Warehouse.class);
    }

    @Override
    protected Warehouses createPage(final List<Warehouse> items, final Paging paging, final Map<String, String> links) {
        return new Warehouses(items, paging, links);
    }
}
