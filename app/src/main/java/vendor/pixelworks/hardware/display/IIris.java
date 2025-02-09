/*
 * Copyright (C) 2025 The Nameless-CLO Project
 * SPDX-License-Identifier: Apache-2.0
 */

package vendor.pixelworks.hardware.display;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IIris extends IInterface {

    public static final String DESCRIPTOR = "vendor$pixelworks$hardware$display$IIris".replace('$', '.');
    public static final String HASH = "02c8c5526cbde39f502b3bf8cccaf196c81de25f";
    public static final int VERSION = 1;

    String getInterfaceHash() throws RemoteException;

    int getInterfaceVersion() throws RemoteException;

    int[] irisConfigureGet(int type, int[] values) throws RemoteException;

    int irisConfigureSet(int type, int[] values) throws RemoteException;

    void registerCallback(long cookie, IIrisCallback iIrisCallback) throws RemoteException;

    public static class Default implements IIris {
        @Override
        public int[] irisConfigureGet(int type, int[] values) throws RemoteException {
            return null;
        }

        @Override
        public int irisConfigureSet(int type, int[] values) throws RemoteException {
            return 0;
        }

        @Override
        public void registerCallback(long cookie, IIrisCallback callback) throws RemoteException {
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

    public static abstract class Stub extends Binder implements IIris {

        static final int TRANSACTION_irisConfigureGet = 16;
        static final int TRANSACTION_irisConfigureSet = 17;
        static final int TRANSACTION_registerCallback = 21;
        static final int TRANSACTION_getInterfaceHash = 16777214;
        static final int TRANSACTION_getInterfaceVersion = 16777215;

        public Stub() {
            markVintfStability();
            attachInterface(this, DESCRIPTOR);
        }

        public static IIris asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            final IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IIris)) {
                return (IIris) iin;
            }
            return new Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case TRANSACTION_irisConfigureGet:
                    return "irisConfigureGet";
                case TRANSACTION_irisConfigureSet:
                    return "irisConfigureSet";
                case TRANSACTION_registerCallback:
                    return "registerCallback";
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
                        case TRANSACTION_irisConfigureGet:
                            final int type1 = data.readInt();
                            final int[] values1 = data.createIntArray();
                            data.enforceNoDataAvail();
                            final int[] res1 = irisConfigureGet(type1, values1);
                            reply.writeNoException();
                            reply.writeIntArray(res1);
                            return true;
                        case TRANSACTION_irisConfigureSet:
                            final int type2 = data.readInt();
                            final int[] values2 = data.createIntArray();
                            data.enforceNoDataAvail();
                            final int res2 = irisConfigureSet(type2, values2);
                            reply.writeNoException();
                            reply.writeInt(res2);
                            return true;
                        case TRANSACTION_registerCallback:
                            final long cookie = data.readLong();
                            final IIrisCallback callback =
                                    IIrisCallback.Stub.asInterface(data.readStrongBinder());
                            data.enforceNoDataAvail();
                            registerCallback(cookie, callback);
                            reply.writeNoException();
                            return true;
                        default:
                            return super.onTransact(code, data, reply, flags);
                    }
            }
        }

        private static class Proxy implements IIris {

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
            public int[] irisConfigureGet(int type, int[] values) throws RemoteException {
                final Parcel _data = Parcel.obtain(asBinder());
                final Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeIntArray(values);
                    final boolean _status = mRemote.transact(16, _data, _reply, 0);
                    if (!_status) {
                        throw new RemoteException("Method irisConfigureGet is unimplemented.");
                    }
                    _reply.readException();
                    final int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public int irisConfigureSet(int type, int[] values) throws RemoteException {
                final Parcel _data = Parcel.obtain(asBinder());
                final Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeIntArray(values);
                    final boolean _status = mRemote.transact(17, _data, _reply, 0);
                    if (!_status) {
                        throw new RemoteException("Method irisConfigureSet is unimplemented.");
                    }
                    _reply.readException();
                    final int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void registerCallback(long cookie, IIrisCallback callback) throws RemoteException {
                final Parcel _data = Parcel.obtain(asBinder());
                final Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeLong(cookie);
                    _data.writeStrongInterface(callback);
                    final boolean _status = mRemote.transact(21, _data, _reply, 0);
                    if (!_status) {
                        throw new RemoteException("Method registerCallback is unimplemented.");
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
