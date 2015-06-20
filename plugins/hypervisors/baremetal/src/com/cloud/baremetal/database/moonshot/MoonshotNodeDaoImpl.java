// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
package com.cloud.baremetal.database.moonshot;

import javax.annotation.PostConstruct;
import javax.ejb.Local;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

@Local(value = {MoonshotNodeDao.class})
@DB()
public class MoonshotNodeDaoImpl extends GenericDaoBase<MoonshotNodeVO, Long> implements MoonshotNodeDao {

    protected SearchBuilder<MoonshotNodeVO> MacAddressSearch;

    public MoonshotNodeDaoImpl() {

    }

    @PostConstruct
    protected void init() {
        MacAddressSearch = createSearchBuilder();
        MacAddressSearch.and("macAddress", MacAddressSearch.entity().getMacAddress(), Op.EQ);
        MacAddressSearch.done();
    }

    @Override
    public MoonshotNodeVO findByMacAddress(String macAddress) {
        SearchCriteria<MoonshotNodeVO> sc = MacAddressSearch.create();
        sc.setParameters("mac_address", macAddress);
        return findOneBy(sc);
    }


}
