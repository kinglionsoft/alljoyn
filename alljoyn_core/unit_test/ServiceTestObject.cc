/**
 * @file
 * Sample implementation of an AllJoyn service for test harness.
 */

/******************************************************************************
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
 ******************************************************************************/

#include "ServiceTestObject.h"
#include "ajTestCommon.h"

#define QCC_MODULE "ALLJOYN"

using namespace std;
using namespace qcc;
using namespace ajn;


ServiceTestObject::ServiceTestObject(BusAttachment& bus, const char*path) : BusObject(path), myBus(bus)
{
}

void ServiceTestObject::RegisterForNameAcquiredSignals()
{
    QStatus status = ER_OK;
    const InterfaceDescription* intf = myBus.GetInterface("org.freedesktop.DBus");
    QCC_ASSERT(intf);
    /* register the signal handler for the 'NameAcquired' signal */
    status =  myBus.RegisterSignalHandler(this,
                                          static_cast<MessageReceiver::SignalHandler>(&ServiceTestObject::NameAcquiredSignalHandler),
                                          intf->GetMember("NameAcquired"),
                                          NULL);
    if (status != ER_OK) {
        QCC_LogError(status, ("Problem while registering name Acquired signal handler"));
    }

}

void ServiceTestObject::PopulateSignalMembers(const char*interface_name) {
    /* Register the signal handler with the bus */
    const InterfaceDescription* regTestIntf = myBus.GetInterface(interface_name);
    QCC_ASSERT(regTestIntf);
    my_signal_member = regTestIntf->GetMember("my_signal");
    QCC_ASSERT(my_signal_member);
}

QStatus ServiceTestObject::InstallMethodHandlers(const char*interface_name) {
    const InterfaceDescription* regTestIntf = myBus.GetInterface(interface_name);
    QCC_ASSERT(regTestIntf);
    /* Register the method handlers with the object */
    const MethodEntry methodEntries[] = {
        { regTestIntf->GetMember("my_ping"), static_cast<MessageReceiver::MethodHandler>(&ServiceTestObject::Ping) },
        { regTestIntf->GetMember("my_sing"), static_cast<MessageReceiver::MethodHandler>(&ServiceTestObject::Sing) },
        { regTestIntf->GetMember("my_king"), static_cast<MessageReceiver::MethodHandler>(&ServiceTestObject::King) },
        { regTestIntf->GetMember("ByteArrayTest"), static_cast<MessageReceiver::MethodHandler>(&ServiceTestObject::ByteArrayTest) },
        { regTestIntf->GetMember("DoubleArrayTest"), static_cast<MessageReceiver::MethodHandler>(&ServiceTestObject::DoubleArrayTest) }

    };

    QStatus status = AddMethodHandlers(methodEntries, ArraySize(methodEntries));
    return status;
}


QStatus ServiceTestObject::AddInterfaceToObject(const InterfaceDescription* intf) {
    QCC_ASSERT(intf);
    QStatus status = AddInterface(*intf);
    return status;
}


void ServiceTestObject::NameAcquiredSignalHandler(const InterfaceDescription::Member* member,
                                                  const char* sourcePath,
                                                  Message& msg) {
    QCC_UNUSED(member);
    QCC_UNUSED(sourcePath);
    QCC_UNUSED(msg);
    //QCC_SyncPrintf("Inside the Name Acquired  signal handler\n");
    output1++;
}


void ServiceTestObject::ObjectRegistered(void) {
    BusObject::ObjectRegistered();
    output1++;
}


void ServiceTestObject::Ping(const InterfaceDescription::Member* member, Message& msg)
{
    QCC_UNUSED(member);

    MsgArg arg(*(msg->GetArg(0)));
    printf("Pinged with: %s\n", msg->GetArg(0)->ToString().c_str());
    QStatus status = MethodReply(msg, &arg, 1);
    if (ER_OK != status) {
        QCC_LogError(status, ("Ping: Error sending reply"));
    }

    //Emit a signal
    MsgArg arg1("s", "Signal Emitted");
    status = Signal(NULL, 0, *my_signal_member, &arg1, 1, 0, 1);
    printf("\n Signal Sent");

}

