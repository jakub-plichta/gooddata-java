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

/**
 * Deserializer of JSON into warehouse users object.
 */
class WarehouseUsersDeserializer extends PageDeserializer<WarehouseUsers, WarehouseUser> {

    protected WarehouseUsersDeserializer() {
        super(WarehouseUser.class);
    }

    @Override
    protected WarehouseUsers createPage(final List<WarehouseUser> items, final Paging paging, final Map<String, String> links) {
        return new WarehouseUsers(items, paging, links);
    }
}
