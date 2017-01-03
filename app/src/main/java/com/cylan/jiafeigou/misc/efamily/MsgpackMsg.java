package com.cylan.jiafeigou.misc.efamily;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.IOException;

/**
 * Created by cylan-hunt on 17-1-3.
 */

public class MsgpackMsg {
    @Message
    public static class MsgHeader {
        public MsgHeader() {
            msgId = -1;
            callee = "";
        }

        @Index(0)
        public int msgId;
        @Index(1)
        public String caller;
        @Index(2)
        public String callee;

        @Ignore
        public byte[] toBytes() {
            try {
                MessagePack msgpack = new MessagePack();
                return msgpack.write(this);
            } catch (IOException ex) {
                return null;
            }
        }

        @Ignore
        public String toString() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

    }

    @Ignore
    public static MsgpackMsg.MsgHeader fromBytes(byte[] bytes) {
        MessagePack msgpack = new MessagePack();
        try {
            return msgpack.read(bytes, MsgpackMsg.MsgHeader.class);
        } catch (Exception e) {
            AppLogger.e("" + e.getLocalizedMessage());
            return null;
        }
    }
}