void ServiceTestObject::Sing(const InterfaceDescription::Member* member, Message& msg)
{
    QCC_UNUSED(member);
    /* Reply with same string that was sent to us */
    MsgArg arg(*(msg->GetArg(0)));
    printf("Sung with: %s\n", msg->GetArg(0)->ToString().c_str());
    QStatus status = MethodReply(msg, &arg, 1);
    if (ER_OK != status) {
        QCC_LogError(status, ("Sing: Error sending reply"));
    }

}

void ServiceTestObject::King(const InterfaceDescription::Member* member, Message& msg)
{
    QCC_UNUSED(member);
    /* Reply with same string that was sent to us */
    MsgArg arg(*(msg->GetArg(0)));
    printf("King with: %s\n", msg->GetArg(0)->ToString().c_str());
    QStatus status = MethodReply(msg, &arg, 1);
    if (ER_OK != status) {
        QCC_LogError(status, ("King: Error sending reply"));
    }

    uint8_t flags = 0;
    flags |= ALLJOYN_FLAG_GLOBAL_BROADCAST;

    //Emit a signal
    MsgArg arg1("s", "Signal1 Emitted : MethodCall King");
    status = Signal(NULL, 0, *my_signal_member, &arg1, 1, 0, flags);
    printf("\n Signal Sent : MethodCall King");

    arg1.Set("s", "Signal2 Emitted : MethodCall King");
    status = Signal(NULL, 0, *my_signal_member, &arg1, 1, 0);
    printf("\n Signal Sent : MethodCall King");

    arg1.Set("s", "Signal3 Emitted : MethodCall King");
    status = Signal(NULL, 0, *my_signal_member, &arg1, 1, 0, flags);
    printf("\n Signal Sent : MethodCall King");


}

void ServiceTestObject::ByteArrayTest(const InterfaceDescription::Member* member, Message& msg)
{
    QCC_UNUSED(member);

    MsgArg arg(*(msg->GetArg(0)));
    QStatus status = MethodReply(msg, &arg, 1);
    if (ER_OK != status) {
        QCC_LogError(status, ("Error sending reply"));
    }
}

void ServiceTestObject::DoubleArrayTest(const InterfaceDescription::Member* member, Message& msg)
{
    QCC_UNUSED(member);

    MsgArg arg(*(msg->GetArg(0)));
    QStatus status = MethodReply(msg, &arg, 1);
    if (ER_OK != status) {
        QCC_LogError(status, ("Error sending reply"));
    }
}




QStatus ServiceTestObject::Get(const char* ifcName, const char*propName, MsgArg& val)
{
    QCC_UNUSED(ifcName);

    QStatus status = ER_OK;
    if (0 == strcmp("int_val", propName)) {
        //val.Set("i", prop_int_val);
        val.typeId = ALLJOYN_INT32;
        val.v_int32 = prop_int_val;
    } else if (0 == strcmp("str_val", propName)) {
        //val.Set("s", prop_str_val.c_str());
        val.typeId = ALLJOYN_STRING;
        val.v_string.str = prop_str_val.c_str();
        val.v_string.len = prop_str_val.size();
    } else if (0 == strcmp("ro_str", propName)) {
        //val.Set("s", prop_ro_str_val.c_str());
        val.typeId = ALLJOYN_STRING;
        val.v_string.str = prop_ro_str.c_str();
        val.v_string.len = prop_ro_str.size();
    } else {
        status = ER_BUS_NO_SUCH_PROPERTY;
    }
    return status;
}

QStatus ServiceTestObject::Set(const char* ifcName, const char*propName, MsgArg& val)
{
    QCC_UNUSED(ifcName);
    QStatus status = ER_OK;
    if ((0 == strcmp("int_val", propName)) && (val.typeId == ALLJOYN_INT32)) {
        prop_int_val = val.v_int32;
    } else if ((0 == strcmp("str_val", propName)) && (val.typeId == ALLJOYN_STRING)) {
        prop_str_val = val.v_string.str;
    } else if (0 == strcmp("ro_str", propName)) {
        status = ER_BUS_PROPERTY_ACCESS_DENIED;
    } else {
        status = ER_BUS_NO_SUCH_PROPERTY;
    }
    return status;
}
