package com.laser.parameters;

import java.util.ArrayList;
import java.util.List;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_param_request_list;
import com.MAVLink.Messages.ardupilotmega.msg_param_set;
import com.MAVLink.Messages.ardupilotmega.msg_param_value;
import com.laser.MAVLink.Drone;
import com.laser.service.MAVLinkClient;


public class ParameterManager {
	

	public interface OnParameterManagerListener 
	{
		public abstract void onParametersReceived();
		public abstract void onParameterReceived(Parameter parameter, short paramIndex);
		public abstract void onParamsCountReceived(int count);
	}

	private MAVLinkClient MAV;
	private OnParameterManagerListener listener;
	private List<Parameter> parametersList;	
	private int paramsCount = 0;

	enum parametersStates {IDLE}
	private parametersStates state = parametersStates.IDLE;
	private Drone drone;

	public ParameterManager(MAVLinkClient MAV, OnParameterManagerListener listener, Drone drone)
	{
		this.MAV = MAV;
		this.listener = listener;
		this.drone = drone;
		parametersList = new ArrayList<Parameter>();
	}

	public void getAllParameters() 
	{
		parametersList.clear();
		requestParametersList();
	}

	private void requestParametersList() 
	{
		msg_param_request_list msg = new msg_param_request_list();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		MAV.sendMavPacket(msg.pack());
	}

	public boolean processMessage(MAVLinkMessage msg) 
	{
		if (msg.sysid != drone.getsysId() || msg.compid != drone.getcompId())
			return false;
		
		switch (state) 
		{
		default:
		case IDLE:
			break;
		}
		if (msg.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) 
		{
			processReceivedParam((msg_param_value) msg);
			return true;
		}
		return false;
	}

	private void processReceivedParam(msg_param_value m_value) 
	{
		listener.onParamsCountReceived(m_value.param_count);
		paramsCount = m_value.param_count;
		Parameter param = new Parameter(m_value);
		parametersList.add(param);
		listener.onParameterReceived(param, m_value.param_index);
		if (m_value.param_index == m_value.param_count - 1) 
			listener.onParametersReceived();
	}

	public void sendParameter(Parameter parameter) {
		msg_param_set msg = new msg_param_set();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.setParam_Id(parameter.name);
		msg.param_type = (byte) parameter.type;
		msg.param_value = (float) parameter.value;
		MAV.sendMavPacket(msg.pack());
	}	
	public double getParameterValue(String parameterName)
	{
		for (int i = 0; i < parametersList.size(); i++)
		{
			if (parametersList.get(i).name.equalsIgnoreCase(parameterName))
				return parametersList.get(i).value;				
		}
		return -1;
	}

	public List<Parameter> getParametersList() 
	{
		//Collections.sort(parameters, new ParametersComparator()); qui fa casino
		return parametersList;
	}
	
	public void updateParametersListFromFile(List<Parameter> parametersFromFile)
	{
		parametersList.clear();
		parametersList = parametersFromFile;
	}
	
	public void clearParametersList() 
	{
		if (parametersList != null)
			parametersList.clear();
	}
	
	public int getParametersCount()
	{
		return paramsCount;
	}
	
	public boolean contains(String name) 
	{
		for (Parameter p : parametersList)
		{
			if (p.name.equalsIgnoreCase(name))
				return true;
		}
		return false;	
	}
	
	public int indexOf(String name) 
	{
		int bRet = -1;
		for (int i = 0; i < parametersList.size(); i++)
		{
			Parameter p = parametersList.get(i);
			if (p.name.equalsIgnoreCase(name))
			{
				bRet = parametersList.indexOf(p);	
				break;
			}
		}
		return bRet;	
	}
	
}
