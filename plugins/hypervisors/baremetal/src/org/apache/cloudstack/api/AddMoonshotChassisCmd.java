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

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.context.CallContext;
import org.apache.log4j.Logger;

import com.cloud.baremetal.manager.BaremetalManager;
import com.cloud.baremetal.networkservice.MoonshotChassisResponse;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;

/**
 * Created by vaibhav on 1/2/15.
 */
@APICommand(name = "addMoonshotChassis", description = "adds Moonshot Chassis", responseObject = MoonshotChassisResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false, authorized = {RoleType.Admin}) //TODO change type to true
public class AddMoonshotChassisCmd extends BaseAsyncCmd {
    private static final String s_name = "addmoonshotchassisresponse";
    public static final Logger s_logger = Logger.getLogger(AddMoonshotChassisCmd.class);

    @Inject
    private BaremetalManager baremetalManager;

    @Parameter(name="name", required = false, description = "Name of Moonshot Chassis")
    private String name;

    @Parameter(name="url", required = true, description = "URL of Moonshot Chassis")
    private String url;

    @Parameter(name="username", required = true, description = "Username of Moonshot Chassis")
    private String username;

    @Parameter(name="password", required = true, description = "Password of Moonshot Chassis")
    private String password;

    @Parameter(name="addhosts", required = false, description = "Whether to create hosts of Moonshot nodes")
    private String addHosts = "true";

    @Parameter(name="importnodes", required = false, description = "Whether to import Moonshot nodes")
    private String importNodes = "true";

    @Parameter(name="clusterid", required = true, description = "Id of cluster")
    private String clusterId;

    @Parameter(name="hosttag", required = true, description = "Host tag to be given to the hosts created for Moonshot nodes")
    private String hostTag;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getHostTag() {
        return hostTag;
    }

    public void setHostTag(String hostTag) {
        this.hostTag = hostTag;
    }

    public String getAddHosts() {
        return addHosts;
    }

    public void setAddHosts(String addHosts) {
        this.addHosts = addHosts;
    }

    public String getImportNodes() {
        return importNodes;
    }

    public void setImportNodes(String importNodes) {
        this.importNodes = importNodes;
    }

    @Override
    public String getEventType() {
        return "BAREMETAL.CHASSIS.ADD"; //TODO add new event type
    }

    @Override
    public String getEventDescription() {
        return "Adding Moonshot Chassis";
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException, ResourceAllocationException, NetworkRuleConflictException {
        try {
            MoonshotChassisResponse rsp = baremetalManager.addMoonshotChassis(this);
            this.setResponseObject(rsp);
        } catch (Exception e) {
            s_logger.warn(String.format("unable to add Moonshot Chassis [%s]", getUrl()), e);
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
