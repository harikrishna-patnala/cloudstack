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

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

@Entity
@Table(name = "moonshot_nodes")
public class MoonshotNodeVO implements InternalIdentity, Identity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "uuid")
    private String uuid  = UUID.randomUUID().toString();

    @Column(name = "moonshot_chassis_id")
    private long moonshotChassisId;

    @Column(name = "host_id")
    private Long hostId;

    @Column(name = "cartridge")
    private String cartridge;

    @Column(name = "node")
    private String node;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "secondary_mac_address")
    private String secondaryMacAddress;

    @Column(name = "no_of_cores")
    private Integer noOfCores;

    @Column(name = "memory")
    private Integer memory;

    @Column(name = "max_clock_speed")
    private Integer maxClockSpeed;



    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getMoonshotChassisId() {
        return moonshotChassisId;
    }

    public void setMoonshotChassisId(long moonshotChassisId) {
        this.moonshotChassisId = moonshotChassisId;
    }

    public String getCartridge() {
        return cartridge;
    }

    public void setCartridge(String cartridge) {
        this.cartridge = cartridge;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getSecondaryMacAddress() {
        return secondaryMacAddress;
    }

    public void setSecondaryMacAddress(String secondaryMacAddress) {
        this.secondaryMacAddress = secondaryMacAddress;
    }

    public Integer getNoOfCores() {
        return noOfCores;
    }

    public void setNoOfCores(Integer noOfCores) {
        this.noOfCores = noOfCores;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public Integer getMaxClockSpeed() {
        return maxClockSpeed;
    }

    public void setMaxClockSpeed(Integer maxClockSpeed) {
        this.maxClockSpeed = maxClockSpeed;
    }

    public String getShortName() {
        return "C" + this.cartridge + "N" + this.node;
    }


}
