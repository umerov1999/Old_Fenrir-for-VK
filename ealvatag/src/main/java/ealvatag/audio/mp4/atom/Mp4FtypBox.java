package ealvatag.audio.mp4.atom;

import static ealvatag.logging.EalvaTagLog.LogLevel.WARN;

import com.google.common.base.MoreObjects;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;

import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.utils.StandardCharsets;
import okio.BufferedSource;

/**
 * Ftyp (File Type) is the first atom, can be used to help identify the mp4 container type
 */
public class Mp4FtypBox extends AbstractMp4Box {
    private static final JLogger LOG = JLoggers.get(Mp4FtypBox.class, EalvaTagLog.MARKER);
    private static final int MAJOR_BRAND_POS = 0;
    private static final int MAJOR_BRAND_LENGTH = 4;
    private static final int MAJOR_BRAND_VERSION_POS = 4;
    private static final int MAJOR_BRAND_VERSION_LENGTH = 4;
    private static final int COMPATIBLE_BRAND_LENGTH = 4; //Can be multiple of these
    private final List<String> compatibleBrands = new ArrayList<String>();
    private String majorBrand;
    private int majorBrandVersion;

    /**
     * @param header     header info
     * @param dataBuffer data of box (doesnt include header data)
     */
    public Mp4FtypBox(Mp4BoxHeader header, ByteBuffer dataBuffer) {
        this.header = header;
        this.dataBuffer = dataBuffer;
        this.dataBuffer.order(ByteOrder.BIG_ENDIAN);
    }

    public Mp4FtypBox(BufferedSource bufferedSource) throws IOException {
        header = new Mp4BoxHeader(bufferedSource);
        int dataSize = header.getDataLength();
        majorBrand = bufferedSource.readString(MAJOR_BRAND_LENGTH, StandardCharsets.ISO_8859_1);
        dataSize -= MAJOR_BRAND_LENGTH;
        majorBrandVersion = bufferedSource.readInt();
        dataSize -= MAJOR_BRAND_VERSION_LENGTH;
        while (dataSize >= COMPATIBLE_BRAND_LENGTH) {
            String compatibleBrand = bufferedSource.readString(COMPATIBLE_BRAND_LENGTH, StandardCharsets.ISO_8859_1);
            dataSize -= COMPATIBLE_BRAND_LENGTH;
            if (!"\u0000\u0000\u0000\u0000".equals(compatibleBrand)) {
                compatibleBrands.add(compatibleBrand);
            }
        }
        if (dataSize != 0) {
            // malformed, but we'll try to press on
            LOG.log(WARN, "%s has unrecognized trailing data size=%s", getClass(), dataSize);
            bufferedSource.skip(dataSize);
        }
    }

    public void processData() throws CannotReadException {
        CharsetDecoder decoder = java.nio.charset.StandardCharsets.ISO_8859_1.newDecoder();
        try {
            majorBrand = decoder.decode((ByteBuffer) dataBuffer.slice().limit(MAJOR_BRAND_LENGTH)).toString();
        } catch (CharacterCodingException cee) {
            //Ignore

        }
        dataBuffer.position(dataBuffer.position() + MAJOR_BRAND_LENGTH);
        majorBrandVersion = dataBuffer.getInt();
        while ((dataBuffer.position() < dataBuffer.limit()) && (dataBuffer.limit() - dataBuffer.position() >= COMPATIBLE_BRAND_LENGTH)) {
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            try {
                String brand = decoder.decode((ByteBuffer) dataBuffer.slice().limit(COMPATIBLE_BRAND_LENGTH)).toString();
                //Sometimes just extra groups of four nulls
                if (!brand.equals("\u0000\u0000\u0000\u0000")) {
                    compatibleBrands.add(brand);
                }
            } catch (CharacterCodingException cee) {
                //Ignore
            }
            dataBuffer.position(dataBuffer.position() + COMPATIBLE_BRAND_LENGTH);
        }
    }


    public String getMajorBrand() {
        return majorBrand;
    }


    public int getMajorBrandVersion() {
        return majorBrandVersion;
    }


    public List<String> getCompatibleBrands() {
        return compatibleBrands;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("majorBrand", majorBrand)
                .add("majorBrandVersion", majorBrandVersion)
                .add("compatibleBrands", compatibleBrands)
                .toString();
    }

    /**
     * Major brand, helps identify whats contained in the file, used by major and compatible brands
     * but this is not an exhaustive list, so for that reason we don't force the values read from the file
     * to tie in with this enum.
     */
    public enum Brand {
        ISO14496_1_BASE_MEDIA("isom", "ISO 14496-1"),
        ISO14496_12_BASE_MEDIA("iso2", "ISO 14496-12"),
        ISO14496_1_VERSION_1("mp41", "ISO 14496-1"),
        ISO14496_1_VERSION_2("mp42", "ISO 14496-2:Multi track with BIFS scenes"),
        QUICKTIME_MOVIE("qt  ", "Original Quicktime"),
        JVT_AVC("avc1", "JVT"),
        THREEG_MOBILE_MP4("MPA ", "3G Mobile"),
        APPLE_AAC_AUDIO("M4P ", "Apple Audio"),
        AES_ENCRYPTED_AUDIO("M4B ", "Apple encrypted Audio"),
        APPLE_AUDIO("mp71", "Apple Audio"),
        ISO14496_12_MPEG7_METADATA("mp71", "MAIN_SYNTHESIS"),
        APPLE_AUDIO_ONLY("M4A ", "M4A Audio"),
        //SOmetimes used by protected mutli track audio
        ;

        private final String id;
        private final String description;

        /**
         * @param id          it is stored as in file
         * @param description human readable description
         */
        Brand(String id, String description) {
            this.id = id;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

    }
}
