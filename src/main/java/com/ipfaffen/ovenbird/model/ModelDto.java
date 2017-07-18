package com.ipfaffen.ovenbird.model;

/**
 * @author Isaias Pfaffenseller
 */
public abstract class ModelDto<T extends ModelDto<T>> implements Comparable<T> {

	@Override
	abstract public boolean equals(Object obj);

	@Override
	abstract public int hashCode();
}