package com.learning.serialize.kryo;

import com.learning.serialize.MessageCodecUtil;
import com.learning.serialize.MessageEncoder;

/**
 * Kryo编码器
 */
public class KryoEncoder extends MessageEncoder {
    public KryoEncoder(MessageCodecUtil util) {
        super(util);
    }
}
