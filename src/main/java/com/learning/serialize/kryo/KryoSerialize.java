package com.learning.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.learning.serialize.RpcSerialize;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Kryo序列化/反序列化
 * @author Loring
 */
public class KryoSerialize implements RpcSerialize {

    private KryoPool pool;

    public KryoSerialize(KryoPool pool) {
        this.pool = pool;
    }

    @Override
    public void serialize(OutputStream output, Object object) {
        Kryo kryo=pool.borrow();
        Output out = new Output(output);
        kryo.writeClassAndObject(out,object);
        out.close();
        pool.release(kryo);
    }

    @Override
    public Object deserialize(InputStream input) {
        Kryo kryo = pool.borrow();
        Input in = new Input(input);
        Object object =kryo.readClassAndObject(in);
        in.close();
        pool.release(kryo);
        return object;
    }
}
