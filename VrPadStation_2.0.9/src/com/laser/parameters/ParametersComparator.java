package com.laser.parameters;

import java.util.Comparator;

public class ParametersComparator implements Comparator<Parameter> {

	@Override
	public int compare(Parameter p1, Parameter p2) {
		return p1.name.compareTo(p2.name);
	}

}
