package de.maxikg.mongowg.codec;

import com.sk89q.worldedit.math.BlockVector2;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.json.JsonReader;
import org.bson.json.JsonWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class BlockVector2DCodecTest {

    @Test
    public void testCodec() throws IOException {
        Codec<BlockVector2> codec = BlockVector2DCodec.INSTANCE;
        BlockVector2 blockVector = BlockVector2.at(4, 8);

        BlockVector2 other;
        try (StringWriter sw = new StringWriter()) {
            codec.encode(new JsonWriter(sw), blockVector, EncoderContext.builder().build());
            other = codec.decode(new JsonReader(sw.toString()), DecoderContext.builder().build());
        }

        Assert.assertEquals(blockVector, other);
    }
}
