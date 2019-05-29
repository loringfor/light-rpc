package com.learning.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.learning.model.MessageRequest;
import com.learning.model.MessageResponse;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * Kryo对象池工厂
 * @author Loring
 * @date 2019.3.19
 */
public class KryoPoolFactory {
    private static KryoPoolFactory poolFactory;

    private KryoFactory factory = new KryoFactory() {
        @Override
        public Kryo create() {
            Kryo kryo =new Kryo();
            kryo.setReferences(false);
            //把已知的结构注册到Kryo注册器里面，提高序列化/反序列化效率
            kryo.register(MessageRequest.class);
            kryo.register(MessageResponse.class);
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };

    private KryoPool pool = new KryoPool.Builder(factory).build();

    private KryoPoolFactory(){}

    // 单例模式
    public static KryoPool getKryoPoolInstance(){
        if(poolFactory==null){
            synchronized (KryoPoolFactory.class){
                if (poolFactory==null){
                    poolFactory = new KryoPoolFactory();
                }
            }
        }
        return poolFactory.getPool();
    }

    public KryoPool getPool(){
        return pool;
    }
}
