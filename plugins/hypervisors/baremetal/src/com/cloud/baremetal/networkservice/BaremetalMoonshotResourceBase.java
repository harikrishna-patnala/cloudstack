package com.cloud.baremetal.networkservice;


import citrix.moonshot.MoonshotClient;
import citrix.moonshot.enums.BootTarget;
import citrix.moonshot.enums.ResetType;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingRoutingCommand;
import com.cloud.agent.api.RebootAnswer;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.SecurityGroupRulesCmd;
import com.cloud.agent.api.StartAnswer;
import com.cloud.agent.api.StartCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.MigrateCommand;
import com.cloud.agent.api.MigrateAnswer;
import com.cloud.agent.api.StopAnswer;
import com.cloud.agent.api.StopCommand;
import com.cloud.agent.api.baremetal.IpmISetBootDevCommand;
import com.cloud.agent.api.baremetal.IpmiBootorResetCommand;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.baremetal.manager.BaremetalManager;
import com.cloud.configuration.Config;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.log4j.Logger;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Map;

public class BaremetalMoonshotResourceBase extends BareMetalResourceBase {
    private static final Logger s_logger = Logger.getLogger(BaremetalMoonshotResourceBase.class);
    protected String _cartridgeNodeLocation;
    protected MoonshotClient _moonshotClient;

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        s_logger.debug("Configuring Baremetal resource. Mac:" + (String) params.get(ApiConstants.HOST_MAC) + " CartridgeNodeLocation:" + (String) params.get(BaremetalManager.CartridgeNodeLocation));
        _name = name;
        _uuid = (String) params.get("guid");
        try {
            _memCapacity = Long.parseLong((String) params.get(ApiConstants.MEMORY)) * 1024L * 1024L;
            _cpuCapacity = Long.parseLong((String) params.get(ApiConstants.CPU_SPEED));
            _cpuNum = Long.parseLong((String) params.get(ApiConstants.CPU_NUMBER));
        } catch (NumberFormatException e) {
            throw new ConfigurationException(String.format("Unable to parse number of CPU or memory capacity "
                            + "or cpu capacity(cpu number = %1$s memCapacity=%2$s, cpuCapacity=%3$s", params.get(ApiConstants.CPU_NUMBER),
                    params.get(ApiConstants.MEMORY), params.get(ApiConstants.CPU_SPEED)));
        }

        _zone = (String) params.get("zone");
        _pod = (String) params.get("pod");
        _cluster = (String) params.get("cluster");
        hostId = (Long) params.get("hostId");
        _ip = (String) params.get(ApiConstants.PRIVATE_IP);
        _mac = (String) params.get(ApiConstants.HOST_MAC);
        _username = (String) params.get(ApiConstants.USERNAME);
        _password = (String) params.get(ApiConstants.PASSWORD);
        _vmName = (String) params.get("vmName");
        _cartridgeNodeLocation = (String) params.get(BaremetalManager.CartridgeNodeLocation);
        //_moonshotClient = new MoonshotClient(_username, _password, _ip, (String) params.get("moonshotScheme"), (Integer) params.get("moonshotPort"));
        _moonshotClient = new MoonshotClient(_username, _password, _ip, "https", 443);
        vmDao = (VMInstanceDao) params.get("vmDao");
        configDao = (ConfigurationDao) params.get("configDao");

        if (_pod == null) {
            throw new ConfigurationException("Unable to get the pod");
        }

        if (_cluster == null) {
            throw new ConfigurationException("Unable to get the pod");
        }

        if (_ip == null) {
            throw new ConfigurationException("Unable to get the host address");
        }

        if (_mac.equalsIgnoreCase("unknown")) {
            throw new ConfigurationException("Unable to get the host mac address");
        }

        if (_mac.split(":").length != 6) {
            throw new ConfigurationException("Wrong MAC format(" + _mac
                    + "). It must be in format of for example 00:11:ba:33:aa:dd which is not case sensitive");
        }

        if (_uuid == null) {
            throw new ConfigurationException("Unable to get the uuid");
        }

        try {
            ipmiRetryTimes = Integer.valueOf(configDao.getValue(Config.BaremetalIpmiRetryTimes.key()));
        } catch (Exception e) {
            s_logger.error(e.getMessage(), e);
        }

