// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.storage.hibernate;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SimpleFieldComparator;
import org.apache.lucene.util.Bits;

/**
 * Numeric type PKs will be indexed as string via {@link IntegerBridge}, {@link LongBridge}, {@link DoubleBridge}, {@link FloatBridge}, {@link ShortBridge}, {@link ByteBridge}
 * But need to sort as number via custom comparators.<br />
 * 
 * created by pwlin on Sep 25, 2020
 *
 */
public class FieldComparators {

    public static class ByteComparator extends NumberComparator<Byte> {

        private final byte[] values;

        private byte bottom;

        private byte topValue;

        public ByteComparator(int numHits, String field, Byte missingValue) {
            super(field, missingValue);
            this.values = new byte[numHits];
        }

        @Override
        public int compare(int slot1, int slot2) {
            return Byte.compare(values[slot1], values[slot2]);
        }

        @Override
        public int compareBottom(int doc) {
            byte v2 = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }

            return Byte.compare(bottom, v2);
        }

        @Override
        public int compareTop(int doc) {
            byte docValue = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && docValue == 0 && !docsWithField.get(doc)) {
                docValue = missingValue;
            }
            return Byte.compare(topValue, docValue);
        }

        @Override
        public void copy(int slot, int doc) {
            byte v2 = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }

            values[slot] = v2;
        }

        @Override
        protected Byte getNumberValue(byte[] b) {
            ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES);
            buffer.put(getFullBytes(b, Byte.BYTES)).flip();
            return buffer.get();
        }

        @Override
        public void setBottom(final int bottom) {
            this.bottom = values[bottom];
        }

        @Override
        public void setTopValue(Byte value) {
            topValue = value;
        }

        @Override
        public Byte value(int slot) {
            return Byte.valueOf(values[slot]);
        }
    }

    public static class ByteComparatorSource extends FieldComparatorSource {

        @Override
        public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
            return new ByteComparator(numHits, fieldname, Byte.MIN_VALUE);
        }
    }

    public static class DoubleComparator extends NumberComparator<Double> {

        private final double[] values;

        private double bottom;

        private double topValue;

        public DoubleComparator(int numHits, String field, Double missingValue) {
            super(field, missingValue);
            this.values = new double[numHits];
        }

        @Override
        public int compare(int slot1, int slot2) {
            return Double.compare(values[slot1], values[slot2]);
        }

        @Override
        public int compareBottom(int doc) {
            double v2 = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }

            return Double.compare(bottom, v2);
        }

        @Override
        public int compareTop(int doc) {
            double docValue = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && docValue == 0 && !docsWithField.get(doc)) {
                docValue = missingValue;
            }
            return Double.compare(topValue, docValue);
        }

        @Override
        public void copy(int slot, int doc) {
            double v2 = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }

            values[slot] = v2;
        }

        @Override
        protected Double getNumberValue(byte[] b) {
            ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
            buffer.put(getFullBytes(b, Double.BYTES)).flip();
            return buffer.getDouble();
        }

        @Override
        public void setBottom(final int bottom) {
            this.bottom = values[bottom];
        }

        @Override
        public void setTopValue(Double value) {
            topValue = value;
        }

        @Override
        public Double value(int slot) {
            return Double.valueOf(values[slot]);
        }
    }

    public static class DoubleComparatorSource extends FieldComparatorSource {

        @Override
        public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
            return new DoubleComparator(numHits, fieldname, Double.MIN_VALUE);
        }
    }

    public static class FloatComparator extends NumberComparator<Float> {

        private final float[] values;

        private float bottom;

        private float topValue;

        public FloatComparator(int numHits, String field, Float missingValue) {
            super(field, missingValue);
            this.values = new float[numHits];
        }

        @Override
        public int compare(int slot1, int slot2) {
            return Float.compare(values[slot1], values[slot2]);
        }

        @Override
        public int compareBottom(int doc) {
            float v2 = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }

            return Float.compare(bottom, v2);
        }

        @Override
        public int compareTop(int doc) {
            float docValue = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && docValue == 0 && !docsWithField.get(doc)) {
                docValue = missingValue;
            }
            return Float.compare(topValue, docValue);
        }

        @Override
        public void copy(int slot, int doc) {
            float v2 = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }

            values[slot] = v2;
        }

        @Override
        protected Float getNumberValue(byte[] b) {
            ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
            buffer.put(getFullBytes(b, Float.BYTES)).flip();
            return buffer.getFloat();
        }

        @Override
        public void setBottom(final int bottom) {
            this.bottom = values[bottom];
        }

        @Override
        public void setTopValue(Float value) {
            topValue = value;
        }

        @Override
        public Float value(int slot) {
            return Float.valueOf(values[slot]);
        }
    }

    public static class FloatComparatorSource extends FieldComparatorSource {

        @Override
        public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
            return new FloatComparator(numHits, fieldname, Float.MIN_VALUE);
        }
    }

    public static class IntComparator extends NumberComparator<Integer> {

        private final int[] values;

        private int bottom;

        private int topValue;

        public IntComparator(int numHits, String field, Integer missingValue) {
            super(field, missingValue);
            this.values = new int[numHits];
        }

        @Override
        public int compare(int slot1, int slot2) {
            return Integer.compare(values[slot1], values[slot2]);
        }

        @Override
        public int compareBottom(int doc) {
            int v2 = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }

            return Integer.compare(bottom, v2);
        }

        @Override
        public int compareTop(int doc) {
            int docValue = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && docValue == 0 && !docsWithField.get(doc)) {
                docValue = missingValue;
            }
            return Integer.compare(topValue, docValue);
        }

        @Override
        public void copy(int slot, int doc) {
            int v2 = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }

            values[slot] = v2;
        }

        @Override
        protected Integer getNumberValue(byte[] b) {
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.put(getFullBytes(b, Integer.BYTES)).flip();
            return buffer.getInt();
        }

        @Override
        public void setBottom(final int bottom) {
            this.bottom = values[bottom];
        }

        @Override
        public void setTopValue(Integer value) {
            topValue = value;
        }

        @Override
        public Integer value(int slot) {
            return Integer.valueOf(values[slot]);
        }
    }

    public static class IntComparatorSource extends FieldComparatorSource {

        @Override
        public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
            return new IntComparator(numHits, fieldname, Integer.MIN_VALUE);
        }
    }

    public static class LongComparator extends NumberComparator<Long> {

        private final long[] values;

        private long bottom;

        private long topValue;

        public LongComparator(int numHits, String field, Long missingValue) {
            super(field, missingValue);
            this.values = new long[numHits];
        }

        @Override
        public int compare(int slot1, int slot2) {
            return Long.compare(values[slot1], values[slot2]);
        }

        @Override
        public int compareBottom(int doc) {
            long v2 = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }

            return Long.compare(bottom, v2);
        }

        @Override
        public int compareTop(int doc) {
            long docValue = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && docValue == 0 && !docsWithField.get(doc)) {
                docValue = missingValue;
            }
            return Long.compare(topValue, docValue);
        }

        @Override
        public void copy(int slot, int doc) {
            long v2 = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }

            values[slot] = v2;
        }

        @Override
        protected Long getNumberValue(byte[] b) {
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.put(getFullBytes(b, Long.BYTES)).flip();
            return buffer.getLong();
        }

        @Override
        public void setBottom(final int bottom) {
            this.bottom = values[bottom];
        }

        @Override
        public void setTopValue(Long value) {
            topValue = value;
        }

        @Override
        public Long value(int slot) {
            return Long.valueOf(values[slot]);
        }
    }

    public static class LongComparatorSource extends FieldComparatorSource {

        @Override
        public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
            return new LongComparator(numHits, fieldname, Long.MIN_VALUE);
        }
    }

    /**
     * Reference {@link NumericComparator}
     *
     */
    public static abstract class NumberComparator<T extends Number> extends SimpleFieldComparator<T> {

        protected final T missingValue;

        protected final String field;

        protected Bits docsWithField;

        protected SortedDocValues currentReaderValues;

        public NumberComparator(String field, T missingValue) {
            this.field = field;
            this.missingValue = missingValue;
        }

        @Override
        protected void doSetNextReader(LeafReaderContext context) throws IOException {
            currentReaderValues = DocValues.getSorted(context.reader(), field);
            if (missingValue != null) {
                docsWithField = DocValues.getDocsWithField(context.reader(), field);
                // optimization to remove unneeded checks on the bit interface:
                if (docsWithField instanceof Bits.MatchAllBits) {
                    docsWithField = null;
                }
            } else {
                docsWithField = null;
            }
        }

        protected byte[] getFullBytes(byte[] b, int length) {
            byte[] bytes = new byte[length];
            if (b.length < length) {
                for (int i = 0; i < length; i++) {
                    if (i < b.length) {
                        bytes[i] = b[i];
                    } else {
                        bytes[i] = 0;
                    }
                }
            }
            return bytes;
        }

        protected abstract T getNumberValue(byte[] b);
    }

    public static class ShortComparator extends NumberComparator<Short> {

        private final short[] values;

        private short bottom;

        private short topValue;

        public ShortComparator(int numHits, String field, Short missingValue) {
            super(field, missingValue);
            this.values = new short[numHits];
        }

        @Override
        public int compare(int slot1, int slot2) {
            return Short.compare(values[slot1], values[slot2]);
        }

        @Override
        public int compareBottom(int doc) {
            short v2 = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }

            return Short.compare(bottom, v2);
        }

        @Override
        public int compareTop(int doc) {
            short docValue = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && docValue == 0 && !docsWithField.get(doc)) {
                docValue = missingValue;
            }
            return Short.compare(topValue, docValue);
        }

        @Override
        public void copy(int slot, int doc) {
            short v2 = getNumberValue(currentReaderValues.get(doc).bytes);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }

            values[slot] = v2;
        }

        @Override
        protected Short getNumberValue(byte[] b) {
            ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
            buffer.put(getFullBytes(b, Short.BYTES)).flip();
            return buffer.getShort();
        }

        @Override
        public void setBottom(final int bottom) {
            this.bottom = values[bottom];
        }

        @Override
        public void setTopValue(Short value) {
            topValue = value;
        }

        @Override
        public Short value(int slot) {
            return Short.valueOf(values[slot]);
        }
    }

    public static class ShortComparatorSource extends FieldComparatorSource {

        @Override
        public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
            return new ShortComparator(numHits, fieldname, Short.MIN_VALUE);
        }
    }
}
