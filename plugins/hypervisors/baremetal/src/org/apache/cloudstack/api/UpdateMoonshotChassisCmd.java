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
package org.apache.cloudstack.api;

import com.cloud.baremetal.manager.BaremetalManager;
import com.cloud.baremetal.networkservice.MoonshotChassisResponse;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.context.CallContext;
import org.apache.log4j.Logger;

import javax.inject.Inject;

/**
 * Created by Raghav on 1/2/15.
 */
@APICommand(name = "updateMoonshotChassis", description = "updates Moonshot Chassis", responseObject = MoonshotChassisResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false, authorized = {RoleType.Admin}) //TODO change type to true
public class UpdateMoonshotChassisCmd extends BaseAsyncCmd {
    private static final String s_name = "updatemoonshotchassisresponse";
    public static final Logger s_logger = Logger.getLogger(UpdateMoonshotChassisCmd.class);

    @Inject
    private BaremetalManager baremetalManager;

    @Parameter(name="chassisid", required = true, description = "UUID of Moonshot Chassis")
    private String chassisId;

    @Parameter(name="password", required = true, description = "Password of Moonshot Chassis")
    private String password;

    @Parameter(name="hosttag", required = true, description = "Host tag to be given to the hosts created for Moonshot nodes")
    private String hostTag;


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHostTag() {
        return hostTag;
    }

    public void setHostTag(String hostTag) {
        this.hostTag = hostTag;
    }

    public String getChassisId() {
        return chassisId;
    }

    public void setChassisId(String chassisId) {
        this.chassisId = chassisId;
    }

    @Override
    public String getEventType() {
        return "BAREMETAL.CHASSIS.UPDATE"; //TODO add new event type
    }

    @Override
    public String getEventDescription() {
        return "Updating Moonshot Chassis";
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException, ResourceAllocationException, NetworkRuleConflictException {
        try {
            MoonshotChassisResponse rsp = baremetalManager.updateMoonshotChassis(this);
            this.setResponseObject(rsp);
        } catch (Exception e) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return CallContext.current().getCallingAccount().getId();
    }
}
