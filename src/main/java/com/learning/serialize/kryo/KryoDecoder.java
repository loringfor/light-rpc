package com.learning.serialize.kryo;

import com.learning.serialize.MessageCodecUtil;
import com.learning.serialize.MessageDecoder;

/**
 * Kryo解码器
 */
public class KryoDecoder extends MessageDecoder {
    public KryoDecoder(MessageCodecUtil util) {
        super(util);
    }
}
