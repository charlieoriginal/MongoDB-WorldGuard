package de.maxikg.mongowg.codec;

import com.sk89q.worldedit.math.BlockVector3;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.json.JsonReader;
import org.bson.json.JsonWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class BlockVectorCodecTest {

    @Test
    public void testCodec() throws IOException {
        Codec<BlockVector3> codec = BlockVectorCodec.INSTANCE;
        BlockVector3 blockVector = BlockVector3.at(4, 8, 15);

        BlockVector3 other;
        try (StringWriter sw = new StringWriter()) {
            codec.encode(new JsonWriter(sw), blockVector, EncoderContext.builder().build());
            other = codec.decode(new JsonReader(sw.toString()), DecoderContext.builder().build());
        }

        Assert.assertEquals(blockVector, other);
    }
}