        s_logger.debug("Successfully configured Baremetal resource");
        return true;
    }

    @Override
    public StartupCommand[] initialize() {
        StartupRoutingCommand cmd = new StartupRoutingCommand(0, 0, 0, 0, null, Hypervisor.HypervisorType.BareMetal,
                new HashMap<String, String>());

        cmd.setDataCenter(_zone);
        cmd.setPod(_pod);
        cmd.setCluster(_cluster);
        cmd.setGuid(_uuid);
        cmd.setName(_ip + "-" + _cartridgeNodeLocation);
        cmd.setPrivateIpAddress(_ip);
        cmd.setStorageIpAddress(_ip);
        cmd.setVersion(BareMetalResourceBase.class.getPackage().getImplementationVersion());
        cmd.setCpus((int) _cpuNum);
        cmd.setSpeed(_cpuCapacity);
        cmd.setMemory(_memCapacity);
        cmd.setPrivateMacAddress(_mac);
        cmd.setPublicMacAddress(_mac);
        return new StartupCommand[] { cmd };
    }

    @Override
    public PingCommand getCurrentStatus(long id) {
        try {
            if (!_moonshotClient.pingNode(_cartridgeNodeLocation)) {
                Thread.sleep(10000); //TODO - make it config based.
                if (!_moonshotClient.pingNode(_cartridgeNodeLocation)) {
                    s_logger.warn("Cannot ping" + _cartridgeNodeLocation);
                    return null;
                }
            }
        } catch (Exception e) {
            s_logger.debug("Cannot ping" + _cartridgeNodeLocation, e);
            return null;
        }

        return new PingRoutingCommand(getType(), id, null);
    }

    @Override
    protected Answer execute(IpmISetBootDevCommand cmd) {
        BootTarget bootTarget = null;
        if (cmd.getBootDev() == IpmISetBootDevCommand.BootDev.disk) {
            bootTarget = BootTarget.M2;
        } else if (cmd.getBootDev() == IpmISetBootDevCommand.BootDev.pxe) {
            bootTarget = BootTarget.PXE;
        } else {
            throw new CloudRuntimeException("Unkonwn boot dev " + cmd.getBootDev());
        }

        String bootDev = cmd.getBootDev().name();
        boolean success = _moonshotClient.bootOnce(_cartridgeNodeLocation, bootTarget);
        if (!success) {
            s_logger.warn("Set " + _cartridgeNodeLocation + " boot dev to " + bootDev + "failed");
            return new Answer(cmd, false, "Set " + _cartridgeNodeLocation + " boot dev to " + bootDev + "failed");
        } else {
            s_logger.warn("Set " + _cartridgeNodeLocation + " boot dev to " + bootDev + "Success");
            return new Answer(cmd, true, "Set " + _cartridgeNodeLocation + " boot dev to " + bootDev + "Success");
        }
    }

    @Override
    protected MigrateAnswer execute(MigrateCommand cmd) {
        boolean success = _moonshotClient.setNodePowerStatus(_cartridgeNodeLocation, ResetType.OFF);
        return success ? new MigrateAnswer(cmd, true, "success", null) : new MigrateAnswer(cmd, false, "Power off failed", null);
    }

    @Override
    protected Answer execute(IpmiBootorResetCommand cmd) {
        String failureMessage = "Boot or reboot failed";
        String status = _moonshotClient.getPowerStatus(_cartridgeNodeLocation);
        if(ResetType.ON.toString().equalsIgnoreCase(status)) {
            if(!_moonshotClient.setNodePowerStatus(_cartridgeNodeLocation, ResetType.RESET)) {
                return new Answer(cmd, false, failureMessage);
            }
        } else if(ResetType.OFF.toString().equalsIgnoreCase(status)) {
            if(!_moonshotClient.setNodePowerStatus(_cartridgeNodeLocation, ResetType.ON)) {
                return new Answer(cmd, false, failureMessage);
            }
        } else {
            return new Answer(cmd, false, failureMessage);
        }

        return new Answer(cmd, true, "Success");
    }

    @Override
    public Answer executeRequest(Command cmd) {
        if (cmd instanceof SecurityGroupRulesCmd) {
            return Answer.createUnsupportedCommandAnswer(cmd);
        }
        return super.executeRequest(cmd);
    }

    @Override
    protected RebootAnswer execute(RebootCommand cmd) {
        boolean success = _moonshotClient.setNodePowerStatus(_cartridgeNodeLocation, ResetType.RESET);
        return success ?  new RebootAnswer(cmd, "Reboot succeeded", true) : new RebootAnswer(cmd, "Reboot failed", false);
    }

    @Override
    protected StopAnswer execute(StopCommand cmd) {
        boolean success = false;
        int count = 0;
        ResetType powerOff = ResetType.OFF;

        while (count < 10) {
            if (!_moonshotClient.setNodePowerStatus(_cartridgeNodeLocation, powerOff)) {
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }

            String status = _moonshotClient.getPowerStatus(_cartridgeNodeLocation);
            if (ResetType.OFF.toString().equalsIgnoreCase(status)) {
                success = true;
                break;
            } else if (ResetType.ON.toString().equalsIgnoreCase(status)) {
                powerOff = ResetType.OFF; // think of option force off
            } else {
                success = true;
                s_logger.warn("Cannot get power status of " + _name + ", assume VM state changed successfully");
                break;
            }

            count++;
        }

        return success ? new StopAnswer(cmd, "Success", true) : new StopAnswer(cmd, "Power off failed", false);
    }

    @Override
    protected StartAnswer execute(StartCommand cmd) {
        VirtualMachineTO vm = cmd.getVirtualMachine();

        String status = _moonshotClient.getPowerStatus(_cartridgeNodeLocation);
        if(ResetType.ON.toString().equalsIgnoreCase(status)) {
            if(!_moonshotClient.setNodePowerStatus(_cartridgeNodeLocation, ResetType.RESET)) {
                return new StartAnswer(cmd, "Node reboot failed");
            }
        } else if(ResetType.OFF.toString().equalsIgnoreCase(status)) {
            if(!_moonshotClient.setNodePowerStatus(_cartridgeNodeLocation, ResetType.ON)) {
                return new StartAnswer(cmd, "Node power on failed");
            }
        } else {
            return new StartAnswer(cmd, "Cannot get current power status of " + _cartridgeNodeLocation);
        }

        s_logger.debug("Start bare metal vm " + vm.getName() + "successfully");
        _vmName = vm.getName();
        return new StartAnswer(cmd);
    }
}
