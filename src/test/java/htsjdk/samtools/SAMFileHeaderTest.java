/*
 * The MIT License
 *
 * Copyright (c) 2017 Nils Homer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 */
package htsjdk.samtools;

import htsjdk.HtsjdkTest;
import htsjdk.samtools.util.BufferedLineReader;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.Log;
import htsjdk.samtools.util.ProgressLogger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import static htsjdk.samtools.Defaults.CREATE_INDEX;
import static org.apache.commons.compress.archivers.ar.ArArchiveEntry.HEADER;

public class SAMFileHeaderTest extends HtsjdkTest {

    @Test
    public void testSortOrder() {
        final SAMFileHeader header = new SAMFileHeader();

        header.setSortOrder(SAMFileHeader.SortOrder.coordinate);
        Assert.assertEquals(header.getSortOrder(), SAMFileHeader.SortOrder.coordinate);
        Assert.assertEquals(header.getAttribute(SAMFileHeader.SORT_ORDER_TAG),
                            SAMFileHeader.SortOrder.coordinate.name());

        header.setAttribute(SAMFileHeader.SORT_ORDER_TAG, SAMFileHeader.SortOrder.queryname.name());
        Assert.assertEquals(header.getSortOrder(), SAMFileHeader.SortOrder.queryname);
        Assert.assertEquals(header.getAttribute(SAMFileHeader.SORT_ORDER_TAG),
                            SAMFileHeader.SortOrder.queryname.name());

        header.setAttribute(SAMFileHeader.SORT_ORDER_TAG, SAMFileHeader.SortOrder.coordinate);
        Assert.assertEquals(header.getSortOrder(), SAMFileHeader.SortOrder.coordinate);
        Assert.assertEquals(header.getAttribute(SAMFileHeader.SORT_ORDER_TAG),
                            SAMFileHeader.SortOrder.coordinate.name());

        header.setAttribute(SAMFileHeader.SORT_ORDER_TAG, "UNKNOWN");
        Assert.assertEquals(header.getSortOrder(), SAMFileHeader.SortOrder.unknown);
        Assert.assertEquals(header.getAttribute(SAMFileHeader.SORT_ORDER_TAG),
                            SAMFileHeader.SortOrder.unknown.name());

        header.setAttribute(SAMFileHeader.SORT_ORDER_TAG, "uNknOWn");
        Assert.assertEquals(header.getSortOrder(), SAMFileHeader.SortOrder.unknown);
        Assert.assertEquals(header.getAttribute(SAMFileHeader.SORT_ORDER_TAG),
                            SAMFileHeader.SortOrder.unknown.name());

        header.setAttribute(SAMFileHeader.SORT_ORDER_TAG, "cOoRdinate");
        Assert.assertEquals(header.getSortOrder(), SAMFileHeader.SortOrder.coordinate);
        Assert.assertEquals(header.getAttribute(SAMFileHeader.SORT_ORDER_TAG),
                            SAMFileHeader.SortOrder.coordinate.name());
    }

    @Test
    public void testGroupOrder() {
        final SAMFileHeader header = new SAMFileHeader();

        header.setGroupOrder(SAMFileHeader.GroupOrder.query);
        Assert.assertEquals(header.getGroupOrder(), SAMFileHeader.GroupOrder.query);
        Assert.assertEquals(header.getAttribute(SAMFileHeader.GROUP_ORDER_TAG),
                            SAMFileHeader.GroupOrder.query.name());

        header.setAttribute(SAMFileHeader.GROUP_ORDER_TAG, SAMFileHeader.GroupOrder.reference.name());
        Assert.assertEquals(header.getGroupOrder(), SAMFileHeader.GroupOrder.reference);
        Assert.assertEquals(header.getAttribute(SAMFileHeader.GROUP_ORDER_TAG),
                            SAMFileHeader.GroupOrder.reference.name());

        header.setAttribute(SAMFileHeader.GROUP_ORDER_TAG, SAMFileHeader.GroupOrder.query);
        Assert.assertEquals(header.getGroupOrder(), SAMFileHeader.GroupOrder.query);
        Assert.assertEquals(header.getAttribute(SAMFileHeader.GROUP_ORDER_TAG),
                            SAMFileHeader.GroupOrder.query.name());
    }

    @Test
    public void testGetSequenceIfSequenceDictionaryIsEmpty() {
        final SAMFileHeader header = new SAMFileHeader();
        header.setSequenceDictionary(null);

        Assert.assertNull(header.getSequence("chr1"));
    }

    @Test
    public void testGetSequenceIfNameIsNotFound() {
        final SAMFileHeader header = new SAMFileHeader();
        final SAMSequenceRecord rec = new SAMSequenceRecord("chr1", 1);
        final SAMSequenceDictionary dict = new SAMSequenceDictionary(Arrays.asList(rec));
        header.setSequenceDictionary(dict);

        Assert.assertNull(header.getSequence("chr2"));
    }

    @DataProvider(name = "dataForTestTagsWithImproperCase")
    public Object[][] dataForTestTagsWithImproperCase() {
        return new Object[][]{
                {"@hd\tVN:1.0\tSO:unsorted\n"},
                {"@sq\tSN:chrM\tLN:16571\n"},
                {"@rg\tID:1\tSM:sample1\n"},
                {"@pg\tID:1\tPN:A\n"},
                {"@co\tVN:1.0\tSO:unsorted\n"}
        };
    }

    @Test(dataProvider = "dataForTestTagsWithImproperCase")
    public void testTagsWithImproperCase(String s) {
        Assert.assertEquals(
                new SAMTextHeaderCodec().decode(BufferedLineReader.fromString(s), null)
                                        .getValidationErrors()
                                        .toString()
                                        .contains("Unrecognized header record type"), false);
    }
}
