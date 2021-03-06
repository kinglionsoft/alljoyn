/*
 *    Copyright (c) Open Connectivity Foundation (OCF), AllJoyn Open Source
 *    Project (AJOSP) Contributors and others.
 *
 *    SPDX-License-Identifier: Apache-2.0
 *
 *    All rights reserved. This program and the accompanying materials are
 *    made available under the terms of the Apache License, Version 2.0
 *    which accompanies this distribution, and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Copyright (c) Open Connectivity Foundation and Contributors to AllSeen
 *    Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for
 *    any purpose with or without fee is hereby granted, provided that the
 *    above copyright notice and this permission notice appear in all
 *    copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *    WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *    WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *    AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 *    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 *    PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 *    TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *    PERFORMANCE OF THIS SOFTWARE.
*/

package org.alljoyn.bus;

public class SessionOpts {

    /**
     * Holds the traffic type for this SessionOpt
     */
    public byte traffic;

    /**
     * Multi-point session capable.
     *
     * A session is multi-point if it can be joined multiple times to form a single
     * session with multi (greater than 2) endpoints. When false, each join attempt
     * creates a new point-to-point session.
     */
    public boolean isMultipoint;

    /**
     * Holds the proximity for this SessionOpt
     */
    public byte proximity;

    /**
     * Holds the allowed transports for this SessionOpt
     */
    public short transports;

    /**
     * Construct a new session options class using the most commonly used options:
     * traffic = TRAFFIC_MESSAGES, isMultipoint = false, proximity = PROXIMITY_ANY,
     * transports = TRANSPORT_ANY.
     */
    public SessionOpts() {
        traffic = TRAFFIC_MESSAGES;
        isMultipoint = false;
        proximity = PROXIMITY_ANY;
        transports = TRANSPORT_ANY;
    }

    /**
     * Construct a new session options class using the provided options.
     *
     * @param traffic the traffic bitfield values.
     * @param isMultipoint the isMultipoint option
     * @param proximity the proximity bitfield values.
     * @param transports the transports bitfield values.
     */
    public SessionOpts(byte traffic, boolean isMultipoint, byte proximity, short transports) {
        this.traffic = traffic;
        this.isMultipoint = isMultipoint;
        this.proximity = proximity;
        this.transports = transports;
    }

    /**
     * Use reliable message-based communication to move data between session endpoints.
     */
    public static final byte TRAFFIC_MESSAGES = 0x01;

    /**
     * Use unreliable (e.g., UDP) socket-based communication to move data between
     * session endpoints.  RAW does not imply raw sockets (that bypass ALL
     * enapsulation possibly down to the MAC level), it implies raw in an AllJoyn
     * sense --MESSAGE encapsulation is not used, but for example UDP + IP + MAC
     * encapsulation is used.
     */
    public static final byte TRAFFIC_RAW_UNRELIABLE = 0x02;

    /**
     * Use reliable (e.g., TCP) socket-based communication to move data between
     * session endpoints.  RAW does not imply raw sockets (that bypass ALL
     * enapsulation possibly down to the MAC level), it implies raw in an AllJoyn
     * sense --MESSAGE encapsulation is not used, but for example UDP + IP + MAC
     * encapsulation is used.
     */
    public static final byte TRAFFIC_RAW_RELIABLE = 0x04;

    /**
     * Do not limit the spatial scope of sessions.  This means that sessions may
     * be joined by jointers located anywhere.
     */
    public static final byte PROXIMITY_ANY = (byte)0xff;

    /**
     * Limit the spatial scope of sessions to the local host.  Interpret as
     * "the same physical machine."  This means that sessions may be joined by
     * jointers located only on the same physical machine as the one hosting the
     * session.
     */
    public static final byte PROXIMITY_PHYSICAL = 0x01;

    /**
     * Limit the spatial scope of sessions to anwhere on the local logical
     * network segment.  This means that sessions may be joined by jointers
     * located somewhere on the network.
     */
    public static final byte PROXIMITY_NETWORK = 0x02;

