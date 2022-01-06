package com.alibaba.confidentialcomputing.common;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * SerializationHelper is a helper class to process Object's serialization and deserialization.
 */
public final class SerializationHelper {
    /**
     * serialization helper method.
     *
     * @return serialization result.
     * @throws IOException {@link IOException} If serialization failed.
     */
    public static byte[] serialize(Object data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(data);
        return bos.toByteArray();
    }

    /**
     * deserialization helper method.
     *
     * @return deserialization result.
     * @throws IOException            {@link IOException} If deserialization failed.
     * @throws ClassNotFoundException {@link ClassNotFoundException} If deserialization failed.
     */
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(inputStream);
        return ois.readObject();
    }
}