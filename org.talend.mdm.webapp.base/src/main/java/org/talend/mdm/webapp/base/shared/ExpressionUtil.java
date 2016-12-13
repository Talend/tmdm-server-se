/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.shared;

import java.util.ArrayList;
import java.util.List;


public class ExpressionUtil {

    private String expression;

    private int peek = ' ';

    private int charIndex = 0;

    private boolean inString = false;

    private char delimiter = ' ';

    private int inPredicates = 0;

    public ExpressionUtil(String expression){
        this.expression = expression;
    }

    private boolean peekNotIn(char... cs) {
        for (char c : cs) {
            if (peek == c)
                return false;
        }
        return true;
    }

    private boolean inXpath() {
        if (peek == -1)
            return false;
        return inPredicates > 0 || (peekNotIn(',', ')', '+', '-', '*', '=', '!', '<', '>', ' ') && !inString);
    }

    public List<String> getDepTypes() {
        charIndex = 0;
        peek = ' ';
        inString = false;
        List<String> typePathes = new ArrayList<String>();
        while (peek != -1) {
            String xpath = scan();
            if (xpath != null) {
                String typePath = xpath.replaceAll("\\[.+?\\]", ""); //$NON-NLS-1$//$NON-NLS-2$
                typePath = typePath.startsWith("/") ? typePath.substring(1) : typePath; //$NON-NLS-1$
                typePathes.add(typePath);
            }
        }
        return typePathes;
    }

    private void readch() {
        if (charIndex == expression.length()) {
            peek = -1;
            return;
        }
        peek = expression.charAt(charIndex);
        if (inString) {
            if (peek == delimiter) {
                inString = false;
                delimiter = ' ';
            }
        } else {
            if (peek == '\"' || peek == '\'') {
                inString = true;
                delimiter = (char) peek;
            }
        }
        
        if (!inString){
            if (peek == '[') {
                inPredicates++;
            }
            if (peek == ']') {
                inPredicates--;
            }
        }
        charIndex++;
    }

    public String scan() {
        while (true) {
            readch();
            if (peek == ' ' || peek == '\t' || peek == '\n')
                continue;
            else
                break;
        }

        while (inString) {
            readch();
        }

        if (peek == '/') {

            StringBuffer b = new StringBuffer();
            do {
                b.append((char) peek);
                readch();
            } while (inXpath());
            return b.toString();
        }

        if (peek == '.') {
            StringBuffer b = new StringBuffer();
            b.append((char) peek);
            readch();
            if (peek == '.') {
                do {
                    b.append((char) peek);
                    readch();
                } while (inXpath());
                return b.toString();
            }
            if (peek == '/') {
                do {
                    b.append((char) peek);
                    readch();
                } while (inXpath());
                return b.toString();
            }
        }
        return null;
    }
}
