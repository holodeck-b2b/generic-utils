/*******************************************************************************
 * Copyright (C) 2021 The Holodeck Team, Sander Fieten
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.holodeckb2b.commons;

/**
 * Represent a pair of objects.
 * 
 * @author Sander Fieten (sander at holodeck-b2b.org)
 */
public class Pair<A, B> {
	
	private final A val1;
	private final B val2;
	
	/**
	 * Constructs a new pair with the given values.
	 * 
	 * @param v1	first value of the pair
	 * @param v2	second value of the pair
	 */
	public Pair(A v1, B v2) {
		this.val1 = v1;
		this.val2 = v2;
	}
	
	/**
	 * @return first value of the pair
	 */
	public A value1() {
		return val1;
	}
	
	/**
	 * @return second value of the pair
	 */
	public B value2() {
		return val2;
	}
	
	/**
	 * Compares this pair for equality with the given object. A Pair is equal if the other Pair object contains the 
	 * same values, i.e. the contained objects are equal.
	 * 
	 * @return <code>true</code> when the other object is also a <code>Pair</code> and contains the same values,</br>
	 * 		   <code>false</code> otherwise
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Pair))
			return false;
		
		@SuppressWarnings("rawtypes")
		Pair op = (Pair) o;
		return (this.val1 == op.val1 || (this != null && this.val1.equals(op.val1))) 
			&& (this.val2 == op.val2 || (this != null && this.val2.equals(op.val2)));
	}
}
