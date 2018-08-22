package bitcoin.walllet.kit.network.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import bitcoin.walllet.kit.common.constant.BitcoinConstants;
import bitcoin.walllet.kit.common.io.BitcoinInput;
import bitcoin.walllet.kit.common.io.BitcoinOutput;
import bitcoin.walllet.kit.common.util.HashUtils;

/**
 * Build P2P message:
 * https://en.bitcoin.it/wiki/Protocol_documentation#Message_structure
 *
 * @author liaoxuefeng
 */
public class GetHeadersMessage extends Message {

    int version; // uint32
    byte[][] hashes; // byte[32]
    byte[] hashStop; // hash of the last desired block header; set to zero to
    // get as many blocks as possible (2000)

    public GetHeadersMessage(byte[] payload) throws IOException {
        super("getheaders");
        try (BitcoinInput input = new BitcoinInput(new ByteArrayInputStream(payload))) {
            this.version = input.readInt();
            long hashCount = input.readVarInt(); // do not keep hash count
            this.hashes = new byte[(int) hashCount][];
            for (int i = 0; i < this.hashes.length; i++) {
                this.hashes[i] = input.readBytes(32);
            }
            this.hashStop = input.readBytes(32);
        }
    }

    public GetHeadersMessage(byte[][] blockHashes) {
        super("getheaders");
        hashes = blockHashes;
        version = BitcoinConstants.PROTOCOL_VERSION;
        hashStop = BitcoinConstants.ZERO_HASH_BYTES;
    }

    @Override
    protected byte[] getPayload() {
        BitcoinOutput output = new BitcoinOutput();
        output.writeInt(this.version).writeVarInt(this.hashes.length);
        for (int i = 0; i < this.hashes.length; i++) {
            output.write(this.hashes[i]);
        }
        output.write(this.hashStop);
        return output.toByteArray();
    }

    @Override
    public String toString() {
        return "GetHeadersMessage(" + this.hashes.length + ": [" + String.join(", ", Arrays.stream(this.hashes).map((hash) -> {
            return HashUtils.toHexStringAsLittleEndian(hash);
        }).limit(10).toArray(String[]::new)) + "], hashStop=" + HashUtils.toHexStringAsLittleEndian(this.hashStop)
                + ")";
    }

}