package com.learning.serialize;

import java.io.InputStream;
import java.io.OutputStream;

public interface RpcSerialize {
    void serialize(OutputStream output, Object object);
    Object deserialize(InputStream input);
}