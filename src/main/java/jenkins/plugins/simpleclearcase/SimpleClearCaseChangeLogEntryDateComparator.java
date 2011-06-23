/**
 * The MIT License
 * 
 * Copyright (c) 2011, Sun Microsystems, Inc., Sam Tavakoli
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jenkins.plugins.simpleclearcase;

import java.util.Comparator;

/*
 * This is used primarily to order ChangeLog entries by date than the order of
 * load rule The order of the date is decided through the increasing parameter
 * when creating the comparator
 */

public class SimpleClearCaseChangeLogEntryDateComparator implements
                                                               Comparator<SimpleClearCaseChangeLogEntry> {

    public static final int DECREASING = -1;
    public static final int INCREASING = 1;

    private final int order;

    /**
     * @param order
     */
    public SimpleClearCaseChangeLogEntryDateComparator(int order) {
        this.order = order;
    }

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(SimpleClearCaseChangeLogEntry e1, SimpleClearCaseChangeLogEntry e2) {
        // depending on the order value we either have decreasing or increasing order
        return (e1.getDate().compareTo(e2.getDate()) * this.order);
    }
}
