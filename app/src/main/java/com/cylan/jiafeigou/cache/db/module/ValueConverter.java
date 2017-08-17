package com.cylan.jiafeigou.cache.db.module;

import android.util.SparseArray;

import com.cylan.jiafeigou.support.log.AppLogger;

import org.greenrobot.greendao.converter.PropertyConverter;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.ArrayBufferInput;
import org.msgpack.value.ImmutableValue;
import org.msgpack.value.Value;

import java.util.Map;

/**
 * Created by yanzhendong on 2017/8/17.
 */

public class ValueConverter implements PropertyConverter<SparseArray<Value>, byte[]> {

    private static ThreadLocal<MessageBufferPacker> packer = new ThreadLocal<MessageBufferPacker>() {
        @Override
        protected MessageBufferPacker initialValue() {
            return MessagePack.newDefaultBufferPacker();
        }
    };

    private static ThreadLocal<MessageUnpacker> unpacker = new ThreadLocal<MessageUnpacker>() {
        @Override
        protected MessageUnpacker initialValue() {
            return MessagePack.newDefaultUnpacker(new byte[0]);
        }
    };

    @Override
    public SparseArray<Value> convertToEntityProperty(byte[] databaseValue) {
        SparseArray<Value> result = new SparseArray<>();
        try {
            MessageUnpacker messageUnpacker = unpacker.get();
            messageUnpacker.reset(new ArrayBufferInput(databaseValue));
            ImmutableValue unpackValue = messageUnpacker.unpackValue();
            if (unpackValue.isMapValue()) {
                for (Map.Entry<Value, Value> entry : unpackValue.asMapValue().entrySet()) {
                    if (entry.getKey().isIntegerValue()) {
                        result.put(entry.getKey().asIntegerValue().asInt(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
        return result;
    }

    @Override
    public byte[] convertToDatabaseValue(SparseArray<Value> entityProperty) {
        if (entityProperty == null) {
            return new byte[0];
        }
        try {
            MessageBufferPacker messageBufferPacker = packer.get();
            messageBufferPacker.flush();
            messageBufferPacker.packMapHeader(entityProperty.size());
            for (int i = 0; i < entityProperty.size(); i++) {
                messageBufferPacker.packInt(entityProperty.keyAt(i)).packValue(entityProperty.valueAt(i));
            }
            return messageBufferPacker.toByteArray();
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
        return new byte[0];
    }
}
