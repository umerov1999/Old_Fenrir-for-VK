package ealvatag.tag.datatype;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import okio.Buffer;

/**
 * Represents a data type that supports multiple terminated Strings (there may only be one)
 */
public class MultipleTextEncodedStringNullTerminated extends AbstractDataType {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Creates a new ObjectStringSizeTerminated datatype.
     *
     * @param identifier identifies the frame type
     */
    public MultipleTextEncodedStringNullTerminated(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
        value = new MultipleTextEncodedStringNullTerminated.Values();
    }

    public MultipleTextEncodedStringNullTerminated(TextEncodedStringSizeTerminated object) {
        super(object);
        value = new MultipleTextEncodedStringNullTerminated.Values();
    }

    public MultipleTextEncodedStringNullTerminated(MultipleTextEncodedStringNullTerminated object) {
        super(object);
    }

    public boolean equals(Object obj) {
        return obj instanceof MultipleTextEncodedStringNullTerminated && super.equals(obj);
    }

    /**
     * Returns the size in bytes of this datatype when written to file
     *
     * @return size of this datatype
     */
    public int getSize() {
        return size;
    }

    /**
     * Check the value can be encoded with the specified encoding
     */
    public boolean canBeEncoded() {
        if (null == value) return false;
        List<String> list = ((Values) value).getList();
        if (list.isEmpty()) return false;
        for (String s : list) {
            if (!new TextEncodedStringNullTerminated(identifier, frameBody, s).canBeEncoded())
                return false;
        }
        return true;
    }

    /**
     * Read Null Terminated Strings from the array starting at offset, continue until unable to find any null terminated
     * Strings or until reached the end of the array. The offset should be set to byte after the last null terminated
     * String found.
     *
     * @param arr    to read the Strings from
     * @param offset in the array to start reading from
     * @throws InvalidDataTypeException if unable to find any null terminated Strings
     */
    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        //Continue until unable to read a null terminated String
        while (true) {
            try {
                //Read String
                TextEncodedStringNullTerminated next = new TextEncodedStringNullTerminated(identifier, frameBody);
                next.readByteArray(arr, offset);

                if (next.getSize() == 0) {
                    break;
                } else {
                    if (null != value) {
                        //Add to value
                        ((Values) value).add((String) next.getValue());

                        //Add to size calculation
                        size += next.getSize();

                        //Increment Offset to start of next datatype.
                        offset += next.getSize();
                    }
                }
            } catch (InvalidDataTypeException idte) {
                break;
            }

            if (size == 0) {
                throw new InvalidDataTypeException("No null terminated Strings found");
            }
        }
    }

    @Override
    public void read(Buffer buffer, int size) throws EOFException, InvalidDataTypeException {
        int runningSize = getSize();
        while (runningSize > 0) {
            TextEncodedStringNullTerminated next = new TextEncodedStringNullTerminated(identifier, frameBody);
            if (next.getSize() == 0) {
                break;
            } else {
                if (value != null) {
                    //Add to value
                    ((Values) value).add((String) next.getValue());

                    //Add to size calculation
                    runningSize -= next.getSize();
                }
            }
        }
    }

    /**
     * For every String write to byte array
     *
     * @return byte[] that should be written to file to persist this data type.
     */
    public byte[] writeByteArray() {

        int localSize = 0;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            if (null != value) {
                for (String s : ((Values) value).getList()) {
                    TextEncodedStringNullTerminated next =
                            new TextEncodedStringNullTerminated(identifier, frameBody, s);
                    buffer.write(next.writeByteArray());
                    localSize += next.getSize();
                }
            } else {
                return EMPTY_BYTE_ARRAY;
            }
        } catch (IOException ioe) {
            //This should never happen because the write is internal with the JVM it is not to a file
            throw new RuntimeException(ioe);
        }

        //Update size member variable
        size = localSize;
        return buffer.toByteArray();
    }

    /**
     * This holds the values held by a MultipleTextEncodedData type
     */
    public static class Values {
        private final List<String> valueList = new ArrayList<>();

        public Values() {

        }

        /**
         * Add String Data type to the value list
         *
         * @param value to add to the list
         */
        public void add(String value) {
            valueList.add(value);
        }


        /**
         * Return the list of values
         *
         * @return the list of values
         */
        public List<String> getList() {
            return valueList;
        }

        /**
         * @return no of values
         */
        @SuppressWarnings("unused")
        public int getNumberOfValues() {
            return valueList.size();
        }

        /**
         * Return the list of values as a single string separated by a comma
         *
         * @return a string representation of the value
         */
        @NotNull
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (ListIterator<String> li = valueList.listIterator(); li.hasNext(); ) {
                String next = li.next();
                sb.append(next);
                if (li.hasNext()) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }
    }
}