    /**
     * Use no transport to communicate with a given session.
     */
    public static final short TRANSPORT_NONE = 0x0000;

    /**
     * Use only the local transport to communicate with a given session.
     */
    public static final short TRANSPORT_LOCAL = 0x0001;

    /**
     * Use the TCP Transport to communicate with a given session.
     */
    public static final short TRANSPORT_TCP = 0x0004;

    /**
     * Use only the AllJoyn Reliable Datagram Protocol (flavor of reliable UDP)
     * to communicate with a given session.
     */
    public static final short TRANSPORT_UDP = 0x0100;

    /**
     * A placeholder for an experimental transport that has not yet reached the
     * performance, stability or testing requirements of a commercialized
     * transport.
     *
     * It is expected that each experimental Transport will alias this bit if
     * included in an AllJoyn release and then allocate one of the reserved mask
     * bits upon attaining commercialized status.
     *
     * For example,
     *     public static final short TRANSPORT_CAN_AND_STRING = TRANSPORT_EXPERIMENTAL
     */
    public static final short TRANSPORT_EXPERIMENTAL = (short)0x8000;

    /**
     * Use any available IP-based transport to communicate with a given session.
     *
     * Selecting the IP transport really implies letting the system decice which
     * transport is best.
     */
    public static final short TRANSPORT_IP = (TRANSPORT_TCP | TRANSPORT_UDP);

    /**
     * Use any available non-experimental transport to communicate with a given session.
     */
    public static final short TRANSPORT_ANY = (TRANSPORT_LOCAL | TRANSPORT_IP);

    public String toString( ) {
        StringBuilder result = new StringBuilder();
        result.append(this.getClass().getName() + " {");

        result.append("traffic = ");
        String value = String.format("(0x%02x)", traffic);
        result.append(value);
        if ((traffic & TRAFFIC_MESSAGES) != 0) result.append(" TRAFFIC_MESSAGES");
        if ((traffic & TRAFFIC_RAW_UNRELIABLE) != 0) result.append(" TRAFFIC_RAW_UNRELIABLE");
        if ((traffic & TRAFFIC_RAW_RELIABLE) != 0) result.append(" TRAFFIC_RAW_RELIABLE");

        result.append(", isMultipoint = ");
        value = String.format("%b", isMultipoint);
        result.append(value);

        result.append(", proximity =");
        value = String.format("(0x%02x)", proximity);
        result.append(value);
        if ((proximity & PROXIMITY_PHYSICAL) != 0) result.append(" PROXIMITY_PHYSICAL");
        if ((proximity & PROXIMITY_NETWORK) != 0) result.append(" PROXIMITY_NETWORK");

        result.append(", transports =");
        value = String.format("(0x%04x)", transports);
        result.append(value);
        if ((transports & TRANSPORT_LOCAL) != 0) result.append(" TRANSPORT_LOCAL");
        if ((transports & TRANSPORT_TCP) != 0) result.append(" TRANSPORT_TCP");
        if ((transports & TRANSPORT_UDP) != 0) result.append(" TRANSPORT_UDP");

        result.append("}");
        return result.toString();
    }

    /**
     * Use only a wireless local area network to communicate with a given session.
     * @deprecated
     */
    @Deprecated public static final short TRANSPORT_WLAN = 0x0004;

    /**
     * Use only a wireless wide area network to communicate with a given session.
     * @deprecated
     */
    @Deprecated public static final short TRANSPORT_WWAN = 0x0008;

    /**
     * Use only a wired local area network to communicate with a given session.
     * @deprecated
     */
    @Deprecated public static final short TRANSPORT_LAN = 0x0010;

    /**
     * Use only the Wi-Fi Direct transport to communicate with a given session.
     * @deprecated
     */
    @Deprecated public static final short TRANSPORT_WFD = 0x0080;
}
