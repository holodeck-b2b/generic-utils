/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.holodeckb2b.commons.security;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameStyle;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;

/**
 * Is an extension of the BouncyCastle class to use the reverse ordering when converting distinguished names to String.
 *
 * @author Sander Fieten (sander at holodeck-b2b.org)
 */
class HB2BStyle extends BCStyle {

	/**
	 * Singleton instance.
	 */
	static final X500NameStyle INSTANCE = new HB2BStyle();

	protected HB2BStyle() {
		super();
	}

	/**
	 * Returns the string representation of the given X500 name using the order as specified in RFC4519.
	 *
	 * @param name	the X500 name to convert
	 * @return string representation of the X500 Name in RFC4519 style
	 */
	@Override
	public String toString(X500Name name) {
		StringBuffer buf = new StringBuffer();
		RDN[] rdns = name.getRDNs();
		for (int i = rdns.length - 1; i >= 0; i--) {
			if (i < rdns.length - 1)
				buf.append(',');
			IETFUtils.appendRDN(buf, rdns[i], defaultSymbols);
		}
		return buf.toString();
	}
}
