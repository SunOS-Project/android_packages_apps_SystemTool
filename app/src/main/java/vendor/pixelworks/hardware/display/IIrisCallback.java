/*
 * Copyright (C) 2025 The Nameless-CLO Project
 * SPDX-License-Identifier: Apache-2.0
 */

package vendor.pixelworks.hardware.display;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIrisCallback extends IInterface {

    public static final String DESCRIPTOR = "vendor$pixelworks$hardware$display$IIrisCallback".replace('$', '.');
    public static final String HASH = "02c8c5526cbde39f502b3bf8cccaf196c81de25f";
    public static final int VERSION = 1;

    String getInterfaceHash() throws RemoteException;

    int getInterfaceVersion() throws RemoteException;

    void onFeatureChanged(int type, int[] values) throws RemoteException;

    public static class Default implements IIrisCallback {
        @Override
        public void onFeatureChanged(int type, int[] values) throws RemoteException {
        }

        @Override
        public int getInterfaceVersion() {
            return 0;
        }

        @Override
        public String getInterfaceHash() {
            return "";
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIrisCallback {

        static final int TRANSACTION_onFeatureChanged = 1;
        static final int TRANSACTION_getInterfaceHash = 16777214;
        static final int TRANSACTION_getInterfaceVersion = 16777215;

        public Stub() {
            markVintfStability();
            attachInterface(this, DESCRIPTOR);
        }

        public static IIrisCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            final IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IIrisCallback)) {
                return (IIrisCallback) iin;
            }
            return new Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case TRANSACTION_onFeatureChanged:
                    return "onFeatureChanged";
                case TRANSACTION_getInterfaceHash:
                    return "getInterfaceHash";
                case TRANSACTION_getInterfaceVersion:
                    return "getInterfaceVersion";
                default:
                    return null;
            }
        }

        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            final String descriptor = DESCRIPTOR;
            if (code >= 1 && code <= TRANSACTION_getInterfaceVersion) {
                data.enforceInterface(descriptor);
            }
            switch (code) {
                case TRANSACTION_getInterfaceHash:
                    reply.writeNoException();
                    reply.writeString(getInterfaceHash());
                    return true;
                case TRANSACTION_getInterfaceVersion:
                    reply.writeNoException();
                    reply.writeInt(getInterfaceVersion());
                    return true;
                case IBinder.INTERFACE_TRANSACTION:
                    reply.writeString(descriptor);
                    return true;
                default:
                    switch (code) {
                        case TRANSACTION_onFeatureChanged:
                            final int type = data.readInt();
                            final int[] values = data.createIntArray();
                            data.enforceNoDataAvail();
                            onFeatureChanged(type, values);
                            reply.writeNoException();
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements IIrisCallback {

            private final IBinder mRemote;
            private int mCachedVersion = -1;
            private String mCachedHash = "-1";

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                return mRemote;
            }

            public String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public void onFeatureChanged(int type, int[] values) throws RemoteException {
                final Parcel _data = Parcel.obtain(asBinder());
                final Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeIntArray(values);
                    final boolean _status = mRemote.transact(1, _data, _reply, 0);
                    if (!_status) {
                        throw new RemoteException("Method onFeatureChanged is unimplemented.");
                    }
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public int getInterfaceVersion() throws RemoteException {
                if (mCachedVersion == -1) {
                    final Parcel data = Parcel.obtain(asBinder());
                    final Parcel reply = Parcel.obtain();
                    try {
                        data.writeInterfaceToken(DESCRIPTOR);
                        mRemote.transact(Stub.TRANSACTION_getInterfaceVersion, data, reply, 0);
                        reply.readException();
                        mCachedVersion = reply.readInt();
                    } finally {
                        reply.recycle();
                        data.recycle();
                    }
                }
                return mCachedVersion;
            }

            @Override
            public synchronized String getInterfaceHash() throws RemoteException {
                if ("-1".equals(mCachedHash)) {
                    final Parcel data = Parcel.obtain(asBinder());
                    final Parcel reply = Parcel.obtain();
                    try {
                        data.writeInterfaceToken(DESCRIPTOR);
                        mRemote.transact(Stub.TRANSACTION_getInterfaceHash, data, reply, 0);
                        reply.readException();
                        mCachedHash = reply.readString();
                        reply.recycle();
                        data.recycle();
                    } catch (Throwable th) {
                        reply.recycle();
                        data.recycle();
                        throw th;
                    }
                }
                return mCachedHash;
            }
        }

        public int getMaxTransactionId() {
            return TRANSACTION_getInterfaceHash;
        }
    }
}
