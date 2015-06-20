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
package com.cloud.baremetal.networkservice;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.cloud.baremetal.database.moonshot.MoonshotChassisVO;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vaibhav on 1/4/15.
 */
@EntityReference(value = MoonshotChassisVO.class)
public class MoonshotChassisResponse extends BaseResponse {
    @SerializedName(ApiConstants.UUID)
    @Param(description = "uuid of Moonshot Chassis")
    private String uuid;

    @SerializedName(ApiConstants.URL)
    @Param(description = "url")
    private String url;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "name")
    private String name;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
